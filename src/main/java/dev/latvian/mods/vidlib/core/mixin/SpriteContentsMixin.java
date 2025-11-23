package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
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

	@Inject(method = "upload", at = @At("RETURN"))
	private void vl$upload(int x, int y, int sourceX, int sourceY, NativeImage[] images, GpuTexture texture, CallbackInfo ci) {
		if (vl$dynamicSpriteTexture != null && vl$dynamicSpriteTexture.initialized) {
			var target = vl$dynamicSpriteTexture.getTexture();

			for (int i = 0; i < this.byMipLevel.length; i++) {
				// NeoForge: Skip uploading if the texture would be made invalid by mip level
				if ((this.width >> i) <= 0 || (this.height >> i) <= 0) {
					break;
				}

				RenderSystem.getDevice()
					.createCommandEncoder()
					.writeToTexture(target, images[i], i, 0, 0, this.width >> i, this.height >> i, sourceX >> i, sourceY >> i);
			}
		}
	}
}
