package dev.latvian.mods.vidlib.feature.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataType;
import dev.latvian.mods.klib.math.Rotation;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public record EntitySnapshot(UUID player, Vec3 position, Rotation rotation, boolean crouching) {
	public static final DataType<EntitySnapshot> DATA_TYPE = DataType.of(RecordCodecBuilder.create(instance -> instance.group(
		KLibCodecs.UUID.fieldOf("player").forGetter(EntitySnapshot::player),
		Vec3.CODEC.fieldOf("position").forGetter(EntitySnapshot::position),
		Rotation.CODEC.fieldOf("rotation").forGetter(EntitySnapshot::rotation),
		Codec.BOOL.fieldOf("crouching").forGetter(EntitySnapshot::crouching)
	).apply(instance, EntitySnapshot::new)), CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, EntitySnapshot::player,
		MCStreamCodecs.VEC3, EntitySnapshot::position,
		Rotation.STREAM_CODEC_NO_ROLL, EntitySnapshot::rotation,
		ByteBufCodecs.BOOL, EntitySnapshot::crouching,
		EntitySnapshot::new
	), EntitySnapshot.class);

	public static final DataType<List<EntitySnapshot>> LIST_DATA_TYPE = DATA_TYPE.listOf();

	public EntitySnapshot(Entity entity) {
		this(entity.getUUID(), entity.position(), entity.viewRotation(1F), entity.isCrouching());
	}
}
