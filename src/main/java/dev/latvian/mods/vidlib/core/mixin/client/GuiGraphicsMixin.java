package dev.latvian.mods.vidlib.core.mixin.client;

import dev.latvian.mods.vidlib.core.api.client.VLGuiGraphics;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

/**
 * 26.3 turned {@link GuiGraphicsExtractor} into an extraction-only object: it no longer owns a
 * {@code MultiBufferSource}, and its pose is a {@link Matrix3x2fStack} (2D) rather than a
 * {@code PoseStack}. The buffer source is therefore instantiated on demand here instead of being
 * shadowed, and the item-count hook targets the renamed {@code itemCount} method.
 */
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

			if (minecraft.gui.screen() != null) {
				vl$originalGuiWidth = minecraft.gui.screen().width;
				minecraft.gui.screen().width = adjustedWidth;
			}
		}

		vl$guiShifted++;
	}

	@Override
	public void vl$popShift() {
		vl$guiShifted--;

		if (vl$guiShifted == 0) {
			pose.popMatrix();

			if (minecraft.gui.screen() != null) {
				minecraft.gui.screen().width = vl$originalGuiWidth;
			}
		}
	}
}
