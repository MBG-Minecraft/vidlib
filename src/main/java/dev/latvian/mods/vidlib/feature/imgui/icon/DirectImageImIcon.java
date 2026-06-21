package dev.latvian.mods.vidlib.feature.imgui.icon;

import com.mojang.blaze3d.textures.GpuTexture;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import imgui.ImGui;

public record DirectImageImIcon(GpuTexture texture, float sizeX, float sizeY, float u0, float v0, float u1, float v1) implements ImIcon {
	public DirectImageImIcon(GpuTexture texture, float sizeX, float sizeY, float u0, float v0, float u1, float v1) {
		this.texture = texture;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.u0 = u0;
		this.v0 = v0;
		this.u1 = u1;
		this.v1 = v1;
	}

	public DirectImageImIcon(GpuTexture texture, float sizeX, float sizeY) {
		this(texture, sizeX, sizeY, 0F, 0F, 1F, 1F);
	}

	public DirectImageImIcon(GpuTexture texture, float u0, float v0, float u1, float v1) {
		this(texture, 18F, 18F, u0, v0, u1, v1);
	}

	public DirectImageImIcon(GpuTexture texture) {
		this(texture, 18F, 18F);
	}

	@Override
	public char toChar() {
		return 0;
	}

	@Override
	public String formatLabel(ImGraphics graphics, String label) {
		ImGui.image(ImGraphics.getTextureId(texture), sizeX, sizeY, u0, v0, u1, v1);
		ImGui.sameLine();
		return label;
	}
}
