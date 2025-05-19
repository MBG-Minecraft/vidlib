package dev.latvian.mods.vidlib.feature.entity.progress;

import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public interface ProgressBarRenderer {
	static void draw(Minecraft mc, GuiGraphics graphics, DeltaTracker deltaTracker) {
		if (mc.level == null || mc.options.hideGui || mc.level.isReplayLevel()) {
			return;
		}

		var y = 0;
		var delta = deltaTracker.getGameTimeDeltaPartialTick(false);

		for (var valueSupplier : ProgressBar.SUPPLIERS) {
			for (var value : valueSupplier.getValues(mc.level, delta)) {
				y += value.bar().type().offset();

				var type = value.bar().type();
				var scale = value.bar().type().scale();
				var progress = value.progress();

				var x = (int) (graphics.guiWidth() / 2F - type.centerX() * scale);
				var scaledTextureWidth = (int) (type.textureWidth() * scale);
				var scaledTextureHeight = (int) (type.textureHeight() * scale);
				var scaledHeight = (int) (type.height() * scale);

				graphics.blit(
					VidLibRenderTypes.GUI,
					type.textures().background(),
					x,
					y,
					0F,
					0F,
					(int) (type.width() * scale),
					scaledHeight,
					scaledTextureWidth,
					scaledTextureHeight
				);

				var scaledBarStart = (int) (type.barStart() * scale);
				var scaledWith = Mth.ceil(type.barWidth() * scale * progress);

				graphics.blit(
					VidLibRenderTypes.GUI,
					type.textures().bar(),
					x + scaledBarStart,
					y,
					scaledBarStart,
					0F,
					scaledWith,
					scaledHeight,
					scaledTextureWidth,
					scaledTextureHeight
				);

				y += scaledHeight;
			}
		}
	}
}
