package org.voximir.client_recipe_fix.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientRecipeFixClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Client Recipe Fix");

    public static final boolean jeiLoaded = FabricLoader.getInstance().isModLoaded("jei");
    public static final boolean reiLoaded = FabricLoader.getInstance().isModLoaded("roughlyenoughitems");

    @Override
    public void onInitializeClient() {
        ModConfig.loadConfig();
        ClientEvents.registerEvents();

        LOGGER.info("Initialized");
    }
}
