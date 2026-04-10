package org.voximir.client_recipe_fix.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Client Recipe Fix"))
                    .setSavingRunnable(ClientRecipeFixConfig::saveConfig);

            ConfigCategory general = builder.getOrCreateCategory(Component.literal("General"));
            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            general.addEntry(entryBuilder.startIntSlider(
                            Component.literal("Injection Delay (ticks)"),
                            ClientRecipeFixConfig.injectionDelayTicks, 0, 100)
                    .setDefaultValue(ClientRecipeFixConfig.injectionDelayTicksDefault)
                    .setTooltip(Component.literal("Delay in ticks before injecting recipes after joining a server"))
                    .setSaveConsumer(newValue -> ClientRecipeFixConfig.injectionDelayTicks = newValue)
                    .build());

            return builder.build();
        };
    }
}
