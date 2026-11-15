package dev.latvian.mods.vidlib.feature.structure;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class StructureRendererLevel implements BlockAndTintGetter, LightChunkGetter {
	private static final BlockState AIR = Blocks.AIR.defaultBlockState();
	private static final FluidState EMPTY_FLUID = Fluids.EMPTY.defaultFluidState();

	public final Long2ObjectMap<BlockState> blocks;
	public final int skyLight;
	public final int blockLight;
	private final Biome biome;
	private StructureRendererLevelLightEngine lightEngine;

	public StructureRendererLevel(Long2ObjectMap<BlockState> blocks, int skyLight, int blockLight, Biome biome) {
		this.blocks = blocks;
		this.skyLight = skyLight;
		this.blockLight = blockLight;
		this.biome = biome;
	}

	@Override
	public CardinalLighting cardinalLighting() {
		return CardinalLighting.DEFAULT;
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
		return colorResolver.getColor(biome, pos.getX(), pos.getZ());
	}

	@Override
	public LevelLightEngine getLightEngine() {
		if (lightEngine == null) {
			lightEngine = new StructureRendererLevelLightEngine(this, false, false);
		}

		return lightEngine;
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return blocks.getOrDefault(pos.asLong(), AIR);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		var state = blocks.get(pos.asLong());
		return state == null ? EMPTY_FLUID : state.getFluidState();
	}

	@Override
	public int getHeight() {
		return 256;
	}

	@Override
	public int getMinY() {
		return 0;
	}

	@Override
	public int getBrightness(LightLayer layer, BlockPos pos) {
		return layer == LightLayer.SKY ? skyLight : blockLight;
	}

	@Override
	public int getRawBrightness(BlockPos pos, int darkness) {
		return Math.max(blockLight, skyLight - darkness);
	}

	@Override
	public BlockGetter getLevel() {
		return this;
	}

	@Override
	@Nullable
	public LightChunk getChunkForLighting(int chunkX, int chunkZ) {
		return null;
	}
}