package dev.latvian.mods.replay.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public interface ReplaySession {
	@Nullable
	<T extends ReplaySessionData> T getData(Class<T> type);

	ReplayFileInfo getFileInfo();

	Level getLevel();

	default RegistryAccess getRegistryAccess() {
		return RegistryAccess.EMPTY;
	}
}
