package com.bonker.stardewfishing.common.init;

import com.bonker.stardewfishing.StardewFishing;
import com.bonker.stardewfishing.server.data.FishingHookAttachment;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class SFAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, StardewFishing.MODID);

    public static final Supplier<AttachmentType<FishingHookAttachment>> HOOK = ATTACHMENT_TYPES.register(
            "hook", () -> AttachmentType.builder(FishingHookAttachment::new).build());
}
