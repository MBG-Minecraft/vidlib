package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.VLGuiGraphics;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements VLGuiGraphics {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Override
	public Minecraft vl$mc() {
		return minecraft;
	}

	@WrapOperation(method = "renderItemCount(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;IIIZ)I"))
	private int vl$drawSize(GuiGraphics instance, Font font, String text, int x, int y, int color, boolean dropShadow, Operation<Integer> original, @Local(argsOnly = true) String pText, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true, ordinal = 0) int pX, @Local(argsOnly = true, ordinal = 1) int pY) {
		if (pText == null && stack.getCount() > 1) {
			return MiscClientUtils.drawStackSize(instance, font, stack.getCount(), pX, pY, color, dropShadow);
		}

		return original.call(instance, font, text, x, y, color, dropShadow);
	}
}
