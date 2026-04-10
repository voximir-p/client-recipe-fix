package org.voximir.client_recipe_fix.client.injector;

import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class JEIRecipeInjector {
    private static final Logger LOGGER = LoggerFactory.getLogger("Client Recipe Fix");

    public static void injectRecipes(Minecraft client, List<RecipeHolder<?>> recipes) {
        // Wrap in a RecipeMap and fire the Fabric API event that JEI listens for
        RecipeMap recipeMap = RecipeMap.create(recipes);
        ClientRecipeSynchronizedEvent.EVENT.invoker().onRecipesSynchronized(client, new RecipeMapSynchronizedRecipes(recipeMap));

        LOGGER.info("Fired recipe sync with {} recipes", recipes.size());
    }

    private record RecipeMapSynchronizedRecipes(RecipeMap recipeMap) implements SynchronizedRecipes {
        @Override
        public <I extends RecipeInput, T extends Recipe<I>> Stream<RecipeHolder<T>> getAllMatches(RecipeType<T> type, I input, Level level) {
            return recipeMap.getRecipesFor(type, input, level);
        }

        @Override
        public <I extends RecipeInput, T extends Recipe<I>> Collection<RecipeHolder<T>> getAllOfType(RecipeType<T> type) {
            return recipeMap.byType(type);
        }

        @Override
        public <I extends RecipeInput, T extends Recipe<I>> Optional<RecipeHolder<T>> getFirstMatch(RecipeType<T> type, I input, Level level) {
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
