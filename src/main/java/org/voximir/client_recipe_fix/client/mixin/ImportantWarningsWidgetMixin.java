package org.voximir.client_recipe_fix.client.mixin;

import me.shedaniel.rei.impl.client.gui.hints.ImportantWarningsWidget;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ImportantWarningsWidget.class)
public class ImportantWarningsWidgetMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void cancelRender(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        ci.cancel();
    }
}
