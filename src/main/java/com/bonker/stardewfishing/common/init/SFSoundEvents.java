package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SFSoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, StardewFishing.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> CAST = registerSound("cast");
    public static final DeferredHolder<SoundEvent, SoundEvent> COMPLETE = registerSound("complete");
    public static final DeferredHolder<SoundEvent, SoundEvent> DWOP = registerSound("dwop");
    public static final DeferredHolder<SoundEvent, SoundEvent> DWOP_REVERSE = registerSound("dwop_reverse");
    public static final DeferredHolder<SoundEvent, SoundEvent> EQUIP = registerSound("equip");
    public static final DeferredHolder<SoundEvent, SoundEvent> UNEQUIP = registerSound("unequip");
    public static final DeferredHolder<SoundEvent, SoundEvent> FISH_ESCAPE = registerSound("fish_escape");
    public static final DeferredHolder<SoundEvent, SoundEvent> FISH_BITE = registerSound("fish_bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> FISH_HIT = registerSound("fish_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> PULL_ITEM = registerSound("pull_item");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEL_CREAK = registerSound("reel_creak");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEL_FAST = registerSound("reel_fast");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEL_SLOW = registerSound("reel_slow");
    public static final DeferredHolder<SoundEvent, SoundEvent> OPEN_CHEST = registerSound("open_chest");
    public static final DeferredHolder<SoundEvent, SoundEvent> OPEN_CHEST_GOLDEN = registerSound("open_chest_golden");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHEST_GET = registerSound("chest_get");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(StardewFishing.resource(name)));
    }
}
