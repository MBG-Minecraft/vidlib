package dev.beast.mods.shimmer.math;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

import java.util.Map;

public record Color(int argb) {
	public static final Color TRANSPARENT = new Color(0x00000000);
	public static final Color WHITE = new Color(0xFFFFFFFF);
	public static final Color BLACK = new Color(0xFF000000);
	public static final Color RED = new Color(0xFFFF0000);
	public static final Color GREEN = new Color(0xFF00FF00);
	public static final Color BLUE = new Color(0xFF0000FF);
	public static final Color YELLOW = new Color(0xFFFFFF00);
	public static final Color MAGENTA = new Color(0xFFFF00FF);
	public static final Color CYAN = new Color(0xFF00FFFF);

	private static final Map<String, Color> NAMED_COLORS = Map.of(
		"transparent", TRANSPARENT,
		"white", WHITE,
		"black", BLACK,
		"red", RED,
		"green", GREEN,
		"blue", BLUE,
		"yellow", YELLOW,
		"magenta", MAGENTA,
		"cyan", CYAN
	);

	public static final Codec<Color> CODEC = Codec.STRING.comapFlatMap(s -> {
		var col = NAMED_COLORS.get(s);

		if (col != null) {
			return DataResult.success(col);
		} else if ((s.length() == 7 || s.length() == 9) && s.charAt(0) == '#') {
			return DataResult.success(new Color((s.length() == 7 ? 0xFF000000 : 0) | Integer.parseUnsignedInt(s.substring(1), 16)));
		} else {
			return DataResult.error(() -> "Invalid color format, expected #RRGGBB or #AARRGGBB: " + s);
		}
	}, c -> c.alpha() == 255 ? c.toRGBString() : c.toARGBString());

	public static Codec<Color> codecWithAlpha(int alpha) {
		return CODEC.xmap(color -> color.withAlpha(alpha), color -> color.withAlpha(255));
	}

	public static Codec<Color> codecWithAlpha(float alpha) {
		return CODEC.xmap(color -> color.withAlpha(alpha), color -> color.withAlpha(255));
	}

	public static final Codec<Color> CODEC_RGB = codecWithAlpha(255);

	public static final StreamCodec<ByteBuf, Color> STREAM_CODEC = ByteBufCodecs.INT.map(Color::new, Color::argb);

	public Color(int a, int r, int g, int b) {
		this(((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
	}

	public Color(int r, int g, int b) {
		this(255, r, g, b);
	}

	public Color(float a, float r, float g, float b) {
		this((int) (a * 255F), (int) (r * 255F), (int) (g * 255F), (int) (b * 255F));
	}

	public int rgb() {
		return argb & 0xFFFFFF;
	}

	public int alpha() {
		return (argb >> 24) & 0xFF;
	}

	public int red() {
		return (argb >> 16) & 0xFF;
	}

	public int green() {
		return (argb >> 8) & 0xFF;
	}

	public int blue() {
		return argb & 0xFF;
	}

	public float alphaf() {
		return alpha() / 255F;
	}

	public float redf() {
		return red() / 255F;
	}

	public float greenf() {
		return green() / 255F;
	}

	public float bluef() {
		return blue() / 255F;
	}

	public Color withAlpha(int alpha) {
		return new Color(alpha, red(), green(), blue());
	}

	public Color withAlpha(float alpha) {
		return new Color((int) (alpha * 255F), red(), green(), blue());
	}

	@Override
	public int hashCode() {
		return argb;
	}

	public String toRGBString() {
		return "#%06X".formatted(rgb());
	}

	public String toARGBString() {
		return "#%08X".formatted(argb);
	}

	@Override
	public String toString() {
		return toARGBString();
	}

	public Color lerp(float delta, Color other, int alpha) {
		return new Color(
			alpha,
			Mth.lerpInt(delta, red(), other.red()),
			Mth.lerpInt(delta, green(), other.green()),
			Mth.lerpInt(delta, blue(), other.blue())
		);
	}

	public Color lerp(float delta, Color other) {
		return lerp(delta, other, Mth.lerpInt(delta, alpha(), other.alpha()));
	}
}
