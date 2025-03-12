package dev.beast.mods.shimmer.feature.zone;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum ZoneRenderType implements StringRepresentable {
	NORMAL("normal"),
	COLLISIONS("collisions"),
	BLOCKS("blocks");

	public static final ZoneRenderType[] VALUES = values();
	public static final Codec<ZoneRenderType> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, ZoneRenderType> STREAM_CODEC = ShimmerStreamCodecs.enumValue(VALUES);

	private final String name;

	ZoneRenderType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
