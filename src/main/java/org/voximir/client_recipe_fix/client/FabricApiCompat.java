package org.voximir.client_recipe_fix.client;

public class FabricApiCompat {
    public static final boolean HAS_RECIPE_SYNC;

    static {
        boolean found;
        try {
            Class.forName("net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes");
            found = true;
        } catch (ClassNotFoundException e) {
            found = false;
        }
        HAS_RECIPE_SYNC = found;
    }
}
