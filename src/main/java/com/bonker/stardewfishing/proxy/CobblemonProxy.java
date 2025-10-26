package com.bonker.stardewfishing.proxy;

//import com.cobblemon.mod.common.api.spawning.SpawnBucket;
//import com.cobblemon.mod.common.entity.fishing.PokeRodFishingBobberEntity;
//import com.cobblemon.mod.common.item.interactive.PokerodItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;

public class CobblemonProxy {
    public static void spawnPokemon(FishingHook hook, ServerPlayer player, Object obj, ItemStack fishingRod) {
//        if (!(hook instanceof PokeRodFishingBobberEntity pokeHook) || !(obj instanceof SpawnBucket bucket)) {
//            return;
//        }
//
//        if (pokeHook.spawnPokemonFromFishing(player, bucket, fishingRod)) {
//            PokerodItem.Companion.consumeBait(fishingRod);
//        }
//
//        float angle = Mth.nextFloat(hook.getRandom(), 0, 360) * Mth.DEG_TO_RAD;
//        float dist = Mth.nextFloat(hook.getRandom(), 25, 60);
//        double x = hook.getX() + (Mth.sin(angle) * dist) * 0.1;
//        player.serverLevel().sendParticles(ParticleTypes.SPLASH, x, hook.getY(), hook.getZ(), 6 + hook.getRandom().nextInt(4), 0.0, 0.2, 0, 0);
    }
}
