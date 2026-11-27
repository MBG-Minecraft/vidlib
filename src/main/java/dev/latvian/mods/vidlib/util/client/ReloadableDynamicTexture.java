package dev.latvian.mods.vidlib.util.client;

import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.server.packs.resources.ResourceManager;

public interface ReloadableDynamicTexture {
	void load(ResourceManager resourceManager, TextureManager manager);
}
