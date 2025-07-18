package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLGuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GuiGraphics.class)
public class GuiGraphicsMixin implements VLGuiGraphics {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Override
	public Minecraft vl$mc() {
		return minecraft;
	}
}
