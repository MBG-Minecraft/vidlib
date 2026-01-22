package dev.latvian.mods.vidlib.core;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface VLClipContext {
	@Nullable
	default Entity vl$getEntity() {
		return null;
	}
}
