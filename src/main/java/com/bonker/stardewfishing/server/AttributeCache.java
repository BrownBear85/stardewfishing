package com.bonker.stardewfishing.server;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class AttributeCache {
    public static final Map<Player, Map<Attribute, Double>> CACHE_MAP = new HashMap<>();

    public static double getAttribute(Player player, Attribute attribute) {
        if (CACHE_MAP.containsKey(player)) {
            return CACHE_MAP.get(player).getOrDefault(attribute, attribute.getDefaultValue());
        } else {
            return attribute.getDefaultValue();
        }
    }

    public static void add(Player player) {
        AttributeMap playerAttributes = player.getAttributes();

        Map<Attribute, Double> cache = new HashMap<>();
        playerAttributes.attributes.forEach((attribute, inst) -> cache.put(attribute.value(), inst.getValue()));
        CACHE_MAP.put(player, cache);
    }

    public static void remove(Player player) {
        CACHE_MAP.remove(player);
    }
}
