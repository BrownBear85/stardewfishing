package com.bonker.stardewfishing.common.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.Date;

public record LegendaryCatch(String player, long time) {
    public static final Codec<LegendaryCatch> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.STRING.fieldOf("player").forGetter(LegendaryCatch::player),
            Codec.LONG.fieldOf("time").forGetter(LegendaryCatch::time)
    ).apply(inst, LegendaryCatch::new));

    public static final StreamCodec<ByteBuf, LegendaryCatch> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, LegendaryCatch::player,
            ByteBufCodecs.VAR_LONG, LegendaryCatch::time,
            LegendaryCatch::new
    );

    public LegendaryCatch(Player player) {
        this(player.getScoreboardName(), new Date().getTime());
    }
}
