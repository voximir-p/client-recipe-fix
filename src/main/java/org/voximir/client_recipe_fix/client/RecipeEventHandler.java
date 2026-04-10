package org.voximir.client_recipe_fix.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.voximir.client_recipe_fix.client.injector.RecipeInjector;

import static org.voximir.client_recipe_fix.client.ClientRecipeFix.LOGGER;
import static org.voximir.client_recipe_fix.client.ClientRecipeFix.jeiLoaded;
import static org.voximir.client_recipe_fix.client.ClientRecipeFix.reiLoaded;

/**
 * Loads vanilla recipes from the MC JAR and fires ClientRecipeSynchronizedEvent
 * so JEI/REI can pick them up. Delayed by a few ticks after joining to let
 * any real server sync finish first.
 */
public class RecipeEventHandler {
    private static int ticksUntilInjection = -1;

    public static void registerEvents() {
        if (!(jeiLoaded || reiLoaded)) {
            LOGGER.warn("Neither JEI nor REI detected! No injection will be performed.");
            return;
        }

        if (jeiLoaded) LOGGER.info("JEI detected");
        if (reiLoaded) LOGGER.info("REI detected");

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.info("Joined server, injecting recipes in {} ticks", ClientRecipeFixConfig.injectionDelayTicks);

            ticksUntilInjection = ClientRecipeFixConfig.injectionDelayTicks;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> ticksUntilInjection = -1);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (ticksUntilInjection > 0) {
                ticksUntilInjection--;
            } else if (ticksUntilInjection == 0) {
                ticksUntilInjection = -1;
                RecipeInjector.performInjection(mc);
            }
        });
    }
}
