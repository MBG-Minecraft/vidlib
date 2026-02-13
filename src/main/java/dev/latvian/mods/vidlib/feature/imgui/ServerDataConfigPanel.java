package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataKeyStorage;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import net.minecraft.client.Minecraft;

public class ServerDataConfigPanel extends DataMapConfigPanel {
	public static final ServerDataConfigPanel INSTANCE = new ServerDataConfigPanel();

	private ServerDataConfigPanel() {
		super("Server Data");
	}

	@Override
	public DataKeyStorage getDataKeyStorage() {
		return DataKey.SERVER;
	}

	@Override
	public DataMap getDataMap(Minecraft mc) {
		return mc.getDataMap();
	}

	@Override
	public <T> void sendUpdate(Minecraft mc, DataKey<T> key, T value) {
		mc.updateServerDataValue(key, value);
	}
}
