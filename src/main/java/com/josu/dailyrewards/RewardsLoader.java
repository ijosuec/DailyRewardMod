package com.josu.dailyrewards;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RewardsLoader {

    private static final Gson GSON = new Gson();
    private static final String DEFAULT_RESOURCE = "/data/dailyrewards/rewards.json";

    private static volatile List<Reward> rewards = List.of();


    public static boolean load() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve(DailyRewards.MOD_ID).resolve("rewards.json");

        try {
            if (Files.notExists(file)) {
                Files.createDirectories(file.getParent());
                try (InputStream defaults = RewardsLoader.class.getResourceAsStream(DEFAULT_RESOURCE)) {
                    if (defaults == null) {
                        DailyRewards.LOGGER.error("Bundled default rewards.json is missing");
                        return false;
                    }
                    Files.copy(defaults, file, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                JsonObject root = GSON.fromJson(reader, JsonObject.class);
                List<Reward> loaded = new ArrayList<>();
                int day = 1;
                for (JsonElement element : root.getAsJsonArray("rewards")) {
                    loaded.add(parseReward(element.getAsJsonObject(), day++));
                }
                rewards = List.copyOf(loaded);
                DailyRewards.LOGGER.info("Loaded {} rewards from {}.", rewards.size(), file);
                return true;
            }
        } catch (Exception e) {
            DailyRewards.LOGGER.error("Failed to load {}", file, e);
            return false;
        }
    }

    public static int getRewardCount() {
        return rewards.size();
    }

    private static Reward parseReward(JsonObject json, int day) {
        List<RewardItem> items = new ArrayList<>();
        for (JsonElement element : json.getAsJsonArray("items")) {
            JsonObject entry = element.getAsJsonObject();
            String id = entry.get("item").getAsString();
            int count = entry.has("count") ? entry.get("count").getAsInt() : 1;

            Optional<Item> item = Optional.ofNullable(ResourceLocation.tryParse(id))
                    .flatMap(BuiltInRegistries.ITEM::getOptional);
            if (item.isEmpty() || count < 1) {
                DailyRewards.LOGGER.warn("Reward #{}: skipping invalid entry '{}' x{}", day, id, count);
                continue;
            }
            items.add(new RewardItem(item.get(), count));
        }
        return new Reward(List.copyOf(items));
    }

    // Reward for a weekday, or null when nothing is configured
    public static Reward getRewardForDay(DayOfWeek day) {
        List<Reward> current = rewards;
        if (current.isEmpty()) {
            return null;
        }
        return current.get((day.getValue() - 1) % current.size());
    }
}
