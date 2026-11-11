package dev.latvian.mods.vidlib.core.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkinTextureDownloader.class)
public class SkinTextureDownloaderMixin {
	@Inject(method = "lambda$registerTextureInManager$0", at = @At("RETURN"))
	private static void vl$registerTextureInManager(net.minecraft.core.ClientAsset.Texture texture, NativeImage image, CallbackInfoReturnable<net.minecraft.core.ClientAsset.Texture> cir) {
		MiscClientUtils.SKIN_IMAGE_MAP.put(texture.texturePath(), image);
	}
}
