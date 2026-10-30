package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.GpuTexture;
import dev.latvian.mods.vidlib.core.VLSpriteContents;
import dev.latvian.mods.vidlib.feature.visual.DynamicSpriteTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpriteContents.class)
public class SpriteContentsMixin implements VLSpriteContents {
	@Shadow
	public NativeImage[] byMipLevel;

	@Shadow
	@Final
	int width;

	@Shadow
	@Final
	int height;

	@Unique
	private DynamicSpriteTexture vl$dynamicSpriteTexture;

	@Override
	public void vl$setDynamicSpriteTexture(@Nullable DynamicSpriteTexture texture) {
		vl$dynamicSpriteTexture = texture;
	}

	@Inject(method = "uploadFirstFrame", at = @At("RETURN"))
	private void vl$uploadFirstFrame(GpuTexture destination, int level, CallbackInfo ci) {
		if (vl$dynamicSpriteTexture != null && vl$dynamicSpriteTexture.initialized && level < this.byMipLevel.length) {
			var target = vl$dynamicSpriteTexture.getTexture();

			if (target != null) {
				// NeoForge: Skip uploading if the texture would be made invalid by mip level
				if ((this.width >> level) <= 0 || (this.height >> level) <= 0) {
					return;
				}

				RenderSystem.getDevice()
					.createCommandEncoder()
					.writeToTexture(target, this.byMipLevel[level], level, 0, 0, 0);
			}
		}
	}
}
