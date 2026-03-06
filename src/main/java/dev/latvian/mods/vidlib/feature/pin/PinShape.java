package dev.latvian.mods.vidlib.feature.pin;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.function.Function;

public enum PinShape implements StringRepresentable {
	PIN("pin", "Pin", VidLib.id("textures/misc/pin/1.png"), VidLibTextures.CIRCLE, VidLib.id("textures/misc/pin/1i.png"), 103, 137, 308, 308),
	PIN_CROSS("pin_cross", "Pin with Cross", VidLib.id("textures/misc/pin/2.png"), VidLibTextures.CIRCLE, VidLib.id("textures/misc/pin/2i.png"), 103, 137, 308, 308),
	SQUARE("square", "Full Square", VidLibTextures.TRANSPARENT, VidLibTextures.SQUARE, VidLib.id("textures/misc/pin/squarei.png"), 0, 0, 512, 512),
	CIRCLE("circle", "Full Circle", VidLibTextures.TRANSPARENT, VidLibTextures.CIRCLE, VidLib.id("textures/misc/pin/circlei.png"), 0, 0, 512, 512),

	;

	public static final PinShape[] VALUES = values();
	public static final Codec<PinShape> CODEC = Codec.either(KLibCodecs.anyEnumCodec(VALUES), Codec.INT).xmap(e -> e.map(Function.identity(), i -> VALUES[i]), Either::left);

	public final String id;
	public final String displayName;
	public final ResourceLocation overlayTexture;
	public final ResourceLocation maskTexture;
	public final ResourceLocation iconTexture;
	public final TexturedRenderType maskedRenderType;
	public final int x, y, w, h;

	PinShape(String id, String displayName, ResourceLocation overlayTexture, ResourceLocation maskTexture, ResourceLocation iconTexture, int x, int y, int w, int h) {
		this.id = id;
		this.displayName = displayName;
		this.overlayTexture = overlayTexture;
		this.maskTexture = maskTexture;
		this.iconTexture = iconTexture;
		this.maskedRenderType = VidLibRenderTypes.MASKED_GUI.apply(maskTexture);
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}

	@Override
	public String getSerializedName() {
		return id;
	}
}
