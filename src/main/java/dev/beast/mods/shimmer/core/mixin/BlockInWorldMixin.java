package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerBlockInWorld;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockInWorld.class)
public abstract class BlockInWorldMixin implements ShimmerBlockInWorld {
	@Override
	@Accessor("state")
	public abstract void shimmer$setState(BlockState state);
}
