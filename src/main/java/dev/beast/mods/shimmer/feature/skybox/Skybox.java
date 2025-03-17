package dev.beast.mods.shimmer.feature.skybox;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class Skybox {
	public final SkyboxData data;
	public final ResourceLocation texture;
	public SkyboxTexture skyboxTexture;

	public Skybox(SkyboxData data) {
		this.data = data;
		this.texture = data.texture().isEmpty() ? data.id().withPath(p -> "textures/skybox/" + p + ".png") : data.texture().get();
	}

	public SkyboxTexture loadTexture(Minecraft mc) {
		if (skyboxTexture == null) {
			var id = data.id().withPath(p -> "textures/generated/skybox/" + p + ".png");

			if (mc.getTextureManager().getTexture(id) instanceof SkyboxTexture tex) {
				skyboxTexture = tex;
				return skyboxTexture;
			}

			skyboxTexture = new SkyboxTexture(id, texture);
			mc.getTextureManager().registerAndLoad(skyboxTexture.resourceId(), skyboxTexture);
		}

		return skyboxTexture;
	}
}
