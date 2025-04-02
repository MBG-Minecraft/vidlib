package dev.beast.mods.shimmer.math.worldposition;

import com.mojang.serialization.Codec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum EntityPositionType implements StringRepresentable {
	BOTTOM("bottom"),
	CENTER("center"),
	TOP("top"),
	EYES("eyes"),
	LEASH("leash"),
	SOUND_SOURCE("sound_source"),
	LOOK_TARGET("look_target"),

	;

	public static final EntityPositionType[] VALUES = values();
	public static final Codec<EntityPositionType> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, EntityPositionType> STREAM_CODEC = ShimmerStreamCodecs.enumValue(VALUES);

	private final String name;

	EntityPositionType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}
}
