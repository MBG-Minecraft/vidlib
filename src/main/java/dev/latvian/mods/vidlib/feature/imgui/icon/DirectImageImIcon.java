package dev.latvian.mods.vidlib.feature.imgui.icon;

import dev.latvian.mods.vidlib.core.VLGpuTexture;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import imgui.ImGui;

public record DirectImageImIcon(int textureID, float sizeX, float sizeY, float u0, float v0, float u1, float v1) implements ImIcon {
	public DirectImageImIcon(VLGpuTexture texture, float sizeX, float sizeY, float u0, float v0, float u1, float v1) {
		this(texture.vl$getHandle(), sizeX, sizeY, u0, v0, u1, v1);
	}

	public DirectImageImIcon(VLGpuTexture texture, float sizeX, float sizeY) {
		this(texture.vl$getHandle(), sizeX, sizeY, 0F, 0F, 1F, 1F);
	}

	public DirectImageImIcon(VLGpuTexture texture, float u0, float v0, float u1, float v1) {
		this(texture, 18F, 18F, u0, v0, u1, v1);
	}

	public DirectImageImIcon(VLGpuTexture texture) {
		this(texture, 18F, 18F);
	}

	@Override
	public char toChar() {
		return 0;
	}

	@Override
	public String formatLabel(ImGraphics graphics, String label) {
		ImGui.image(textureID, sizeX, sizeY, u0, v0, u1, v1);
		ImGui.sameLine();
		return label;
	}
}
