package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;

public class SFAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, StardewFishing.MODID);

    public static final DeferredHolder<Attribute, RangedAttribute> LINE_STRENGTH = register("line_strength",
            descriptionId -> new RangedAttribute(descriptionId, 1, 0, 2));

    public static final DeferredHolder<Attribute, RangedAttribute> BAR_SIZE = register("bar_size",
            descriptionId -> new RangedAttribute(descriptionId, 36, 16, 142));

    public static final DeferredHolder<Attribute, RangedAttribute> TREASURE_CHANCE_BONUS = register("treasure_chance_bonus",
            descriptionId -> new RangedAttribute(descriptionId, 0, 0, 1));

    public static final DeferredHolder<Attribute, RangedAttribute> GOLDEN_CHEST_BONUS = register("golden_chest_bonus",
            descriptionId -> new RangedAttribute(descriptionId, 0, 0, 1));

    public static final DeferredHolder<Attribute, RangedAttribute> EXP_MULTIPLIER = register("exp_multiplier",
            descriptionId -> new RangedAttribute(descriptionId, 1, 0, 1000));

    private static <T extends Attribute> DeferredHolder<Attribute, T> register(String id, Function<String, T> constructor) {
        return ATTRIBUTES.register(id, () -> constructor.apply("attribute.name." + StardewFishing.MODID + "." + id));
    }
}
