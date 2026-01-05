package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.common.init.SFItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

public class SparkleParticle extends SingleQuadParticle {
    private static final Vector3f COLOR = new Vector3f(
            ARGB.red(SFItems.LEGENDARY_FISH_COLOR) / 255F,
            ARGB.green(SFItems.LEGENDARY_FISH_COLOR) / 255F,
            ARGB.blue(SFItems.LEGENDARY_FISH_COLOR) / 255F
    );
    private static final float SIZE = 0.1F;

    private final SpriteSet sprites;

    private SparkleParticle(ClientLevel pLevel, double pX, double pY, double pZ, SpriteSet sprites) {
        super(pLevel, pX, pY, pZ, sprites.first());
        this.sprites = sprites;
        quadSize = 0;
        setSpriteFromAge(sprites);
        setLifetime(120);
        setAlpha(0.8F);
        setColor(COLOR.x, COLOR.y, COLOR.z);
    }

    @Override
    public void tick() {
        super.tick();

        float x = age / (lifetime / 2F);
        if (x > 1) {
            x = 2 - x;
        }
        quadSize = Mth.sin((x * Mth.PI) / 2) * SIZE;

        setSpriteFromAge(sprites);
    }

    @Override
    protected @NotNull Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<@NotNull SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        @Override
        public @Nullable Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new SparkleParticle(level, x, y, z, sprite);
        }
    }
}
