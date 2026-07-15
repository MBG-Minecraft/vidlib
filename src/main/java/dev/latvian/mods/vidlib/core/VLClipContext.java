package dev.latvian.mods.vidlib.core;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import org.jetbrains.annotations.Nullable;

public interface VLClipContext {
	default ClipContext.Block vl$getBlock() {
		throw new NoMixinException(this);
	}

	default ClipContext.Fluid vl$getFluid() {
		throw new NoMixinException(this);
	}

	default CollisionContext vl$getCollisionContext() {
		throw new NoMixinException(this);
	}

	@Nullable
	default Entity vl$getEntity() {
		return vl$getCollisionContext() instanceof EntityCollisionContext ctx ? ctx.getEntity() : null;
	}
}
