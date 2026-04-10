package org.voximir.client_recipe_fix.client.injector;

import dev.architectury.event.events.client.ClientRecipeUpdateEvent;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.protocol.game.ClientboundRecipeBookAddPacket;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayEntry;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/**
 * Fires Architectury's ClientRecipeUpdateEvent.ADD so REI picks up our recipes.
 * Only loaded when Architectury is present (REI dependency).
 */
public class REIRecipeInjector {

    private static final Logger LOGGER = LoggerFactory.getLogger("Client Recipe Fix");

    public static void injectRecipes(RecipeAccess recipeAccess, List<RecipeHolder<?>> recipes) {
        List<ClientboundRecipeBookAddPacket.Entry> entries = new ArrayList<>();
        Object2IntOpenHashMap<String> groupIndices = new Object2IntOpenHashMap<>();
        int displayIndex = 0;

        for (RecipeHolder<?> holder : recipes) {
            var recipe = holder.value();

            // Compute group index (same logic as RecipeManager.unpackRecipeInfo)
            OptionalInt group;
            String groupStr = recipe.group();
            if (groupStr.isEmpty()) {
                group = OptionalInt.empty();
            } else {
                group = OptionalInt.of(groupIndices.computeIfAbsent(groupStr, k -> groupIndices.size()));
            }

            // Crafting requirements: non-special recipes list their ingredients
            Optional<List<Ingredient>> craftingRequirements;
            if (recipe.isSpecial()) {
                craftingRequirements = Optional.empty();
            } else {
                craftingRequirements = Optional.of(recipe.placementInfo().ingredients());
            }

            // Each recipe can have multiple displays
            for (RecipeDisplay display : recipe.display()) {
                RecipeDisplayId id = new RecipeDisplayId(displayIndex++);
                RecipeDisplayEntry displayEntry = new RecipeDisplayEntry(
                        id, display, group, recipe.recipeBookCategory(), craftingRequirements);
                entries.add(new ClientboundRecipeBookAddPacket.Entry(displayEntry, false, false));
            }
        }

        LOGGER.info("Firing Architectury ADD event with {} display entries for REI", entries.size());
        ClientRecipeUpdateEvent.ADD.invoker().add(recipeAccess, entries);
    }
}
