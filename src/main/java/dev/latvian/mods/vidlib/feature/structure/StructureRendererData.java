package dev.latvian.mods.vidlib.feature.structure;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record StructureRendererData(
	boolean centerX,
	boolean centerY,
	boolean centerZ,
	boolean cull,
	int skyLight,
	int blockLight
) {
	public static final StructureRendererData DEFAULT = new StructureRendererData(true, false, true, true, 15, 15);
	public static final StructureRendererData DEFAULT_GHOST_CHUNKS = new StructureRendererData(false, false, false, true, 15, 15);

	public static final Codec<StructureRendererData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("center_x", true).forGetter(StructureRendererData::centerX),
		Codec.BOOL.optionalFieldOf("center_y", false).forGetter(StructureRendererData::centerY),
		Codec.BOOL.optionalFieldOf("center_z", true).forGetter(StructureRendererData::centerZ),
		Codec.BOOL.optionalFieldOf("cull", true).forGetter(StructureRendererData::cull),
		Codec.INT.optionalFieldOf("sky_level", 15).forGetter(StructureRendererData::skyLight),
		Codec.INT.optionalFieldOf("block_level", 15).forGetter(StructureRendererData::blockLight)
	).apply(instance, StructureRendererData::new));
}