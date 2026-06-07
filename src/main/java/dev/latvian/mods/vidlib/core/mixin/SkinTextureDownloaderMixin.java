package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkinTextureDownloader.class)
public class SkinTextureDownloaderMixin {
	@Inject(method = "lambda$registerTextureInManager$2", at = @At("RETURN"))
	private static void vl$registerTextureInManager(Minecraft mc, ResourceLocation texture, NativeImage image, CallbackInfoReturnable<ResourceLocation> cir) {
		MiscClientUtils.SKIN_IMAGE_MAP.put(texture, image);
	}
}
