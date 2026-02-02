package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLGuiGraphics;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements VLGuiGraphics {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Override
	public Minecraft vl$mc() {
		return minecraft;
	}

	@Inject(method = "renderItemCount", at = @At("HEAD"), cancellable = true)
	private void vl$drawSize(Font font, ItemStack stack, int x, int y, @Nullable String text, CallbackInfo ci) {
		if (ClientGameEngine.INSTANCE.drawItemStackSize(vl$self(), stack, font, text, x, y)) {
			ci.cancel();
		}
	}
}
