package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.klib.util.Lazy;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

public record DynamicTextureHolder<T extends AbstractTexture>(ClientAsset.ResourceTexture asset, Lazy<T> texture) {
	public DynamicTextureHolder(Identifier id, Lazy<T> texture) {
		this(new ClientAsset.ResourceTexture(id), texture);
	}
}
