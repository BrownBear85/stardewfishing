package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SFAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, StardewFishing.MODID);

    public static final DeferredHolder<Attribute, Attribute> LINE_STRENGTH = ATTRIBUTES.register("line_strength",
            () -> new RangedAttribute("attribute.name.stardew_fishing.line_strength", 0, 0, 1));

    public static final DeferredHolder<Attribute, Attribute> BAR_SIZE = ATTRIBUTES.register("bar_size",
            () -> new RangedAttribute("attribute.name.stardew_fishing.bar_size", 36, 4, 142));

    public static final DeferredHolder<Attribute, Attribute> TREASURE_CHANCE_BONUS = ATTRIBUTES.register("treasure_chance_bonus",
            () -> new RangedAttribute("attribute.name.stardew_fishing.treasure_chance_bonus", 0, 0, 1));

    public static final DeferredHolder<Attribute, Attribute> EXPERIENCE_MULTIPLIER = ATTRIBUTES.register("exp_multiplier",
            () -> new RangedAttribute("attribute.name.stardew_fishing.exp_multiplier", 1, 1, 100));
}
