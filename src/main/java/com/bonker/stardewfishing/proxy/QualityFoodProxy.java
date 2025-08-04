package com.bonker.stardewfishing.proxy;

import de.cadentem.quality_food.core.codecs.Quality;
import de.cadentem.quality_food.core.codecs.QualityType;
import de.cadentem.quality_food.registry.QFComponents;
import de.cadentem.quality_food.util.QualityUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.CommonHooks;

public class QualityFoodProxy {
    public static final ResourceKey<QualityType> IRON = ResourceKey.create(QFComponents.QUALITY_TYPE_REGISTRY,
            ResourceLocation.fromNamespaceAndPath("quality_food", "iron"));

    public static final ResourceKey<QualityType> GOLD = ResourceKey.create(QFComponents.QUALITY_TYPE_REGISTRY,
            ResourceLocation.fromNamespaceAndPath("quality_food", "gold"));

    public static final ResourceKey<QualityType> DIAMOND = ResourceKey.create(QFComponents.QUALITY_TYPE_REGISTRY,
            ResourceLocation.fromNamespaceAndPath("quality_food", "diamond"));

    public static void applyQuality(ItemStack stack, int quality) {
        stack.remove(QFComponents.QUALITY_DATA_COMPONENT);

        if (quality == 0) {
            QualityUtils.applyQuality(stack, Quality.NONE);
        } else {
            ResourceKey<QualityType> key;
            if (quality == 3) key = DIAMOND;
            else if (quality == 2) key = GOLD;
            else key = IRON;

            HolderLookup.RegistryLookup<QualityType> registryAccess = CommonHooks.resolveLookup(QFComponents.QUALITY_TYPE_REGISTRY);
            if (registryAccess != null) {
                Holder<QualityType> type = registryAccess.getOrThrow(key);
                QualityUtils.applyQuality(stack, type);
            }
        }
    }
}
