package com.josu.dailyrewards;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.time.LocalDate;

public class ClaimCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("claimreward")
                        .executes(context -> claim(context.getSource()))
        );
    }

    private static int claim(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Only players can claim rewards."));
            return 0;
        }

        String today = LocalDate.now().toString();
        String lastClaim = player.getAttached(ModAttachments.LAST_CLAIM);

        if (today.equals(lastClaim)) {
            source.sendFailure(Component.literal("You've already claimed a reward today. Come back tomorrow!"));
            return 0;
        }

        int dayIndex = LocalDate.now().getDayOfWeek().getValue() - 1;
        Reward reward = RewardsLoader.getRewardForDay(dayIndex);

        if (reward == null) {
            source.sendFailure(Component.literal("No rewards are configured."));
            return 0;
        }

        ResourceLocation itemId = ResourceLocation.parse(reward.item);
        Item item = BuiltInRegistries.ITEM.get(itemId);
        ItemStack stack = new ItemStack(item, reward.count);

        boolean added = player.getInventory().add(stack);
        if (!added) {
            player.drop(stack, false);
        }

        player.setAttached(ModAttachments.LAST_CLAIM, today);

        source.sendSuccess(() -> Component.literal("Reward Claimed!"), false);
        return 1;
    }
}