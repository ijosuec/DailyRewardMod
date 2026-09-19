package com.josu.dailyrewards;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.resources.ResourceLocation;

public class ModAttachments {

    // Stores the last claim date (e.g. "2026-09-17") on each player.
    // It persists automatically when the world is saved.
    public static final AttachmentType<String> LAST_CLAIM =
            AttachmentRegistry.createPersistent(
                    ResourceLocation.fromNamespaceAndPath(DailyRewards.MOD_ID, "last_claim"),
                    com.mojang.serialization.Codec.STRING
            );

    // Called from the mod initializer so the class loads and registers the attachment.
    public static void init() {
    }
}