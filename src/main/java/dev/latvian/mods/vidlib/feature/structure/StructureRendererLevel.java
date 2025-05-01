package dev.latvian.mods.vidlib.feature.structure;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class StructureRendererLevel implements BlockAndTintGetter, LightChunkGetter {
	public final Long2ObjectMap<BlockState> blocks;
	private StructureRendererLevelLightEngine lightEngine;
	public final int skyLight;
	public final int blockLight;
	private final BlockState airBlock;
	private final FluidState emptyFluid;
	private final Biome biome;

	public StructureRendererLevel(Long2ObjectMap<BlockState> blocks, int skyLight, int blockLight, Biome biome) {
		this.blocks = blocks;
		this.skyLight = skyLight;
		this.blockLight = blockLight;
		this.airBlock = Blocks.AIR.defaultBlockState();
		this.emptyFluid = Fluids.EMPTY.defaultFluidState();
		this.biome = biome;
	}

	@Override
	public float getShade(Direction direction, boolean shade) {
		return shade ? switch (direction) {
			case DOWN -> 0.5F;
			case NORTH, SOUTH -> 0.8F;
			case WEST, EAST -> 0.6F;
			default -> 1F;
		} : 1F;
	}

	@Override
	public LevelLightEngine getLightEngine() {
		if (lightEngine == null) {
			lightEngine = new StructureRendererLevelLightEngine(this, false, false);
		}

		return lightEngine;
	}

	@Override
	public int getBlockTint(BlockPos pos, ColorResolver colorResolver) {
		return colorResolver.getColor(biome, pos.getX(), pos.getZ());
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity(BlockPos pos) {
		return null;
	}

	@Override
	public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> blockEntityType) {
		return Optional.empty();
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return blocks.getOrDefault(pos.asLong(), airBlock);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		var b = blocks.get(pos.asLong());
		return b == null ? emptyFluid : b.getFluidState();
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
