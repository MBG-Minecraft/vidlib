package dev.latvian.mods.replay.api.event;

import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.replay.api.ReplayMarkerType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ReplayCaptureSession {
	Level getLevel();

	Player getPlayer();

	default void addMarker(ReplayMarkerType type, ReplayMarkerData data) {
	}
}
