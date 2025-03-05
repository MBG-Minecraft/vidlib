package dev.beast.mods.shimmer.feature.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class StructureRendererLevelLightEngine extends LevelLightEngine {
	public StructureRendererLevelLightEngine(StructureRendererLevel lightChunkGetter, boolean blockLight, boolean skyLight) {
		super(lightChunkGetter, blockLight, skyLight);
	}

	@Override
	public int getRawBrightness(BlockPos pos, int amount) {
		return ((StructureRendererLevel) levelHeightAccessor).getRawBrightness(pos, amount);
	}
}
