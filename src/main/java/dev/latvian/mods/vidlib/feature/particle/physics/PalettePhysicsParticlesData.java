package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.vidlib.feature.block.BlockStatePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record PalettePhysicsParticlesData(PhysicsParticleData data, long seed, BlockStatePalette palette, List<BlockPos> positions) {
	public static final Codec<PalettePhysicsParticlesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		PhysicsParticleData.CODEC.fieldOf("data").forGetter(PalettePhysicsParticlesData::data),
		Codec.LONG.optionalFieldOf("seed", 0L).forGetter(PalettePhysicsParticlesData::seed),
		BlockStatePalette.CODEC.fieldOf("palette").forGetter(PalettePhysicsParticlesData::palette),
		BlockPos.CODEC.listOf().optionalFieldOf("positions", List.of()).forGetter(PalettePhysicsParticlesData::positions)
	).apply(instance, PalettePhysicsParticlesData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PalettePhysicsParticlesData> STREAM_CODEC = CompositeStreamCodec.of(
		PhysicsParticleData.STREAM_CODEC, PalettePhysicsParticlesData::data,
		ByteBufCodecs.LONG, PalettePhysicsParticlesData::seed,
		BlockStatePalette.STREAM_CODEC, PalettePhysicsParticlesData::palette,
		KLibStreamCodecs.listOf(BlockPos.STREAM_CODEC), PalettePhysicsParticlesData::positions,
		PalettePhysicsParticlesData::new
	);
}
