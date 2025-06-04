package dev.latvian.mods.vidlib.feature.misc;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public interface ScreenTextRenderer {
	static void render(GuiGraphics graphics, ScreenText text, Font font, int startX, int startY, int width, int height, int bgColor, int color) {
		for (int i = 0; i < text.topLeft.list.size(); i++) {
			int w = font.width(text.topLeft.list.get(i));
			int x = startX + 1;
			int y = startY + 2 + i * 11;

			if (bgColor != 0) {
				graphics.fill(x, y, x + w + 3, y + 11, bgColor);
			}

			graphics.drawString(font, text.topLeft.list.get(i), x + 2, y + 2, color, true);
		}

		for (int i = 0; i < text.topRight.list.size(); i++) {
			int w = font.width(text.topRight.list.get(i));
			int x = startX + width - w - 4;
			int y = startY + 2 + i * 11;

			if (bgColor != 0) {
				graphics.fill(x, y, x + w + 3, y + 11, bgColor);
			}

			graphics.drawString(font, text.topRight.list.get(i), x + 2, y + 2, color, true);
		}

		for (int i = 0; i < text.bottomLeft.list.size(); i++) {
			int w = font.width(text.bottomLeft.list.get(i));
			int x = startX + 1;
			int y = startY + i * 11 + height - text.bottomLeft.list.size() * 11 - 2;

			if (bgColor != 0) {
				graphics.fill(x, y, x + w + 3, y + 11, bgColor);
			}

			graphics.drawString(font, text.bottomLeft.list.get(i), x + 2, y + 2, color, true);
		}

		for (int i = 0; i < text.bottomRight.list.size(); i++) {
			int w = font.width(text.bottomRight.list.get(i));
			int x = startX + width - w - 4;
			int y = startY + i * 11 + height - text.bottomRight.list.size() * 11 - 2;

			if (bgColor != 0) {
				graphics.fill(x, y, x + w + 3, y + 11, bgColor);
			}

			graphics.drawString(font, text.bottomRight.list.get(i), x + 2, y + 2, color, true);
		}
	}
}
