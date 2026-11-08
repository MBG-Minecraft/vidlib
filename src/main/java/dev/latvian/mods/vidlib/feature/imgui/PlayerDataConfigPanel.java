package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataKeyStorage;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.server.players.NameAndId;

public class PlayerDataConfigPanel extends DataMapConfigPanel<PlayerSessionData> {
	public final NameAndId profile;
	public final DataMap<PlayerSessionData> dataMap;

	public PlayerDataConfigPanel(NameAndId profile, DataMap<PlayerSessionData> dataMap) {
		super("Player Data of " + profile.name());
		this.profile = profile;
		this.dataMap = dataMap;
	}

	@Override
	public DataKeyStorage<PlayerSessionData> getDataKeyStorage() {
		return DataKey.PLAYER;
	}

	@Override
	public DataMap<PlayerSessionData> getDataMap(Minecraft mc) {
		return dataMap;
	}

	@Override
	public <T> void sendUpdate(Minecraft mc, DataKey<PlayerSessionData, T> key, T value) {
		mc.updatePlayerDataValue(profile.id(), key, value);
	}
}
