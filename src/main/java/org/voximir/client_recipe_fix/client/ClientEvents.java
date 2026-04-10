package org.voximir.client_recipe_fix.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voximir.client_recipe_fix.client.injector.Injector;

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

    public static void registerEvents() {
        if (!(jeiLoaded || reiLoaded)) {
            LOGGER.warn("Neither JEI nor REI detected! No injection will be performed.");
            return;
        }

        if (jeiLoaded) LOGGER.info("JEI detected");
        if (reiLoaded) LOGGER.warn("REI detected! It is not supported in this version.");

        ClientPlayConnectionEvents.JOIN.register((_, _, _) -> {
            LOGGER.info("Joined server, injecting recipes in {} ticks", INJECTION_DELAY_TICKS);

            ticksUntilInjection = INJECTION_DELAY_TICKS;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> ticksUntilInjection = -1);

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (ticksUntilInjection > 0) {
                ticksUntilInjection--;
            } else if (ticksUntilInjection == 0) {
                ticksUntilInjection = -1;
                Injector.performInjection(mc);
            }
        });
    }
}
