package dev.latvian.mods.vidlib.feature.gallery;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;

import java.util.UUID;

@FunctionalInterface
public interface GalleryImageRenderCallback {
	NativeImage render(Minecraft mc, UUID uuid, String name);
}
