package dev.latvian.mods.vidlib.feature.pin;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import net.minecraft.resources.ResourceLocation;

public enum PinShape {
	S1(VidLib.id("textures/misc/pin/1.png"), VidLibTextures.CIRCLE, VidLib.id("textures/misc/pin/1i.png"), 103, 137, 308, 308),
	S2(VidLib.id("textures/misc/pin/2.png"), VidLibTextures.CIRCLE, VidLib.id("textures/misc/pin/2i.png"), 103, 137, 308, 308),

	;

	public static final PinShape[] VALUES = values();

	public final ResourceLocation overlayTexture;
	public final ResourceLocation maskTexture;
	public final ResourceLocation iconTexture;
	public final TexturedRenderType maskedRenderType;
	public final int x, y, w, h;

	PinShape(ResourceLocation overlayTexture, ResourceLocation maskTexture, ResourceLocation iconTexture, int x, int y, int w, int h) {
		this.overlayTexture = overlayTexture;
		this.maskTexture = maskTexture;
		this.iconTexture = iconTexture;
		this.maskedRenderType = VidLibRenderTypes.MASKED_GUI.apply(maskTexture);
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
}
