package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLClipContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClipContext.class)
public abstract class ClipContextMixin implements VLClipContext {
	@Override
	@Accessor("block")
	public abstract ClipContext.Block vl$getBlock();

	@Override
	@Accessor("fluid")
	public abstract ClipContext.Fluid vl$getFluid();

	@Override
	@Accessor("collisionContext")
	public abstract CollisionContext vl$getCollisionContext();
}
