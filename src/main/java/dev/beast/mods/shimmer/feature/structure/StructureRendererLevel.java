package dev.beast.mods.shimmer.feature.structure;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class StructureRendererLevel implements BlockAndTintGetter {
	public final BlockAndTintGetter fallback;
	public final boolean mirror;
	public final boolean mirrorBlocks;
	public final Long2ObjectMap<BlockState> blocks;

	public StructureRendererLevel(BlockAndTintGetter fallback, boolean mirror, Long2ObjectMap<BlockState> blocks, boolean noBlocks) {
		this.fallback = fallback;
		this.mirror = mirror;
		this.mirrorBlocks = mirror && !noBlocks;
		this.blocks = blocks;
	}

	@Override
	public float getShade(Direction direction, boolean shade) {
		return fallback.getShade(direction, shade);
	}

	@Override
	public LevelLightEngine getLightEngine() {
		return fallback.getLightEngine();
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
		return mirror ? fallback.getBlockTint(pos, colorResolver) : 0xFFFFFFFF;
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return mirrorBlocks ? fallback.getBlockEntity(pos) : null;
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		var state = blocks.get(pos.asLong());
		return state == null ? mirrorBlocks ? fallback.getBlockState(pos) : Blocks.AIR.defaultBlockState() : state;
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return mirrorBlocks ? fallback.getFluidState(pos) : Fluids.EMPTY.defaultFluidState();
	}

	@Override
	public int getHeight() {
		return mirror ? fallback.getHeight() : 256;
	}

	@Override
	public int getMinBuildHeight() {
		return mirror ? fallback.getMinBuildHeight() : -128;
	}

	@Override
	public int getBrightness(LightLayer layer, BlockPos pos) {
		return mirror ? fallback.getBrightness(layer, pos) : 15;
	}

	@Override
	public int getRawBrightness(BlockPos pos, int darkness) {
		return mirror ? fallback.getRawBrightness(pos, darkness) : 15;
	}
}
