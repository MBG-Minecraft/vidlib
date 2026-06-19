package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import net.minecraft.core.ClientAsset;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(SkinTextureDownloader.class)
public class SkinTextureDownloaderMixin {
	@Inject(method = "registerTextureInManager", at = @At("HEAD"))
	private void vl$registerTextureInManager(ClientAsset.Texture texture, NativeImage image, CallbackInfoReturnable<CompletableFuture<ClientAsset.Texture>> cir) {
		MiscClientUtils.SKIN_IMAGE_MAP.put(texture.texturePath(), image);
	}
}
