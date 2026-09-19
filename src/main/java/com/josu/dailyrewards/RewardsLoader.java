package com.josu.dailyrewards;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class RewardsLoader {

    private static final List<Reward> REWARDS = new ArrayList<>();

    public static void load() {
        Gson gson = new Gson();
        String path = "/data/dailyrewards/rewards.json";

        try (InputStream stream = RewardsLoader.class.getResourceAsStream(path)) {
            if(stream == null) {
                DailyRewards.LOGGER.error("Could not find rewards.json");
                return;
            }

            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            JsonObject root = gson.fromJson(reader, JsonObject.class);

            root.getAsJsonArray("rewards").forEach(element -> {
                Reward reward = gson.fromJson(element, Reward.class);
                REWARDS.add(reward);
            });
            DailyRewards.LOGGER.info("Loader {} rewards.", REWARDS.size());
        } catch (Exception e) {
            DailyRewards.LOGGER.error("Failed to load rewards.json", e);
        }
    }

    public static  Reward getRewardForDay(int dayIndex) {
        if (REWARDS.isEmpty()) {
            return null;
        }
        return REWARDS.get(dayIndex % REWARDS.size());
    }
}
