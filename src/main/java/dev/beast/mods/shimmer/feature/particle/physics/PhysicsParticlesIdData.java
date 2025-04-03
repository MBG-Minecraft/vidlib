package dev.beast.mods.shimmer.feature.particle.physics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerCodecs;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record PhysicsParticlesIdData(ResourceLocation id, long seed, List<PositionedBlock> blocks) {
	public static final Codec<PhysicsParticlesIdData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ShimmerCodecs.VIDEO_ID.fieldOf("id").forGetter(PhysicsParticlesIdData::id),
		Codec.LONG.optionalFieldOf("seed", 0L).forGetter(PhysicsParticlesIdData::seed),
		PositionedBlock.CODEC.listOf().optionalFieldOf("blocks", List.of()).forGetter(PhysicsParticlesIdData::blocks)
	).apply(instance, PhysicsParticlesIdData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PhysicsParticlesIdData> STREAM_CODEC = CompositeStreamCodec.of(
		ShimmerStreamCodecs.VIDEO_ID, PhysicsParticlesIdData::id,
		ByteBufCodecs.LONG, PhysicsParticlesIdData::seed,
		PositionedBlock.LIST_STREAM_CODEC, PhysicsParticlesIdData::blocks,
		PhysicsParticlesIdData::new
	);
}
