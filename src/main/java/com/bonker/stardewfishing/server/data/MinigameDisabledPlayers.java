package com.bonker.stardewfishing.server.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@NullMarked
public class MinigameDisabledPlayers extends SavedData {
    public static final Codec<MinigameDisabledPlayers> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            UUIDUtil.CODEC.listOf().fieldOf("minigame_disabled_for").forGetter(o -> o.players)
    ).apply(inst, MinigameDisabledPlayers::new));

    public static final SavedDataType<MinigameDisabledPlayers> TYPE = new SavedDataType<>("minigame_disabled_players", MinigameDisabledPlayers::new, CODEC);

    private final List<UUID> players = new ArrayList<>();

    private MinigameDisabledPlayers() {

    }

    private MinigameDisabledPlayers(List<UUID> list) {
        players.addAll(list);
    }

    public boolean isMinigameDisabled(ServerPlayer player) {
        return players.contains(player.getUUID());
    }

    public void setMinigameDisabled(ServerPlayer player, boolean disabled) {
        UUID uuid = player.getUUID();
        if (disabled) {
            if (!players.contains(uuid)) {
                players.add(uuid);
                setDirty();
            }
        } else {
            if (players.contains(uuid)) {
                players.remove(uuid);
                setDirty();
            }
        }
    }

    public static MinigameDisabledPlayers get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }
}
