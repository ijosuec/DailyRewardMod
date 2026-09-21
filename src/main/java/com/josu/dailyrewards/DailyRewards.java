package com.josu.dailyrewards;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DailyRewards implements ModInitializer {
	public static final String MOD_ID = "dailyrewards";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModAttachments.init();
		RewardsLoader.load();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			ClaimCommand.register(dispatcher);
		});
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}