package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.color.Color;

public record StructureRendererData(
	boolean centerX,
	boolean centerY,
	boolean centerZ,
	boolean cull,
	Color glowing,
	int skyLight,
	int blockLight,
	boolean inflate
) {
	public static final StructureRendererData DEFAULT = new StructureRendererData(
		true,
		false,
		true,
		true,
		Color.TRANSPARENT,
		15,
		15,
		false
	);

	public static final Codec<StructureRendererData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("center_x", true).forGetter(StructureRendererData::centerX),
		Codec.BOOL.optionalFieldOf("center_y", false).forGetter(StructureRendererData::centerY),
		Codec.BOOL.optionalFieldOf("center_z", true).forGetter(StructureRendererData::centerZ),
		Codec.BOOL.optionalFieldOf("cull", true).forGetter(StructureRendererData::cull),
		Color.CODEC.optionalFieldOf("glowing", Color.TRANSPARENT).forGetter(StructureRendererData::glowing),
		Codec.INT.optionalFieldOf("sky_level", 15).forGetter(StructureRendererData::skyLight),
		Codec.INT.optionalFieldOf("block_level", 15).forGetter(StructureRendererData::blockLight),
		Codec.BOOL.optionalFieldOf("inflate", false).forGetter(StructureRendererData::inflate)
	).apply(instance, StructureRendererData::new));
}
