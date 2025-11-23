package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.visual.DynamicSpriteTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.Executor;

@Mixin(SpriteLoader.class)
public class SpriteLoaderMixin {
	@Shadow
	@Final
	private ResourceLocation location;

	@Inject(method = "stitch", at = @At("HEAD"))
	private void vl$stitch(List<SpriteContents> contents, int mipLevel, Executor executor, CallbackInfoReturnable<SpriteLoader.Preparations> cir) {
		DynamicSpriteTexture.clearAtlas(location);
	}
}
