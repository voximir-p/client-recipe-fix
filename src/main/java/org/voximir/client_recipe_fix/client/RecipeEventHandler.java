package org.voximir.client_recipe_fix.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.voximir.client_recipe_fix.client.injector.RecipeInjector;

import static org.voximir.client_recipe_fix.client.ClientRecipeFix.LOGGER;
import static org.voximir.client_recipe_fix.client.ClientRecipeFix.jeiLoaded;
import static org.voximir.client_recipe_fix.client.ClientRecipeFix.reiLoaded;

/**
 * Loads vanilla recipes from the MC JAR and fires ClientRecipeSynchronizedEvent
 * so JEI/REI can pick them up. Fires immediately on JOIN to beat JEI's initialization.
 */
public class RecipeEventHandler {
    public static void registerEvents() {
        if (!(jeiLoaded || reiLoaded)) {
            LOGGER.warn("Neither JEI nor REI detected! No injection will be performed.");
            return;
        }

        if (jeiLoaded) LOGGER.info("JEI detected");
        if (reiLoaded) LOGGER.info("REI detected");

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.info("Joined server, injecting recipes immediately");
            RecipeInjector.performInjection(client);
        });
    }
}
