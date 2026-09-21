package com.josu.dailyrewards;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;

public class ModAttachments {

    // Stores the last claim date (e.g. "2026-09-17") on each player.
    // Persists with the world and is copied on death so respawning doesn't reset the claim.
    public static final AttachmentType<String> LAST_CLAIM =
            AttachmentRegistry.create(
                    DailyRewards.id("last_claim"),
                    builder -> builder.persistent(Codec.STRING).copyOnDeath()
            );

    // Called from the mod initializer so the class loads and registers the attachment.
    public static void init() {
    }
}
