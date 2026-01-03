package com.bonker.stardewfishing.common.items;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.common.init.SFComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PokemonPlaceholderItem extends Item {
    public PokemonPlaceholderItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull Component getName(ItemStack stack) {
        String species = stack.get(SFComponentTypes.POKEMON_SPECIES);
        species = species == null ? "Unknown Pokémon" : species;
        if (Character.isLowerCase(species.charAt(0))) {
            species = Character.toUpperCase(species.charAt(0)) + species.substring(1);
        }
        return Component.literal(species).setStyle(StardewFishing.LIGHTER_COLOR);
    }
}
