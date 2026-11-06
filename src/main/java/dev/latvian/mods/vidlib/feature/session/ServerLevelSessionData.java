package dev.latvian.mods.vidlib.feature.session;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ServerLevelSessionData extends LevelSessionData {
	public ServerLevelSessionData(ServerWorldSessionData worldData, ResourceKey<Level> dimension) {
		super(worldData, dimension);
	}
}
