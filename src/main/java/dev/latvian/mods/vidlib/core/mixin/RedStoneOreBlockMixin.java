package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RedStoneOreBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(RedStoneOreBlock.class)
public class RedStoneOreBlockMixin {
	/**
	 * @author Lat
	 * @reason MBG
	 */
	@Overwrite
	private static void interact(BlockState state, Level level, BlockPos pos) {
	}
}
