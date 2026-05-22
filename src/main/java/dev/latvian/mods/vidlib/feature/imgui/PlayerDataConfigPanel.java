package dev.latvian.mods.vidlib.feature.imgui;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataKeyStorage;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import net.minecraft.client.Minecraft;

public class PlayerDataConfigPanel extends DataMapConfigPanel {
	public final GameProfile profile;
	public final DataMap dataMap;

	public PlayerDataConfigPanel(GameProfile profile, DataMap dataMap) {
		super("Player Data of " + profile.getName());
		this.profile = profile;
		this.dataMap = dataMap;
	}

	@Override
	public DataKeyStorage getDataKeyStorage() {
		return DataKey.PLAYER;
	}

	@Override
	public DataMap getDataMap(Minecraft mc) {
		return dataMap;
	}

	@Override
	public <T> void sendUpdate(Minecraft mc, DataKey<T> key, T value) {
		mc.updatePlayerDataValue(profile.getId(), key, value);
	}
}
