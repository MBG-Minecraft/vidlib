package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLBlockInWorld;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockInWorld.class)
public abstract class BlockInWorldMixin implements VLBlockInWorld {
	@Override
	@Accessor("state")
	public abstract void vl$setState(BlockState state);
}
