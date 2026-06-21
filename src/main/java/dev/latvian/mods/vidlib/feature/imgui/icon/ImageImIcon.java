package dev.latvian.mods.vidlib.feature.imgui.icon;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import imgui.ImGui;
import net.minecraft.resources.Identifier;

public record ImageImIcon(Identifier texture, float sizeX, float sizeY, float u0, float v0, float u1, float v1) implements ImIcon {
	public ImageImIcon(Identifier texture, float sizeX, float sizeY) {
		this(texture, sizeX, sizeY, 0F, 0F, 1F, 1F);
	}

	public ImageImIcon(Identifier texture, float u0, float v0, float u1, float v1) {
		this(texture, 18F, 18F, u0, v0, u1, v1);
	}

	public ImageImIcon(Identifier texture) {
		this(texture, 18F, 18F);
	}

	@Override
	public char toChar() {
		return 0;
	}

	@Override
	public String formatLabel(ImGraphics graphics, String label) {
		var tex = graphics.mc.getTextureManager().getTexture(texture);
		ImGui.image(ImGraphics.getTextureId(tex.getTexture()), sizeX, sizeY, u0, v0, u1, v1);
		ImGui.sameLine();
		return label;
	}
}
