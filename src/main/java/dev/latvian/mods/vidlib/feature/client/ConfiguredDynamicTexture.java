package dev.latvian.mods.vidlib.feature.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.TriState;

import java.util.function.Supplier;

public class ConfiguredDynamicTexture extends DynamicTexture {
	public ConfiguredDynamicTexture(Supplier<String> label, NativeImage image, boolean clamp, TriState blur) {
		super(label, image);
		configureSampler(clamp, blur);
	}

	public final void configureSampler(boolean clamp, TriState blur) {
		var addressMode = clamp ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT;
		var filterMode = blur.toBoolean(false) ? FilterMode.LINEAR : FilterMode.NEAREST;
		this.sampler = RenderSystem.getSamplerCache().getSampler(addressMode, addressMode, filterMode, filterMode, false);
	}
}
