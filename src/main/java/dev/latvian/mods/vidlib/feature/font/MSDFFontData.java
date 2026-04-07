package dev.latvian.mods.vidlib.feature.font;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;

import java.util.List;
import java.util.Optional;

public record MSDFFontData(
	Atlas atlas,
	Metrics metrics,
	List<Glyph> glyphs,
	List<Kerning> kerning
) {
	public record Atlas(
		String type,
		float distanceRange,
		float distanceRangeMiddle,
		float size,
		float width,
		float height,
		String yOrigin
	) {
		public static final Codec<Atlas> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.fieldOf("type").forGetter(Atlas::type),
			Codec.FLOAT.fieldOf("distanceRange").forGetter(Atlas::distanceRange),
			Codec.FLOAT.fieldOf("distanceRangeMiddle").forGetter(Atlas::distanceRangeMiddle),
			Codec.FLOAT.fieldOf("size").forGetter(Atlas::size),
			Codec.FLOAT.fieldOf("width").forGetter(Atlas::width),
			Codec.FLOAT.fieldOf("height").forGetter(Atlas::height),
			Codec.STRING.fieldOf("yOrigin").forGetter(Atlas::yOrigin)
		).apply(instance, Atlas::new));
	}

	public record Metrics(
		float emSize,
		float lineHeight,
		float ascender,
		float descender,
		float underlineY,
		float underlineThickness
	) {
		public static final Codec<Metrics> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("emSize").forGetter(Metrics::emSize),
			Codec.FLOAT.fieldOf("lineHeight").forGetter(Metrics::lineHeight),
			Codec.FLOAT.fieldOf("ascender").forGetter(Metrics::ascender),
			Codec.FLOAT.fieldOf("descender").forGetter(Metrics::descender),
			Codec.FLOAT.fieldOf("underlineY").forGetter(Metrics::underlineY),
			Codec.FLOAT.fieldOf("underlineThickness").forGetter(Metrics::underlineThickness)
		).apply(instance, Metrics::new));
	}

	public record Bounds(
		float left,
		float bottom,
		float right,
		float top
	) {
		public static final Bounds EMPTY = new Bounds(0F, 0F, 0F, 0F);

		public static final Codec<Bounds> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("left").forGetter(Bounds::left),
			Codec.FLOAT.fieldOf("bottom").forGetter(Bounds::bottom),
			Codec.FLOAT.fieldOf("right").forGetter(Bounds::right),
			Codec.FLOAT.fieldOf("top").forGetter(Bounds::top)
		).apply(instance, Bounds::new));
	}

	public record Glyph(
		char unicode,
		float advance,
		Optional<Bounds> planeBounds,
		Optional<Bounds> atlasBounds
	) {
		public static final Codec<Character> CHAR_FROM_INT_CODEC = ExtraCodecs.NON_NEGATIVE_INT.xmap(integer -> (char) integer.intValue(), Integer::valueOf);

		public static final Codec<Glyph> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CHAR_FROM_INT_CODEC.fieldOf("unicode").forGetter(Glyph::unicode),
			Codec.FLOAT.fieldOf("advance").forGetter(Glyph::advance),
			Bounds.CODEC.optionalFieldOf("planeBounds").forGetter(Glyph::planeBounds),
			Bounds.CODEC.optionalFieldOf("atlasBounds").forGetter(Glyph::atlasBounds)
		).apply(instance, Glyph::new));
	}

	public record Kerning(
		char unicode1,
		char unicode2,
		float advance
	) {
		public static final Codec<Character> CHAR_FROM_INT_CODEC = ExtraCodecs.NON_NEGATIVE_INT.xmap(integer -> (char) integer.intValue(), Integer::valueOf);

		public static final Codec<Kerning> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			CHAR_FROM_INT_CODEC.fieldOf("unicode1").forGetter(Kerning::unicode1),
			CHAR_FROM_INT_CODEC.fieldOf("unicode2").forGetter(Kerning::unicode2),
			Codec.FLOAT.fieldOf("advance").forGetter(Kerning::advance)
		).apply(instance, Kerning::new));
	}

	public static final MSDFFontData EMPTY = new MSDFFontData(
		new Atlas("empty", 0F, 0F, 0F, 0F, 0F, "bottom"),
		new Metrics(0F, 0F, 0F, 0F, 0F, 0F),
		List.of(),
		List.of()
	);

	public static final Codec<MSDFFontData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Atlas.CODEC.fieldOf("atlas").forGetter(MSDFFontData::atlas),
		Metrics.CODEC.fieldOf("metrics").forGetter(MSDFFontData::metrics),
		Glyph.CODEC.listOf().fieldOf("glyphs").forGetter(MSDFFontData::glyphs),
		Kerning.CODEC.listOf().optionalFieldOf("kerning", List.of()).forGetter(MSDFFontData::kerning)
	).apply(instance, MSDFFontData::new));
}
