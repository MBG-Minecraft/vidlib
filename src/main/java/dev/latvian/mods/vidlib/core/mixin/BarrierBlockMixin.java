package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BarrierBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BarrierBlock.class)
public abstract class BarrierBlockMixin extends Block {
	@Override
	@Shadow
	protected abstract RenderShape getRenderShape(BlockState state);

	public BarrierBlockMixin(Properties properties) {
		super(properties);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext ctx && ctx.getEntity() != null) {
			var v = EntityOverride.PASS_THROUGH_BARRIERS.get(ctx.getEntity());

			if (v == null ? ctx.getEntity().vl$isCreative() : v) {
				return Shapes.empty();
			}
		}

		return Shapes.block();
	}

	@Override
	protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
}
