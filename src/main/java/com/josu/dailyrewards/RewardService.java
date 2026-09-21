package com.josu.dailyrewards;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.time.LocalDate;

public class RewardService {

    public enum Result { CLAIMED, ALREADY_CLAIMED, NO_REWARD }

    public static boolean hasClaimedToday(ServerPlayer player) {
        return LocalDate.now().toString().equals(player.getAttached(ModAttachments.LAST_CLAIM));
    }

    public static Result tryClaim(ServerPlayer player) {
        if (hasClaimedToday(player)) {
            return Result.ALREADY_CLAIMED;
        }

        LocalDate today = LocalDate.now();
        Reward reward = RewardsLoader.getRewardForDay(today.getDayOfWeek());
        if (reward == null || reward.items().isEmpty()) {
            return Result.NO_REWARD;
        }

        for (RewardItem rewardItem : reward.items()) {
            for (ItemStack stack : rewardItem.toStacks()) {
                // add() may only fit part of the stack; whatever is left goes to the ground.
                player.getInventory().add(stack);
                if (!stack.isEmpty()) {
                    player.drop(stack, false);
                }
            }
        }

        player.setAttached(ModAttachments.LAST_CLAIM, today.toString());
        return Result.CLAIMED;
    }
}
