package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLGuiGraphics;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public class GuiGraphicsMixin implements VLGuiGraphics {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Shadow
	@Final
	private Matrix3x2fStack pose;

	@Unique
	private int vl$guiShifted;

	@Unique
	private int vl$originalGuiWidth;

	@Override
	public Minecraft vl$mc() {
		return minecraft;
	}

	@Override
	public MultiBufferSource.BufferSource vl$buffers() {
		return minecraft.renderBuffers().bufferSource();
	}

	/**
	 * @author Lat
	 * @reason MBG
	 */
	@Overwrite
	public int guiWidth() {
		return MiscClientUtils.adjustScreenWidth(minecraft, vl$guiShifted > 0);
	}

	@Inject(method = "itemCount", at = @At("HEAD"), cancellable = true)
	private void vl$drawSize(Font font, ItemStack stack, int x, int y, @Nullable String text, CallbackInfo ci) {
		if (ClientGameEngine.INSTANCE.drawItemStackSize(vl$self(), stack, font, text, x, y)) {
			ci.cancel();
		}
	}

	@Override
	public void vl$pushShift() {
		if (vl$guiShifted == 0) {
			int adjustedWidth = MiscClientUtils.adjustScreenWidth(minecraft, true);
			pose.pushMatrix();
			pose.translate(MiscClientUtils.adjustScreenX(minecraft, adjustedWidth), 0F);

			if (minecraft.screen != null) {
				vl$originalGuiWidth = minecraft.screen.width;
				minecraft.screen.width = adjustedWidth;
			}
		}

		vl$guiShifted++;
	}

	@Override
	public void vl$popShift() {
		vl$guiShifted--;

		if (vl$guiShifted == 0) {
			pose.popMatrix();

			if (minecraft.screen != null) {
				minecraft.screen.width = vl$originalGuiWidth;
			}
		}
	}
}
