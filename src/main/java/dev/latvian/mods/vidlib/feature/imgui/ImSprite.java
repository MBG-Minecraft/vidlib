package dev.latvian.mods.vidlib.feature.imgui;

import imgui.ImDrawList;
import imgui.ImGui;
import net.minecraft.resources.ResourceLocation;

public class ImSprite {

	private final ResourceLocation identifier;
	private final float u0, v0, u1, v1;
	private int textureId;

	public ImSprite(ResourceLocation identifier, int atlasSize, int u, int v) {
		this.identifier = identifier;
		this.u0 = u / (float) atlasSize;
		this.v0 = v / (float) atlasSize;
		this.u1 = (u + 1) / (float) atlasSize;
		this.v1 = (v + 1) / (float) atlasSize;
	}

	public ImSprite(ResourceLocation identifier, float u0, float v0, float u1, float v1) {
		this.identifier = identifier;
		this.u0 = u0;
		this.v0 = v0;
		this.u1 = u1;
		this.v1 = v1;
	}

	public void add(ImDrawList drawList, float x, float y, float width, float height, int color) {
		drawList.addImage(getTextureId(), x, y, x + width, y + height, u0, v0, u1, v1, color);
	}

	public void draw(float width, float height) {
		ImGui.image(getTextureId(), width, height, u0, v0, u1, v1);
	}

	public ResourceLocation getIdentifier() {
		return this.identifier;
	}

	public float getU0() {
		return this.u0;
	}

	public float getU1() {
		return this.u1;
	}

	public float getV0() {
		return this.v0;
	}

	public float getV1() {
		return this.v1;
	}

	private int getTextureId() {
		if (textureId == 0) {
			textureId = ImGuiUtils.getTextureId(identifier);
		}
		return textureId;
	}

}
