package dev.latvian.mods.vidlib.feature.pin;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public enum PinShape implements StringRepresentable {
	PIN(ID.vidlib("pin"), "Pin", false, VidLibTextures.CIRCLE.texturePath(), 103, 137, 308),
	PIN_CROSS(ID.vidlib("pin_cross"), "Pin with Cross", false, VidLibTextures.CIRCLE.texturePath(), 103, 137, 308),
	SQUARE("square", "Full Square", true, null, VidLibTextures.SQUARE.texturePath(), ID.vidlib("textures/misc/pin/icon/square.png"), 0, 0, 512),
	CIRCLE("circle", "Full Circle", true, null, VidLibTextures.CIRCLE.texturePath(), ID.vidlib("textures/misc/pin/icon/circle.png"), 0, 0, 512),
	ARROW(ID.vidlib("arrow"), "Arrow", true, VidLibTextures.SQUARE.texturePath(), 0, 0, 512),
	ARROW_OFFSET(ID.vidlib("arrow_offset"), "Arrow (Offset)", true, VidLibTextures.SQUARE.texturePath(), 50, 0, 412),

	;

	public static final PinShape[] VALUES = values();
	public static final Codec<PinShape> CODEC = Codec.either(KLibCodecs.anyEnumCodec(VALUES), Codec.INT).xmap(e -> e.map(Function.identity(), i -> VALUES[i]), Either::left);

	public final String id;
	public final String displayName;
	public final boolean transparentBackground;
	public final Identifier overlayTexture;
	public final Identifier maskTexture;
	public final Identifier iconTexture;
	public final TexturedRenderType maskedRenderType;
	public final int x, y, size;

	PinShape(String id, String displayName, boolean transparentBackground, @Nullable Identifier overlayTexture, Identifier maskTexture, Identifier iconTexture, int x, int y, int size) {
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

	PinShape(Identifier id, String displayName, boolean transparentBackground, Identifier maskTexture, int x, int y, int size) {
		this(id.getPath(), displayName, transparentBackground, id.withPath(s -> "textures/misc/pin/" + s + ".png"), maskTexture, id.withPath(s -> "textures/misc/pin/icon/" + s + ".png"), x, y, size);
	}

	@Override
	public String getSerializedName() {
		return id;
	}
}
