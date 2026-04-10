package org.voximir.client_recipe_fix.client.injector;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voximir.client_recipe_fix.client.VanillaRecipeLoader;

import java.util.List;

import static org.voximir.client_recipe_fix.client.RecipeEventHandler.jeiSupport;
import static org.voximir.client_recipe_fix.client.ClientRecipeFix.jeiLoaded;
import static org.voximir.client_recipe_fix.client.ClientRecipeFix.reiLoaded;

public class RecipeInjector {
    private static final Logger LOGGER = LoggerFactory.getLogger("Client Recipe Fix");

    public static void performInjection(Minecraft client) {
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
            if (jeiLoaded && jeiSupport) JEIRecipeInjector.injectRecipes(client, recipes);

            // Fire Architectury event for REI (only if REI is present)
            if (reiLoaded) REIRecipeInjector.injectRecipes(connection.recipes(), recipes);

        } catch (Exception e) {
            LOGGER.error("Failed to inject recipes", e);
        }
    }
}
