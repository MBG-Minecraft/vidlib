package dev.latvian.mods.vidlib.feature.session;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ClientLevelSessionData extends LevelSessionData {
	public ClientLevelSessionData(ClientWorldSessionData worldData, ResourceKey<Level> dimension) {
		super(worldData, dimension);
	}
}
