package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SFParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, StardewFishing.MODID);

    public static final Supplier<SimpleParticleType> SPARKLE = PARTICLE_TYPES.register("sparkle",
            () -> new SimpleParticleType(false));
}
