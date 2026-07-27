package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import dev.latvian.mods.vidlib.feature.zone.ZoneCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.clock.ClockNetworkState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

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
	default ZoneCache vl$getActiveZones() {
		var player = Minecraft.getInstance().player;
		return player == null ? null : player.vl$sessionData().filteredZones;
	}

	@Override
	default boolean isReplayLevel() {
		var mc = Minecraft.getInstance();
		return mc.player != null && mc.player.isReplayCamera();
	}

	@Override
	default void vl$setDayTime(long time) {
		vl$level().dimensionType().defaultClock().ifPresent(clock -> vl$level().clockManager().handleUpdates(vl$level().getGameTime(), Map.of(clock, new ClockNetworkState(time, 0F, 1F))));
	}
}
