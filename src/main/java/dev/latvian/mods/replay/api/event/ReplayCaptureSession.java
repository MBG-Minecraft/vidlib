package dev.latvian.mods.replay.api.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ReplayCaptureSession {
	Level getLevel();

	Player getPlayer();
}
