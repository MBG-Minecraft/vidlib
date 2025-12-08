package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BarrierBlock.class)
public abstract class BarrierBlockMixin extends Block {
	public BarrierBlockMixin(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
		var override = CommonGameEngine.INSTANCE.overrideBarrierShape(state, blockGetter, pos, context);

		if (override != null) {
			return override;
		}

		return Shapes.block();
	}

	@Override
	protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
}
