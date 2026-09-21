package com.josu.dailyrewards;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record RewardItem(Item item, int count) {

    public List<ItemStack> toStacks() {
        List<ItemStack> stacks = new ArrayList<>();
        int remaining = count;
        int maxStack = new ItemStack(item).getMaxStackSize();
        while (remaining > 0) {
            int size = Math.min(remaining, maxStack);
            stacks.add(new ItemStack(item, size));
            remaining -= size;
        }
        return stacks;
    }
}
