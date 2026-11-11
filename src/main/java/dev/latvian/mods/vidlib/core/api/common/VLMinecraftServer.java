package dev.latvian.mods.vidlib.core.api.common;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import org.jetbrains.annotations.Nullable;

public interface VLMinecraftServer {
	@Nullable
	default ServerSessionData vl$getGlobalData() {
		throw new NoMixinException(this);
	}
}
