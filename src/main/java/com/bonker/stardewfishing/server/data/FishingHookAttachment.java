package com.bonker.stardewfishing.server.data;

import com.bonker.stardewfishing.common.init.SFAttachmentTypes;
import com.bonker.stardewfishing.server.LockableList;
import com.bonker.stardewfishing.server.event.StardewMinigameStartedEvent;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public class FishingHookAttachment {
    // Lockable list because I don't want to have to tell the Tide dev to make more changes
    private final LockableList<ItemStack> rewards = new LockableList<>();
    private boolean hasTreasureChest = false;
    private boolean hasGoldenChest = false;
    private StardewMinigameStartedEvent event = null;

    public static FishingHookAttachment get(FishingHook entity) {
        return entity.getData(SFAttachmentTypes.HOOK);
    }

    public LockableList<ItemStack> getRewards() {
        return rewards;
    }

    public boolean hasTreasureChest() {
        return hasTreasureChest;
    }

    public void setTreasureChest(boolean hasTreasureChest) {
        this.hasTreasureChest = hasTreasureChest;
    }

    public boolean hasGoldenChest() {
        return hasGoldenChest;
    }

    public void setGoldenChest(boolean hasGoldenChest) {
        this.hasGoldenChest = hasGoldenChest;
    }

    public StardewMinigameStartedEvent getEvent() {
        return event;
    }

    public void setEvent(StardewMinigameStartedEvent event) {
        this.event = event;
    }
}
