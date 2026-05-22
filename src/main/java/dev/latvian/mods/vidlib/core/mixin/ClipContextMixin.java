package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLClipContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClipContext.class)
public class ClipContextMixin implements VLClipContext {
	@Shadow
	@Final
	public CollisionContext collisionContext;

	@Override
	@Nullable
	public Entity vl$getEntity() {
		return collisionContext instanceof EntityCollisionContext ctx ? ctx.getEntity() : null;
	}
}
