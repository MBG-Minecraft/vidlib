package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.block.collection.BlockCollection;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.registry.Ref;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PhysicsParticlesDisplayData(
	Ref<PhysicsParticleData> data,
	long seed,
	Ref<BlockCollection> blocks
) {
	public static final Codec<PhysicsParticlesDisplayData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		PhysicsParticleData.CODEC.fieldOf("data").forGetter(PhysicsParticlesDisplayData::data),
		Codec.LONG.optionalFieldOf("seed", 0L).forGetter(PhysicsParticlesDisplayData::seed),
		BlockCollection.CODEC.fieldOf("blocks").forGetter(PhysicsParticlesDisplayData::blocks)
	).apply(instance, PhysicsParticlesDisplayData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PhysicsParticlesDisplayData> STREAM_CODEC = CompositeStreamCodec.of(
		PhysicsParticleData.STREAM_CODEC, PhysicsParticlesDisplayData::data,
		ByteBufCodecs.LONG, PhysicsParticlesDisplayData::seed,
		BlockCollection.STREAM_CODEC, PhysicsParticlesDisplayData::blocks,
		PhysicsParticlesDisplayData::new
	);
}
