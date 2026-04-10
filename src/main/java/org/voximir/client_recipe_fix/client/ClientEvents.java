package org.voximir.client_recipe_fix.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.voximir.client_recipe_fix.client.ClientRecipeFixClient.jeiLoaded;
import static org.voximir.client_recipe_fix.client.ClientRecipeFixClient.reiLoaded;

/**
 * Loads vanilla recipes from the MC JAR and fires ClientRecipeSynchronizedEvent
 * so JEI/REI can pick them up. Delayed by a few ticks after joining to let
 * any real server sync finish first.
 */
public class ClientEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger("Client Recipe Fix");

    private static final int INJECTION_DELAY_TICKS = 5;
    private static int ticksUntilInjection = -1;

    private static boolean jeiSupport = true;

    public static void registerEvents() {
        if (!(jeiLoaded || reiLoaded)) {
            LOGGER.warn("Neither JEI nor REI detected! No injection will be performed.");
            return;
        }

        if (jeiLoaded) LOGGER.info("JEI detected");
        if (reiLoaded) LOGGER.info("REI detected");

        String jei_msg = """
                JEI detected!
                Full recipe sync with JEI requires Minecraft 1.21.10 or newer.
                On versions 1.21.9 and below, this is limited by the Fabric API.
                For full support on older versions, consider using REI instead.
                """;

        if (!Compat.HAS_RECIPE_SYNC && jeiLoaded) {
            jeiSupport = false;
            LOGGER.warn(jei_msg.replaceAll("\\s+", " "));
        }

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.info("Joined server, injecting recipes in {} ticks", INJECTION_DELAY_TICKS);

            if (!jeiSupport && client.player != null) {
                client.player.displayClientMessage(Component.literal("[Client Recipe Fix] " + jei_msg), false);
            }

            ticksUntilInjection = INJECTION_DELAY_TICKS;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ticksUntilInjection = -1);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (ticksUntilInjection > 0) {
                ticksUntilInjection--;
            } else if (ticksUntilInjection == 0) {
                ticksUntilInjection = -1;
                performInjection(mc);
            }
        });
    }

    private static void performInjection(Minecraft client) {
        try {
            ClientPacketListener connection = client.getConnection();
            if (connection == null) {
                LOGGER.warn("No connection, skipping");
                return;
            }

            List<RecipeHolder<?>> recipes = VanillaRecipeLoader.loadVanillaRecipes(connection.registryAccess());
            if (recipes.isEmpty()) {
                LOGGER.warn("No recipes loaded");
                return;
            }

            // Inject into JEI (only if JEI is present and supported)
            if (jeiLoaded && jeiSupport) JEIInjector.injectRecipes(client, recipes);

            // Fire Architectury event for REI (only if REI is present)
            if (reiLoaded) REIInjector.injectRecipes(connection.recipes(), recipes);

        } catch (Exception e) {
            LOGGER.error("Failed to inject recipes", e);
        }
    }
}
