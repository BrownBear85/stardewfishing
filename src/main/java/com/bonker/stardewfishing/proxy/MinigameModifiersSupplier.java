package com.bonker.stardewfishing.proxy;

import com.bonker.stardewfishing.server.data.MinigameModifiers;
import net.minecraft.world.item.Item;

import java.util.Map;

public interface MinigameModifiersSupplier {
    Map<Item, MinigameModifiers> getData();
}