package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Function;

public class SFAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, StardewFishing.MODID);

    public static final RegistryObject<RangedAttribute> LINE_STRENGTH = register("line_strength",
            descriptionId -> new RangedAttribute(descriptionId, 1, 0, 2));

    public static final RegistryObject<RangedAttribute> BAR_SIZE = register("bar_size",
            descriptionId -> new RangedAttribute(descriptionId, 36, 16, 142));

    public static final RegistryObject<RangedAttribute> TREASURE_CHANCE_BONUS = register("treasure_chance_bonus",
            descriptionId -> new RangedAttribute(descriptionId, 0, 0, 1));

    public static final RegistryObject<RangedAttribute> GOLDEN_CHEST_BONUS = register("golden_chest_bonus",
            descriptionId -> new RangedAttribute(descriptionId, 0, 0, 1));

    public static final RegistryObject<RangedAttribute> EXP_MULTIPLIER = register("exp_multiplier",
            descriptionId -> new RangedAttribute(descriptionId, 1, 0, 1000));

    private static <T extends Attribute> RegistryObject<T> register(String id, Function<String, T> constructor) {
        return ATTRIBUTES.register(id, () -> constructor.apply("attribute.name." + StardewFishing.MODID + "." + id));
    }
}
