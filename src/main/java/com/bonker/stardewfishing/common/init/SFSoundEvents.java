package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.SFConfig;
import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class SFSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, StardewFishing.MODID);

    public static final RegistryObject<SoundEvent> CAST = registerSound("cast");
    public static final RegistryObject<SoundEvent> COMPLETE = registerSound("complete");
    public static final RegistryObject<SoundEvent> DWOP = registerSound("dwop");
    public static final RegistryObject<SoundEvent> DWOP_REVERSE = registerSound("dwop_reverse");
    public static final RegistryObject<SoundEvent> DWOP_LOUD = registerSound("dwop_loud");
    public static final RegistryObject<SoundEvent> DWOP_REVERSE_LOUD = registerSound("dwop_reverse_loud");
    public static final RegistryObject<SoundEvent> EQUIP = registerSound("equip");
    public static final RegistryObject<SoundEvent> UNEQUIP = registerSound("unequip");
    public static final RegistryObject<SoundEvent> FISH_ESCAPE = registerSound("fish_escape");
    public static final RegistryObject<SoundEvent> FISH_BITE = registerSound("fish_bite");
    public static final RegistryObject<SoundEvent> FISH_HIT = registerSound("fish_hit");
    public static final RegistryObject<SoundEvent> PULL_ITEM = registerSound("pull_item");
    public static final RegistryObject<SoundEvent> REEL_CREAK = registerSound("reel_creak");
    public static final RegistryObject<SoundEvent> REEL_FAST = registerSound("reel_fast");
    public static final RegistryObject<SoundEvent> REEL_SLOW = registerSound("reel_slow");
    public static final RegistryObject<SoundEvent> OPEN_CHEST = registerSound("open_chest");
    public static final RegistryObject<SoundEvent> OPEN_CHEST_GOLDEN = registerSound("open_chest_golden");
    public static final RegistryObject<SoundEvent> CHEST_GET = registerSound("chest_get");

    public static SoundEvent getDwop(boolean normalDwop) {
        boolean loud = SFConfig.isolateAudioCues();
        if (loud) {
            return (normalDwop ? DWOP_LOUD : DWOP_REVERSE_LOUD).get();
        } else {
            return (normalDwop ? DWOP : DWOP_REVERSE).get();
        }
    }

    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(StardewFishing.resource(name)));
    }
}
