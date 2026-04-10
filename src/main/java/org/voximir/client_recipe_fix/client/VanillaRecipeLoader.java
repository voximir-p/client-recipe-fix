package org.voximir.client_recipe_fix.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import org.jspecify.annotations.NonNull;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static org.voximir.client_recipe_fix.client.ClientRecipeFix.LOGGER;

public class VanillaRecipeLoader {
    /**
     * Reads all vanilla recipe JSONs from the minecraft-common JAR and deserializes them.
     */
    public static List<RecipeHolder<?>> loadVanillaRecipes(HolderLookup.Provider registries) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();

        try {
            // Locate the JAR containing RecipeManager (minecraft-common), which has data/minecraft/recipe/
            URI jarUri = RecipeManager.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path jarPath = Path.of(jarUri);

            LOGGER.info("Loading recipes from: {}", jarPath);

            if (Files.isDirectory(jarPath)) {
                // Dev env - exploded classes
                Map<String, List<String>> itemTags = loadItemTags(jarPath.resolve("data/minecraft/tags/item"));
                Path recipeDir = jarPath.resolve("data/minecraft/recipe");
                if (Files.isDirectory(recipeDir)) {
                    loadFromDirectory(recipeDir, registries, recipes, itemTags);
                } else {
                    LOGGER.warn("Recipe dir not found at {}", recipeDir);
                }
            } else {
                // Production - inside JAR
                try (FileSystem fs = FileSystems.newFileSystem(jarPath)) {
                    Map<String, List<String>> itemTags = loadItemTags(fs.getPath("data/minecraft/tags/item"));
                    Path recipeDir = fs.getPath("data/minecraft/recipe");
                    if (Files.isDirectory(recipeDir)) {
                        loadFromDirectory(recipeDir, registries, recipes, itemTags);
                    } else {
                        LOGGER.warn("Recipe dir not found in JAR");
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to locate recipe JAR", e);
        }

        LOGGER.info("Loaded {} vanilla recipes", recipes.size());
        return recipes;
    }

    private static void loadFromDirectory(Path recipeDir, HolderLookup.Provider registries,
                                          List<RecipeHolder<?>> recipes,
                                          Map<String, List<String>> itemTags) {
        try (Stream<Path> paths = Files.walk(recipeDir)) {
            List<Path> jsonFiles = paths.filter(p -> p.toString().endsWith(".json")).toList();
            LOGGER.info("Found {} recipe files", jsonFiles.size());

            var ops = registries.createSerializationContext(JsonOps.INSTANCE);

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
                        expandTagReferences(json, itemTags);
                        Recipe<?> recipe = Recipe.CODEC.parse(ops, json).getOrThrow();
                        RecipeHolder<?> holder = new RecipeHolder<>(key, recipe);
                        recipes.add(holder);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to parse recipe {}: {}", recipeId, e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to walk recipe directory", e);
        }
    }

    // ── Tag loading ──────────────────────────────────────────────────────────

    /**
     * Loads and resolves all item tag definitions from the given directory
     * (e.g. {@code data/minecraft/tags/item/} inside the Minecraft JAR).
     * Nested tag references ({@code #minecraft:other_tag}) are resolved recursively.
     *
     * @return map from fully-qualified tag name (e.g. {@code minecraft:planks})
     *         to a list of concrete item identifiers (e.g. {@code minecraft:oak_planks})
     */
    private static Map<String, List<String>> loadItemTags(Path tagDir) {
        Map<String, List<String>> rawTags = new HashMap<>();

        if (!Files.isDirectory(tagDir)) {
            LOGGER.warn("Item tag dir not found at {}", tagDir);
            return Collections.emptyMap();
        }

        try (Stream<Path> paths = Files.walk(tagDir)) {
            List<Path> tagFiles = paths.filter(p -> p.toString().endsWith(".json")).toList();

            for (Path tagFile : tagFiles) {
                String relativePath = tagDir.relativize(tagFile).toString();
                String tagName = "minecraft:" + relativePath
                        .substring(0, relativePath.length() - 5)
                        .replace('\\', '/');

                try (InputStream is = Files.newInputStream(tagFile);
                     InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    JsonArray values = json.getAsJsonArray("values");
                    List<String> entries = getStrings(values);

                    rawTags.put(tagName, entries);
                } catch (Exception e) {
                    LOGGER.warn("Failed to load item tag {}: {}", tagName, e.getMessage());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to walk item tag directory", e);
        }

        // Resolve nested tag references (e.g. #minecraft:planks → concrete item list)
        Map<String, List<String>> resolved = new HashMap<>();
        for (String tagName : rawTags.keySet()) {
            resolveTag(tagName, rawTags, resolved, new HashSet<>());
        }

        LOGGER.info("Loaded {} item tags", resolved.size());
        return resolved;
    }

    private static @NonNull List<String> getStrings(JsonArray values) {
        List<String> entries = new ArrayList<>();

        for (JsonElement entry : values) {
            if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
                entries.add(entry.getAsString());
            } else if (entry.isJsonObject()) {
                // Optional entry format: {"id": "...", "required": false}
                JsonObject entryObj = entry.getAsJsonObject();
                if (entryObj.has("id")) {
                    entries.add(entryObj.get("id").getAsString());
                }
            }
        }
        return entries;
    }

    /**
     * Recursively resolves a tag to a flat list of concrete item identifiers.
     */
    private static List<String> resolveTag(String tagName,
                                           Map<String, List<String>> rawTags,
                                           Map<String, List<String>> resolvedCache,
                                           Set<String> resolving) {
        if (resolvedCache.containsKey(tagName)) {
            return resolvedCache.get(tagName);
        }
        if (resolving.contains(tagName)) {
            LOGGER.warn("Circular tag reference detected for {}", tagName);
            return Collections.emptyList();
        }

        resolving.add(tagName);
        List<String> raw = rawTags.get(tagName);
        if (raw == null) {
            resolving.remove(tagName);
            return Collections.emptyList();
        }

        List<String> resolved = new ArrayList<>();
        for (String entry : raw) {
            if (entry.startsWith("#")) {
                String nestedTag = entry.substring(1);
                resolved.addAll(resolveTag(nestedTag, rawTags, resolvedCache, resolving));
            } else {
                resolved.add(entry);
            }
        }

        resolving.remove(tagName);
        resolvedCache.put(tagName, resolved);
        return resolved;
    }

    // ── JSON pre-processing ──────────────────────────────────────────────────

    /**
     * Walks the JSON tree and replaces tag-reference strings (e.g.
     * {@code "#minecraft:shulker_boxes"}) with a JSON array of the concrete
     * item identifiers that belong to that tag.  This allows the recipe codec
     * to parse tag-based ingredients without requiring bound tags in the
     * registry.
     */
    private static void expandTagReferences(JsonObject obj, Map<String, List<String>> itemTags) {
        for (String key : new ArrayList<>(obj.keySet())) {
            JsonElement value = obj.get(key);
            if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                String str = value.getAsString();
                // Tag references look like "#minecraft:planks"; pattern chars like "#" don't contain ':'
                if (str.startsWith("#") && str.contains(":")) {
                    String tagName = str.substring(1);
                    List<String> items = itemTags.get(tagName);
                    if (items != null && !items.isEmpty()) {
                        JsonArray array = new JsonArray();
                        for (String item : items) {
                            array.add(item);
                        }
                        obj.add(key, array);
                    }
                }
            } else if (value.isJsonArray()) {
                expandTagReferencesInArray(value.getAsJsonArray(), itemTags);
            } else if (value.isJsonObject()) {
                expandTagReferences(value.getAsJsonObject(), itemTags);
            }
        }
    }

    private static void expandTagReferencesInArray(JsonArray array, Map<String, List<String>> itemTags) {
        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                String str = element.getAsString();
                if (str.startsWith("#") && str.contains(":")) {
                    String tagName = str.substring(1);
                    List<String> items = itemTags.get(tagName);
                    if (items != null && !items.isEmpty()) {
                        JsonArray replacement = new JsonArray();
                        for (String item : items) {
                            replacement.add(item);
                        }
                        array.set(i, replacement);
                    }
                }
            } else if (element.isJsonArray()) {
                expandTagReferencesInArray(element.getAsJsonArray(), itemTags);
            } else if (element.isJsonObject()) {
                expandTagReferences(element.getAsJsonObject(), itemTags);
            }
        }
    }
}
