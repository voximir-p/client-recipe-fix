package org.voximir.client_recipe_fix.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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

            // Wrap in a RecipeMap and fire the Fabric API event that JEI listens for
            RecipeMap recipeMap = RecipeMap.create(recipes);
            ClientRecipeSynchronizedEvent.EVENT.invoker()
                    .onRecipesSynchronized(client, new RecipeMapSynchronizedRecipes(recipeMap));

            LOGGER.info("Fired recipe sync with {} recipes", recipes.size());
        } catch (Exception e) {
            LOGGER.error("Failed to inject recipes", e);
        }
    }

    private record RecipeMapSynchronizedRecipes(RecipeMap recipeMap) implements SynchronizedRecipes {
        @Override
        public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(
                RecipeType<T> type, I input, Level level) {
            return recipeMap.getRecipesFor(type, input, level);
        }

        @Override
        public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(
                RecipeType<T> type) {
            return recipeMap.byType(type);
        }

        @Override
        public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatch(
                RecipeType<T> type, I input, Level level) {
            return recipeMap.getRecipesFor(type, input, level).findFirst();
        }

        @Override
        public RecipeHolder<?> get(ResourceKey<Recipe<?>> key) {
            return recipeMap.byKey(key);
        }

        @Override
        public Collection<RecipeHolder<?>> recipes() {
            return recipeMap.values();
        }
    }
}
