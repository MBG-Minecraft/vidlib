package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.prop.ServerProps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class ServerLevelSessionData extends LevelSessionData<ServerProps> {
	public static ServerLevelSessionData of(Level level) {
		return (ServerLevelSessionData) LevelSessionData.of(level);
	}

	public final ServerLevel level;

	public ServerLevelSessionData(ServerWorldSessionData worldData, ServerLevel level) {
		super(worldData, level.dimension());
		this.level = level;
	}

	@Override
	public KNumberContext createGlobalContext() {
		var ctx = new KNumberContext(worldData.globalVariables);
		ctx.updateLevelData(level);
		return ctx;
	}
}
