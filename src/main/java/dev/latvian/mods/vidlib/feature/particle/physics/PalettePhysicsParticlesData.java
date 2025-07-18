package dev.latvian.mods.vidlib.feature.particle.physics;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.block.BlockStatePalette;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record PalettePhysicsParticlesData(ResourceLocation id, long seed, BlockStatePalette palette, List<BlockPos> positions) {
	public static final Codec<PalettePhysicsParticlesData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ID.CODEC.fieldOf("id").forGetter(PalettePhysicsParticlesData::id),
		Codec.LONG.optionalFieldOf("seed", 0L).forGetter(PalettePhysicsParticlesData::seed),
		BlockStatePalette.CODEC.fieldOf("palette").forGetter(PalettePhysicsParticlesData::palette),
		BlockPos.CODEC.listOf().optionalFieldOf("positions", List.of()).forGetter(PalettePhysicsParticlesData::positions)
	).apply(instance, PalettePhysicsParticlesData::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, PalettePhysicsParticlesData> STREAM_CODEC = CompositeStreamCodec.of(
		ID.STREAM_CODEC, PalettePhysicsParticlesData::id,
		ByteBufCodecs.LONG, PalettePhysicsParticlesData::seed,
		BlockStatePalette.STREAM_CODEC, PalettePhysicsParticlesData::palette,
		BlockPos.STREAM_CODEC.listOf(), PalettePhysicsParticlesData::positions,
		PalettePhysicsParticlesData::new
	);
}
