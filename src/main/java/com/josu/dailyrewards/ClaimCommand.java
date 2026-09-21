package com.josu.dailyrewards;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ClaimCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("claimreward").executes(context -> claim(context.getSource())));
        dispatcher.register(Commands.literal("rewards")
                .executes(context -> openMenu(context.getSource()))
                .then(Commands.literal("reload")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> reload(context.getSource()))));
    }

    private static int reload(CommandSourceStack source) {
        if (!RewardsLoader.load()) {
            source.sendFailure(Component.literal("Failed to reload rewards.json, check the server log. Keeping the previous rewards."));
            return 0;
        }
        int count = RewardsLoader.getRewardCount();
        source.sendSuccess(() -> Component.literal("Rewards reloaded (" + count + " entries)."), true);
        return 1;
    }

    private static int openMenu(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Only players can open the rewards menu."));
            return 0;
        }
        RewardsMenu.openMain(player);
        return 1;
    }

    private static int claim(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Only players can claim rewards."));
            return 0;
        }

        switch (RewardService.tryClaim(player)) {
            case CLAIMED -> {
                source.sendSuccess(() -> Component.literal("Reward Claimed!"), false);
                return 1;
            }
            case ALREADY_CLAIMED -> source.sendFailure(Component.literal("You've already claimed a reward today. Come back tomorrow!"));
            case NO_REWARD -> source.sendFailure(Component.literal("No rewards are configured."));
        }
        return 0;
    }
}
