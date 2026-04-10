package org.voximir.client_recipe_fix.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.mojang.text2speech.Narrator.LOGGER;

public class ClientRecipeFixConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("client_recipe_fix.json");

    // ── Config fields ────────────────────────────────────────────────────────
    public static int injectionDelayTicksDefault = 5;
    public static int injectionDelayTicks = injectionDelayTicksDefault;

    // ── Serializable data holder ─────────────────────────────────────────────
    private static class Data {
        int injectionDelayTicks = injectionDelayTicksDefault;
    }

    /**
     * Loads config from disk. If the file is missing or unreadable,
     * defaults are kept and a fresh file is written.
     */
    public static void loadConfig() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                Data data = GSON.fromJson(json, Data.class);
                if (data != null) {
                    injectionDelayTicks = data.injectionDelayTicks;
                    LOGGER.info("Config loaded (injectionDelayTicks={})", injectionDelayTicks);
                    return;
                }
            } catch (Exception e) {
                LOGGER.warn("Failed to read config, using defaults", e);
            }
        }

        // First launch or broken file → write defaults
        saveConfig();
    }

    /**
     * Persists the current config values to disk.
     */
    public static void saveConfig() {
        Data data = new Data();
        data.injectionDelayTicks = injectionDelayTicks;

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(data));
            LOGGER.info("Config saved");
        } catch (IOException e) {
            LOGGER.error("Failed to save config", e);
        }
    }
}
