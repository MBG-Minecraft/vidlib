package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.vidlib.feature.prop.ClientProps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class ClientLevelSessionData extends LevelSessionData<ClientProps> {
	public static ClientLevelSessionData of(Level level) {
		return (ClientLevelSessionData) LevelSessionData.of(level);
	}

	public final ClientSessionData globalData;

	public ClientLevelSessionData(ClientSessionData globalData, ResourceKey<Level> dimension) {
		super(globalData, dimension);
		this.globalData = globalData;
	}

	@Override
	public KNumberContext createGlobalContext() {
		var ctx = new KNumberContext(globalData.globalVariables);
		ctx.updateLevelData(globalData.mc.level);
		ctx.entity = globalData.mc.player;
		return ctx;
	}
}
