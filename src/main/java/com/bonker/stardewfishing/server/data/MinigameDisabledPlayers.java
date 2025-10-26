package com.bonker.stardewfishing.server.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MinigameDisabledPlayers extends SavedData {
    private final List<UUID> players = new ArrayList<>();

    private MinigameDisabledPlayers() {

    }

    private MinigameDisabledPlayers(CompoundTag compoundTag) {
        ListTag list = compoundTag.getList("minigame_disabled_for", Tag.TAG_INT_ARRAY);
        for (Tag tag : list) {
            players.add(NbtUtils.loadUUID(tag));
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        ListTag list = new ListTag();
        for (UUID uuid : players) {
            list.add(NbtUtils.createUUID(uuid));
        }
        compoundTag.put("minigame_disabled_for", list);
        return compoundTag;
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
        return server.overworld().getDataStorage().computeIfAbsent(MinigameDisabledPlayers::new, MinigameDisabledPlayers::new, "disabled_minigame_players");
    }
}