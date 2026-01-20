package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLSpriteContents;
import dev.latvian.mods.vidlib.core.VLTextureAtlasSprite;
import dev.latvian.mods.vidlib.feature.visual.DynamicSpriteTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(TextureAtlasSprite.class)
public class TextureAtlasSpriteMixin implements VLTextureAtlasSprite {
	@Shadow
	@Final
	private SpriteContents contents;

	@Unique
	private DynamicSpriteTexture vl$dst;

	@Override
	public void vl$invalidateDynamicSpriteTexture() {
		vl$dst = null;
	}

	@Override
	public DynamicSpriteTexture vl$getDynamicSpriteTexture(Minecraft mc) {
		if (vl$dst == null) {
			vl$dst = new DynamicSpriteTexture((TextureAtlasSprite) (Object) this);
			vl$dst.initialize(mc);
			mc.getTextureManager().register(vl$dst.path, vl$dst);
			vl$dst.initialized = true;
			((VLSpriteContents) contents).vl$setDynamicSpriteTexture(vl$dst);
		}

		return vl$dst;
	}
}
