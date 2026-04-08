package org.voximir.client_recipe_fix.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Loads vanilla recipes from the MC JAR and fires ClientRecipeSynchronizedEvent
 * so JEI/REI can pick them up. Delayed by a few ticks after joining to let
 * any real server sync finish first.
 */
public class ClientRecipeFixClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("Client Recipe Fix");
    private static final int INJECTION_DELAY_TICKS = 20;

    private int ticksUntilInjection = -1;

    @Override
    public void onInitializeClient() {
        LOGGER.info("Initialized");

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.info("Joined server, injecting recipes in {} ticks", INJECTION_DELAY_TICKS);
            ticksUntilInjection = INJECTION_DELAY_TICKS;
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ticksUntilInjection = -1;
        });

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (ticksUntilInjection > 0) {
                ticksUntilInjection--;
            } else if (ticksUntilInjection == 0) {
                ticksUntilInjection = -1;
                performInjection(mc);
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            boolean jeiLoaded = FabricLoader.getInstance().isModLoaded("jei");

            if (jeiLoaded && client.player != null) {
                client.player.displayClientMessage(
                        Component.literal(
                                """
                                        [Client Recipe Fix] §cJEI is detected!§r
                                        Full recipe sync with JEI requires Minecraft 1.21.10 or newer.
                                        On versions 1.21.9 and below, this is limited by the Fabric API.
                                        For full support on older versions, consider using REI instead.
                                        """
                        ),
                        false
                );
            }

            ticksUntilInjection = INJECTION_DELAY_TICKS;
        });
    }

    private void performInjection(Minecraft client) {
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

            // Fire Architectury event for REI (only if Architectury is present)
            try {
                REICompat.fireRecipeAddEvent(connection.recipes(), recipes);
            } catch (NoClassDefFoundError ignored) {
                // Architectury/REI not installed, skip
            }
        } catch (Exception e) {
            LOGGER.error("Failed to inject recipes", e);
        }
    }
}
