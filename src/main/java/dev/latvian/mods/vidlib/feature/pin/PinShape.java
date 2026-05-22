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
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public enum PinShape implements StringRepresentable {
	PIN(VidLib.id("pin"), "Pin", false, VidLibTextures.CIRCLE, 103, 137, 308),
	PIN_CROSS(VidLib.id("pin_cross"), "Pin with Cross", false, VidLibTextures.CIRCLE, 103, 137, 308),
	SQUARE("square", "Full Square", true, null, VidLibTextures.SQUARE, VidLib.id("textures/misc/pin/icon/square.png"), 0, 0, 512),
	CIRCLE("circle", "Full Circle", true, null, VidLibTextures.CIRCLE, VidLib.id("textures/misc/pin/icon/circle.png"), 0, 0, 512),
	ARROW(VidLib.id("arrow"), "Arrow", true, VidLibTextures.SQUARE, 0, 0, 512),
	ARROW_OFFSET(VidLib.id("arrow_offset"), "Arrow (Offset)", true, VidLibTextures.SQUARE, 50, 0, 412),

	;

	public static final PinShape[] VALUES = values();
	public static final Codec<PinShape> CODEC = Codec.either(KLibCodecs.anyEnumCodec(VALUES), Codec.INT).xmap(e -> e.map(Function.identity(), i -> VALUES[i]), Either::left);

	public final String id;
	public final String displayName;
	public final boolean transparentBackground;
	public final ResourceLocation overlayTexture;
	public final ResourceLocation maskTexture;
	public final ResourceLocation iconTexture;
	public final TexturedRenderType maskedRenderType;
	public final int x, y, size;

	PinShape(String id, String displayName, boolean transparentBackground, @Nullable ResourceLocation overlayTexture, ResourceLocation maskTexture, ResourceLocation iconTexture, int x, int y, int size) {
		this.id = id;
		this.displayName = displayName;
		this.transparentBackground = transparentBackground;
		this.overlayTexture = overlayTexture;
		this.maskTexture = maskTexture;
		this.iconTexture = iconTexture;
		this.maskedRenderType = VidLibRenderTypes.MASKED_GUI.apply(maskTexture);
		this.x = x;
		this.y = y;
		this.size = size;
	}

	PinShape(ResourceLocation id, String displayName, boolean transparentBackground, ResourceLocation maskTexture, int x, int y, int size) {
		this(id.getPath(), displayName, transparentBackground, id.withPath(s -> "textures/misc/pin/" + s + ".png"), maskTexture, id.withPath(s -> "textures/misc/pin/icon/" + s + ".png"), x, y, size);
	}

	@Override
	public String getSerializedName() {
		return id;
	}
}
