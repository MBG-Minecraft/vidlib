package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataKeyStorage;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import net.minecraft.client.Minecraft;

public class PlayerDataConfigPanel extends DataMapConfigPanel {
	public final DataMap dataMap;

	public PlayerDataConfigPanel(String name, DataMap dataMap) {
		super("Player Data of " + name);
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
	}
}
