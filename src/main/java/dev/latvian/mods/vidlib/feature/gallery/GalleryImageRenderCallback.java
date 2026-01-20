package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;

@FunctionalInterface
public interface GalleryImageRenderCallback<K> {
	NativeImage render(Minecraft mc, K uuid, String name);
}
