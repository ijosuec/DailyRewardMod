package com.josu.dailyrewards;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

//Server-side chest claim menu
public class RewardsMenu {

    private static final int[] DAY_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int BACK_SLOT = 18;
    private static final int CLAIM_SLOT = 22;

    public static void openMain(ServerPlayer player) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal("Daily Rewards"));
        fillBackground(gui);

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        for (DayOfWeek day : DayOfWeek.values()) {
            gui.setSlot(DAY_SLOTS[day.ordinal()], dayPaper(player, day, today)
                    .setCallback((index, type, action) -> openDetail(player, day)));
        }
        gui.open();
    }

    public static void openDetail(ServerPlayer player, DayOfWeek day) {
        SimpleGui gui = new SimpleGui(MenuType.GENERIC_9x3, player, false);
        gui.setTitle(Component.literal(dayName(day) + " rewards"));
        fillBackground(gui);

        DayOfWeek today = LocalDate.now().getDayOfWeek();
        gui.setSlot(4, dayPaper(player, day, today));

        Reward reward = RewardsLoader.getRewardForDay(day);
        List<RewardItem> items = reward == null ? List.of() : reward.items();
        int start = 9 + (9 - Math.min(items.size(), 9)) / 2;
        for (int i = 0; i < Math.min(items.size(), 9); i++) {
            RewardItem entry = items.get(i);
            gui.setSlot(start + i, new GuiElementBuilder(entry.item(), Math.min(entry.count(), 64)));
        }

        gui.setSlot(BACK_SLOT, new GuiElementBuilder(Items.POPPY)
                .setName(text("Back", ChatFormatting.RED))
                .setCallback((index, type, action) -> openMain(player)));

        if (day == today) {
            if (RewardService.hasClaimedToday(player)) {
                gui.setSlot(CLAIM_SLOT, new GuiElementBuilder(Items.BARRIER)
                        .setName(text("Already claimed today", ChatFormatting.RED)));
            } else {
                gui.setSlot(CLAIM_SLOT, new GuiElementBuilder(Items.EMERALD)
                        .setName(text("Claim reward", ChatFormatting.GREEN))
                        .glow()
                        .setCallback((index, type, action) -> {
                            claim(player);
                            openDetail(player, day);
                        }));
            }
        }
        gui.open();
    }

    private static void claim(ServerPlayer player) {
        switch (RewardService.tryClaim(player)) {
            case CLAIMED -> player.displayClientMessage(text("Reward claimed!", ChatFormatting.GREEN), true);
            case ALREADY_CLAIMED -> player.displayClientMessage(text("You already claimed today's reward.", ChatFormatting.RED), true);
            case NO_REWARD -> player.displayClientMessage(text("No rewards are configured.", ChatFormatting.RED), true);
        }
    }

    private static GuiElementBuilder dayPaper(ServerPlayer player, DayOfWeek day, DayOfWeek today) {
        boolean isToday = day == today;
        boolean claimable = isToday && !RewardService.hasClaimedToday(player);

        GuiElementBuilder builder = new GuiElementBuilder(Items.PAPER)
                .setName(text(dayName(day), isToday ? ChatFormatting.GOLD : ChatFormatting.WHITE));
        if (claimable) {
            builder.glow();
        }

        Reward reward = RewardsLoader.getRewardForDay(day);
        if (reward != null) {
            for (RewardItem entry : reward.items()) {
                builder.addLoreLine(text("• " + entry.count() + "x ", ChatFormatting.GRAY)
                        .append(entry.item().getDescription()));
            }
        }

        builder.addLoreLine(Component.empty());
        if (isToday) {
            builder.addLoreLine(claimable
                    ? text("Today - click to open", ChatFormatting.YELLOW)
                    : text("Today - already claimed", ChatFormatting.GREEN));
        } else {
            builder.addLoreLine(text("Click to view", ChatFormatting.DARK_GRAY));
        }
        return builder;
    }

    private static void fillBackground(SimpleGui gui) {
        for (int i = 0; i < gui.getSize(); i++) {
            gui.setSlot(i, new GuiElementBuilder(Items.BLACK_STAINED_GLASS_PANE).setName(Component.empty()));
        }
    }

    private static String dayName(DayOfWeek day) {
        return day.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private static MutableComponent text(String value, ChatFormatting color) {
        return Component.literal(value).withStyle(style -> style.withItalic(false).withColor(color));
    }
}
