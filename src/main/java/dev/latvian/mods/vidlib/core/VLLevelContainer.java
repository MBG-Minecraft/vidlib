package dev.latvian.mods.vidlib.core;

import net.minecraft.world.level.Level;

public interface VLLevelContainer extends VLEnvironmentContainer {
	default Level vl$level() {
		throw new NoMixinException(this);
	}

	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return vl$level().getEnvironment();
	}
}
