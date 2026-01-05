package com.bonker.stardewfishing.common.items;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record Bobber(ItemStack item) {
    public static final Codec<Bobber> CODEC = ItemStack.OPTIONAL_CODEC.xmap(Bobber::new, Bobber::item);

    public static final StreamCodec<RegistryFriendlyByteBuf, Bobber> STREAM_CODEC = ItemStack.STREAM_CODEC.map(Bobber::new, Bobber::item);
}
