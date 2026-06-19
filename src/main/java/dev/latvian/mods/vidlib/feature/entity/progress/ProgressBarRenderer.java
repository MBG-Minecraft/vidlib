package dev.latvian.mods.vidlib.feature.entity.progress;

import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;

public interface ProgressBarRenderer {
	static void draw(Minecraft mc, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (mc.level == null || ClientGameEngine.INSTANCE.hideGui(mc) || mc.level.isReplayLevel()) {
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
					RenderPipelines.GUI_TEXTURED,
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
					RenderPipelines.GUI_TEXTURED,
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
