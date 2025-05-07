package dev.latvian.mods.vidlib.util;

import com.mojang.serialization.Codec;
import dev.latvian.mods.vidlib.feature.codec.VLStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum TerrainRenderLayer implements StringRepresentable {
	SOLID("solid"),
	CUTOUT_MIPPED("cutout_mipped"),
	CUTOUT("cutout"),
	TRANSLUCENT("translucent"),
	TRIPWIRE("tripwire");

	public static final TerrainRenderLayer[] VALUES = values();
	public static final Codec<TerrainRenderLayer> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, TerrainRenderLayer> STREAM_CODEC = VLStreamCodecs.enumValue(VALUES);

	public static TerrainRenderLayer fromBlockRenderType(Object blockRenderType) {
		for (TerrainRenderLayer type : VALUES) {
			if (type.blockRenderType == blockRenderType) {
				return type;
			}
		}

		return SOLID;
	}

	private final String name;
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

	public void setClientValues(Object blockRenderType, Object renderTypeFunction, Object noCullRenderTypeFunction) {
		this.blockRenderType = blockRenderType;
		this.renderTypeFunction = renderTypeFunction;
		this.noCullRenderTypeFunction = noCullRenderTypeFunction;
	}
}
