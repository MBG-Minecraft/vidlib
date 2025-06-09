package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.bulk.PositionedBlock;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record PhysicsParticlesIdData(ResourceLocation id, long seed, List<PositionedBlock> blocks) {
	public static final Codec<PhysicsParticlesIdData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.fieldOf("id").forGetter(PhysicsParticlesIdData::id),
		Codec.LONG.optionalFieldOf("seed", 0L).forGetter(PhysicsParticlesIdData::seed),
		PositionedBlock.CODEC.listOf().optionalFieldOf("blocks", List.of()).forGetter(PhysicsParticlesIdData::blocks)
	).apply(instance, PhysicsParticlesIdData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PhysicsParticlesIdData> STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC, PhysicsParticlesIdData::id,
		ByteBufCodecs.LONG, PhysicsParticlesIdData::seed,
		PositionedBlock.LIST_STREAM_CODEC, PhysicsParticlesIdData::blocks,
		PhysicsParticlesIdData::new
	);
}
