package com.bonker.stardewfishing.server.data;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.server.ModifierOperation;
import com.bonker.stardewfishing.server.event.StardewMinigameStartedEvent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;

public final class MinigameModifiers {
    private static final Component PIXELS = Component.translatable("tooltip.stardew_fishing.modifier.pixels");
    private static final Component SECONDS = Component.translatable("tooltip.stardew_fishing.modifier.seconds");
    private static final Component PIXELS_PER_SECOND = Component.translatable("tooltip.stardew_fishing.modifier.pixels_per_second");
    private static final Component PIXELS_PER_SECOND_SQUARED = Component.translatable("tooltip.stardew_fishing.modifier.pixels_per_second_squared");
    private static final Component PERCENT = Component.translatable("tooltip.stardew_fishing.modifier.percent");
    private static final Component TIERS = Component.translatable("tooltip.stardew_fishing.modifier.tiers");

    private final ModifierOperation[] operations;
    private final Map<Type, ModifierOperation> modifierMap;
    private final List<Component> tooltip;

    public MinigameModifiers(ModifierOperation... operations) {
        if (operations.length != Type.values().length) {
            throw new RuntimeException("Minigame modifiers does not cover all types: " + Arrays.toString(operations));
        }

        this.operations = operations;

        modifierMap = new TreeMap<>();
        for (int i = 0; i < operations.length; i++) {
            modifierMap.put(Type.values()[i], operations[i]);
        }

        tooltip = makeTooltip();
    }

    public void apply(StardewMinigameStartedEvent event) {
        event.setIdleTime(modifierMap.get(Type.IDLE_TIME).apply(event.getIdleTime()));
        event.setTopSpeed(modifierMap.get(Type.TOP_SPEED).apply(event.getTopSpeed()));
        event.setUpAcceleration(modifierMap.get(Type.UP_ACCELERATION).apply(event.getUpAcceleration()));
        event.setDownAcceleration(modifierMap.get(Type.DOWN_ACCELERATION).apply(event.getDownAcceleration()));
        event.setAvgDistance(modifierMap.get(Type.AVG_DISTANCE).apply(event.getAvgDistance()));
        event.setMoveVariation(modifierMap.get(Type.MOVE_VARIATION).apply(event.getMoveVariation()));
        event.setLineStrength(modifierMap.get(Type.LINE_STRENGTH).apply(event.getLineStrength()));
        event.setBarSize(modifierMap.get(Type.BAR_SIZE).apply(event.getBarSize()));
        event.setTreasureChanceBonus(modifierMap.get(Type.TREASURE_CHANCE_BONUS).apply(event.getTreasureChanceBonus()));
        event.setGoldenChanceBonus(modifierMap.get(Type.GOLDEN_CHANCE_BONUS).apply(event.getGoldenChanceBonus()));
        event.setExpMultiplier(modifierMap.get(Type.EXP_MULTIPLIER).apply(event.getExpMultiplier()));
        event.setQualityBoost(modifierMap.get(Type.QUALITY_BOOST).apply(event.getQualityBoost()));
    }

    public void appendTooltip(List<Component> list) {
        list.addAll(tooltip);
    }

    public MinigameModifiers merge(MinigameModifiers other) {
        for (Type type : Type.values()) {
            modifierMap.computeIfPresent(type, (t, operation) -> operation.merge(other.modifierMap.get(t)));
        }
        return this;
    }

    private List<Component> makeTooltip() {
        List<Component> tooltip = new ArrayList<>();

        boolean combineAccelerations = modifierMap.get(Type.UP_ACCELERATION).equals(modifierMap.get(Type.DOWN_ACCELERATION));

        for (Map.Entry<Type, ModifierOperation> entry : modifierMap.entrySet()) {
            Type type = entry.getKey();
            ModifierOperation operation = entry.getValue();
            
            if ((combineAccelerations && type == Type.DOWN_ACCELERATION) || (!StardewFishing.QUALITY_FOOD_INSTALLED && type == Type.QUALITY_BOOST)) {
                continue;
            }

            if (operation.matters()) {
                String key;
                if (combineAccelerations && type == Type.UP_ACCELERATION) {
                    key = "tooltip.stardew_fishing.modifier.both_acceleration";
                } else {
                    key = "tooltip.stardew_fishing.modifier." + type;
                }

                MutableComponent operationComponent = Component.literal(type == Type.IDLE_TIME ? operation.toString(1.0 / 20) : operation.toString());
                if (operation.type() != ModifierOperation.Type.MULTIPLICATION) {
                    operationComponent = operationComponent.append(type.getUnits());
                }
                boolean good = operation.isPositive() == type.higherIsBetter();
                operationComponent = operationComponent.withStyle(good ? StardewFishing.GREEN : StardewFishing.RED);

                tooltip.add(Component.translatable(key, operationComponent).withStyle(StardewFishing.LIGHTER_COLOR));
            }
        }

        return tooltip;
    }

    public static final Codec<MinigameModifiers> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            m(0), m(1), m(2), m(3), m(4), m(5), m(6), m(7), m(8), m(9), m(10), m(11)
    ).apply(inst, MinigameModifiers::new));


    private static RecordCodecBuilder<MinigameModifiers, ModifierOperation> m(int i) {
        return ModifierOperation.CODEC.optionalFieldOf(Type.values()[i].toString(), ModifierOperation.DEFAULT).forGetter(o -> o.modifierMap.get(Type.values()[i]));
    }

    public void write(FriendlyByteBuf buf) {
        for (ModifierOperation operation : operations) {
            buf.writeByte(operation.type().ordinal());
            buf.writeDouble(operation.value());
        }
    }

    public static MinigameModifiers read(FriendlyByteBuf buf) {
        ModifierOperation[] operations = new ModifierOperation[Type.values().length];
        for (int i = 0; i < operations.length; i++) {
            ModifierOperation.Type type = ModifierOperation.Type.values()[buf.readByte()];
            double value = buf.readDouble();
            operations[i] = new ModifierOperation(type, value);
        }
        return new MinigameModifiers(operations);
    }

    public enum Type {
        IDLE_TIME, TOP_SPEED, UP_ACCELERATION, DOWN_ACCELERATION, AVG_DISTANCE, MOVE_VARIATION,
        LINE_STRENGTH, BAR_SIZE, TREASURE_CHANCE_BONUS, GOLDEN_CHANCE_BONUS, EXP_MULTIPLIER, QUALITY_BOOST;

        public boolean higherIsBetter() {
            return switch (this) {
                case IDLE_TIME, LINE_STRENGTH, BAR_SIZE, TREASURE_CHANCE_BONUS, GOLDEN_CHANCE_BONUS, EXP_MULTIPLIER, QUALITY_BOOST -> true;
                default -> false;
            };
        }

        public Component getUnits() {
            return switch (this) {
                case IDLE_TIME -> SECONDS;
                case AVG_DISTANCE, MOVE_VARIATION, BAR_SIZE -> PIXELS;
                case TOP_SPEED -> PIXELS_PER_SECOND;
                case UP_ACCELERATION, DOWN_ACCELERATION -> PIXELS_PER_SECOND_SQUARED;
                case LINE_STRENGTH, TREASURE_CHANCE_BONUS, GOLDEN_CHANCE_BONUS, EXP_MULTIPLIER -> PERCENT;
                case QUALITY_BOOST -> TIERS;
            };
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }
}
