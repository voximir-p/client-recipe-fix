package org.voximir.client_recipe_fix.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class VanillaRecipeLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("client_recipe_fix");

    /**
     * Reads all vanilla recipe JSONs from the minecraft-common JAR and deserializes them.
     */
    public static List<RecipeHolder<?>> loadVanillaRecipes(HolderLookup.Provider registries) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();

        try {
            // Locate the JAR containing RecipeManager (minecraft-common), which has data/minecraft/recipe/
            URI jarUri = RecipeManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path jarPath = Path.of(jarUri);

            LOGGER.info("[Client Recipe Fix] Loading recipes from: {}", jarPath);

            if (Files.isDirectory(jarPath)) {
                // Dev env - exploded classes
                Path recipeDir = jarPath.resolve("data/minecraft/recipe");
                if (Files.isDirectory(recipeDir)) {
                    loadFromDirectory(recipeDir, registries, recipes);
                } else {
                    LOGGER.warn("[Client Recipe Fix] Recipe dir not found at {}", recipeDir);
                }
            } else {
                // Production - inside JAR
                try (FileSystem fs = FileSystems.newFileSystem(jarPath)) {
                    Path recipeDir = fs.getPath("data/minecraft/recipe");
                    if (Files.isDirectory(recipeDir)) {
                        loadFromDirectory(recipeDir, registries, recipes);
                    } else {
                        LOGGER.warn("[Client Recipe Fix] Recipe dir not found in JAR");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[Client Recipe Fix] Failed to locate recipe JAR", e);
        }

        LOGGER.info("[Client Recipe Fix] Loaded {} vanilla recipes", recipes.size());
        return recipes;
    }

    private static void loadFromDirectory(Path recipeDir, HolderLookup.Provider registries,
                                          List<RecipeHolder<?>> recipes) {
        try (Stream<Path> paths = Files.walk(recipeDir)) {
            List<Path> jsonFiles = paths.filter(p -> p.toString().endsWith(".json")).toList();
            LOGGER.info("[Client Recipe Fix] Found {} recipe files", jsonFiles.size());

            for (Path jsonFile : jsonFiles) {
                String relativePath = recipeDir.relativize(jsonFile).toString();
                String recipeIdPath = relativePath.substring(0, relativePath.length() - 5)
                        .replace('\\', '/');
                Identifier recipeId = Identifier.fromNamespaceAndPath("minecraft", recipeIdPath);

                try {
                    ResourceKey<Recipe<?>> key = ResourceKey.create(Registries.RECIPE, recipeId);
                    try (InputStream is = Files.newInputStream(jsonFile);
                         InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
                        Recipe<?> recipe = Recipe.CODEC.parse(ops, json).getOrThrow();
                        RecipeHolder<?> holder = new RecipeHolder<>(key, recipe);
                        recipes.add(holder);
                    }
                } catch (Exception e) {
                    LOGGER.warn("[Client Recipe Fix] Failed to parse recipe {}: {}", recipeId, e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[Client Recipe Fix] Failed to walk recipe directory", e);
        }
    }
}
