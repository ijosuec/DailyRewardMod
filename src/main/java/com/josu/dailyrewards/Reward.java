package com.josu.dailyrewards;

import java.util.List;

/** Everything a player receives on one day of the week. */
public record Reward(List<RewardItem> items) {
}
