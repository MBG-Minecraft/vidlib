package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.util.LevelGameTimeProvider;
import net.minecraft.world.level.Level;

public interface VLLevelContainer extends VLEnvironmentContainer, LevelGameTimeProvider {
	default Level vl$level() {
		throw new NoMixinException(this);
	}

	@Override
	default long getLevelGameTime() {
		return vl$level().getGameTime();
	}

	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return vl$level().getEnvironment();
	}
}
