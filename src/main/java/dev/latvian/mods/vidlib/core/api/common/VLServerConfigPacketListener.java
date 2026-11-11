package dev.latvian.mods.vidlib.core.api.common;

import dev.latvian.mods.klib.core.NoMixinException;
import net.minecraft.server.network.ConfigurationTask;

public interface VLServerConfigPacketListener {
	default void vl$finishTask(ConfigurationTask.Type type) {
		throw new NoMixinException(this);
	}
}
