package dev.latvian.mods.vidlib.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

public interface VLClientLevel extends VLLevel {
	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return Minecraft.getInstance();
	}

	@Override
	default ClientLevel vl$level() {
		return (ClientLevel) this;
	}
}
