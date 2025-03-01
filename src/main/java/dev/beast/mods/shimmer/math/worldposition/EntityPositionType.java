package dev.beast.mods.shimmer.math.worldposition;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public enum EntityPositionType implements StringRepresentable {
	BOTTOM("bottom"),
	CENTER("center"),
	TOP("top"),
	EYES("eyes"),
	LEASH("leash");

	public static final EntityPositionType[] VALUES = values();
	public static final Codec<EntityPositionType> CODEC = StringRepresentable.fromEnum(() -> VALUES);
	public static final StreamCodec<ByteBuf, EntityPositionType> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(i -> VALUES[i], EntityPositionType::ordinal);

	private final String name;

	EntityPositionType(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return name;
	}

	public Vec3 getPosition(Entity e) {
		return switch (this) {
			case CENTER -> new Vec3(e.getX(), e.getY() + e.getBbHeight() / 2D, e.getZ());
			case TOP -> new Vec3(e.getX(), e.getY() + e.getBbHeight(), e.getZ());
			case EYES -> e.getEyePosition();
			case LEASH -> e.position().add(e.getLeashOffset(1F));
			default -> e.position();
		};
	}
}
