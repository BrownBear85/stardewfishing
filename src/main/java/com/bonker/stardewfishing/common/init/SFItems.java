package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.items.SFBobberItem;
import com.bonker.stardewfishing.common.items.SFTooltipItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class SFItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, StardewFishing.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StardewFishing.MODID);

    public static final int LEGENDARY_FISH_COLOR = 0xFFFFBE;
    public static final Component LEGENDARY_FISH_TOOLTIP = Component.translatable("tooltip.stardew_fishing.legendary_fish")
            .withStyle(StardewFishing.LEGENDARY);

    public static final RegistryObject<Item> TRAP_BOBBER = ITEMS.register("trap_bobber",
            () -> new SFBobberItem(new Item.Properties().durability(64)));

    public static final RegistryObject<Item> CORK_BOBBER = ITEMS.register("cork_bobber",
            () -> new SFBobberItem(new Item.Properties().durability(64)));

    public static final RegistryObject<Item> SONAR_BOBBER = ITEMS.register("sonar_bobber",
            () -> new SFBobberItem(new Item.Properties().durability(64)));

    public static final RegistryObject<Item> TREASURE_BOBBER = ITEMS.register("treasure_bobber",
            () -> new SFBobberItem(new Item.Properties().durability(64)));

    public static final RegistryObject<Item> QUALITY_BOBBER = ITEMS.register("quality_bobber",
            () -> new SFBobberItem(new Item.Properties().durability(64)) {
                @Override
                protected List<Component> makeTooltip() {
                    List<Component> tooltip = super.makeTooltip();
                    if (StardewFishing.QUALITY_FOOD_INSTALLED) {
                        tooltip.add(1, Component.translatable(getDescriptionId() + ".quality_food_tooltip").withStyle(StardewFishing.LIGHTER_COLOR));
                    }
                    return tooltip;
                }
            });

    public static final RegistryObject<Item> GOLIATH_GROUPER = ITEMS.register("goliath_grouper",
            () -> new SFTooltipItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(30)
                            .saturationMod(40f)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 2), 1.0f)
                            .build())));

    public static final RegistryObject<Item> VAMPIRE_PAYARA = ITEMS.register("vampire_payara",
            () -> new SFTooltipItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(30)
                            .saturationMod(40f)
                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 3600, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 12000, 0), 1.0f)
                            .build())));

    public static final RegistryObject<Item> GOLDEN_SNOOK = ITEMS.register("golden_snook",
            () -> new SFTooltipItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(30)
                            .saturationMod(40f)
                            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 12000, 4), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.LUCK, 12000, 1), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.GLOWING, 12000, 0), 1.0f)
                            .build())));

    public static final RegistryObject<Item> SABRETOOTHED_TIGERFISH = ITEMS.register("sabretoothed_tigerfish",
            () -> new SFTooltipItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(30)
                            .saturationMod(40f)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 3), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 6000, 3), 1.0f)
                            .build())));

    public static final RegistryObject<Item> CHROMATIC_ARAPAIMA = ITEMS.register("chromatic_arapaima",
            () -> new SFTooltipItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(30)
                            .saturationMod(40f)
                            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, 12000, 0), 1.0f)
                            .build())));

    public static final RegistryObject<Item> CYCLOPS_MAHIMAHI = ITEMS.register("cyclops_mahimahi",
            () -> new SFTooltipItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(30)
                            .saturationMod(40f)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.JUMP, 3600, 2), 1.0f)
                            .build())));

    public static final RegistryObject<Item> STORM_TARPON = ITEMS.register("storm_tarpon",
            () -> new SFTooltipItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(30)
                            .saturationMod(40f)
                            .effect(() -> new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 8000, 0), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.CONDUIT_POWER, 8000, 0), 1.0f)
                            .build())));

    public static final RegistryObject<Item> BLAZING_OARFISH = ITEMS.register("blazing_oarfish",
            () -> new SFTooltipItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(30)
                            .saturationMod(40f)
                            .effect(() -> new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 3600, 1), 1.0f)
                            .build())));

    public static final RegistryObject<Item> CRYSTALLINE_SNAKEHEAD = ITEMS.register("crystalline_snakehead",
            () -> new SFTooltipItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(30)
                            .saturationMod(40f)
                            .effect(() -> new MobEffectInstance(MobEffects.LUCK, 12000, 1), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 2400, 2), 1.0f)
                            .build())));

    public static final RegistryObject<Item> DEMON_GAR = ITEMS.register("demon_gar",
            () -> new SFTooltipItem(new Item.Properties()
                    .food(new FoodProperties.Builder()
                            .nutrition(30)
                            .saturationMod(40f)
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 6000, 2), 1.0f)
                            .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 6000, 3), 1.0f)
                            .build())));

    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("items", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.stardewFishing"))
            .icon(() -> new ItemStack(SABRETOOTHED_TIGERFISH.get()))
            .displayItems((pParameters, pOutput) -> {
                pOutput.accept(SFBlocks.FISH_DISPLAY.get());
                pOutput.accept(TRAP_BOBBER.get());
                pOutput.accept(CORK_BOBBER.get());
                pOutput.accept(SONAR_BOBBER.get());
                pOutput.accept(TREASURE_BOBBER.get());
                pOutput.accept(QUALITY_BOBBER.get());
                pOutput.accept(GOLIATH_GROUPER.get());
                pOutput.accept(VAMPIRE_PAYARA.get());
                pOutput.accept(GOLDEN_SNOOK.get());
                pOutput.accept(SABRETOOTHED_TIGERFISH.get());
                pOutput.accept(CHROMATIC_ARAPAIMA.get());
                pOutput.accept(CYCLOPS_MAHIMAHI.get());
                pOutput.accept(STORM_TARPON.get());
                pOutput.accept(BLAZING_OARFISH.get());
                pOutput.accept(CRYSTALLINE_SNAKEHEAD.get());
                pOutput.accept(DEMON_GAR.get());
            })
            .build());
}
