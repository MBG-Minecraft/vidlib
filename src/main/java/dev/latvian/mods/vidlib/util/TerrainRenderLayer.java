package dev.latvian.mods.vidlib.util;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum TerrainRenderLayer implements StringRepresentable {
	SOLID("solid"),
	CUTOUT_MIPPED("cutout_mipped"),
	CUTOUT("cutout"),
	TRANSLUCENT("translucent"),
	TRIPWIRE("tripwire"),
	PARTICLE("particle"),
	BRIGHT("bright"),
	BLOOM("bloom");

	public static final TerrainRenderLayer[] ALL = values();
	public static final Codec<TerrainRenderLayer> CODEC = StringRepresentable.fromEnum(() -> ALL);
	public static final StreamCodec<ByteBuf, TerrainRenderLayer> STREAM_CODEC = KLibStreamCodecs.enumValue(ALL);

	public static TerrainRenderLayer fromBlockRenderType(Object blockRenderType) {
		for (TerrainRenderLayer type : ALL) {
			if (type.blockRenderType == blockRenderType) {
				return type;
			}
		}

		return SOLID;
	}

	private final String name;
	public Object neoForgeStage;
	public Object blockRenderType;
	public Object renderTypeFunction;
	public Object noCullRenderTypeFunction;

	TerrainRenderLayer(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public void setClientValues(Object neoForgeStage, Object blockRenderType, Object renderTypeFunction, Object noCullRenderTypeFunction) {
		this.neoForgeStage = neoForgeStage;
		this.blockRenderType = blockRenderType;
		this.renderTypeFunction = renderTypeFunction;
		this.noCullRenderTypeFunction = noCullRenderTypeFunction;
	}
}
