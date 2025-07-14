package dev.latvian.mods.vidlib.feature.imgui.icon;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import imgui.ImGui;
import net.minecraft.resources.ResourceLocation;

public record ImageImIcon(ResourceLocation texture, float sizeX, float sizeY, float u0, float v0, float u1, float v1) implements ImIcon {
	public ImageImIcon(ResourceLocation texture, float sizeX, float sizeY) {
		this(texture, sizeX, sizeY, 0F, 0F, 1F, 1F);
	}

	public ImageImIcon(ResourceLocation texture, float u0, float v0, float u1, float v1) {
		this(texture, 18F, 18F, u0, v0, u1, v1);
	}

	public ImageImIcon(ResourceLocation texture) {
		this(texture, 18F, 18F);
	}

	@Override
	public char toChar() {
		return 0;
	}

	@Override
	public String formatLabel(ImGraphics graphics, String label) {
		var tex = graphics.mc.getTextureManager().getTexture(texture);
		ImGui.image(tex.getTexture().vl$getHandle(), sizeX, sizeY, u0, v0, u1, v1);
		ImGui.sameLine();
		return label;
	}
}
