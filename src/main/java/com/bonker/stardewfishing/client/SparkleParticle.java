package com.bonker.stardewfishing.client;

import com.bonker.stardewfishing.common.init.SFItems;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class SparkleParticle extends TextureSheetParticle {
    private static final Vector3f COLOR = new Vector3f(
            FastColor.ARGB32.red(SFItems.LEGENDARY_FISH_COLOR) / 255F,
            FastColor.ARGB32.green(SFItems.LEGENDARY_FISH_COLOR) / 255F,
            FastColor.ARGB32.blue(SFItems.LEGENDARY_FISH_COLOR) / 255F
    );
    private static final float SIZE = 0.1F;

    private final SpriteSet sprites;

    private SparkleParticle(ClientLevel pLevel, double pX, double pY, double pZ, SpriteSet sprites) {
        super(pLevel, pX, pY, pZ);
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
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            return new SparkleParticle(pLevel, pX, pY, pZ, sprite);
        }
    }
}
