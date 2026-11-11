package dev.latvian.mods.vidlib.core.api.common;

import net.minecraft.server.level.ServerPlayer;

public interface VLServerPlayer extends VLPlayer {
	@Override
	default ServerPlayer vl$self() {
		return (ServerPlayer) this;
	}
}
