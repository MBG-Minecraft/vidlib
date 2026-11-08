package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ClientLevelSessionData extends LevelSessionData<ClientProps> {
	public static ClientLevelSessionData of(Level level) {
		return (ClientLevelSessionData) LevelSessionData.of(level);
	}

	public final ClientSessionData worldData;

	public ClientLevelSessionData(ClientSessionData worldData, ResourceKey<Level> dimension) {
		super(worldData, dimension);
		this.worldData = worldData;
	}

	@Override
	public KNumberContext createGlobalContext() {
		var ctx = new KNumberContext(worldData.globalVariables);
		ctx.updateLevelData(worldData.mc.level);
		ctx.entity = worldData.mc.player;
		return ctx;
	}
}
