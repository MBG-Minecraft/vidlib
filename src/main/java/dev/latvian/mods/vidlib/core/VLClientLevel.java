package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.zone.ActiveZones;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.jetbrains.annotations.Nullable;

public interface VLClientLevel extends VLLevel {
	@Override
	default VLMinecraftEnvironment getEnvironment() {
		return Minecraft.getInstance();
	}

	@Override
	default ClientLevel vl$level() {
		return (ClientLevel) this;
	}

	@Override
	default ClientProps getProps() {
		throw new NoMixinException(this);
	}

	@Override
	@Nullable
	default ActiveZones vl$getActiveZones() {
		var player = Minecraft.getInstance().player;
		return player == null ? null : player.vl$sessionData().filteredZones;
	}

	@Override
	default boolean vl$isReplayLevel() {
		var mc = Minecraft.getInstance();
		return mc.player != null && mc.player.isReplayCamera();
	}
}
