package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataKeyStorage;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;
import net.minecraft.client.Minecraft;

public class ServerDataConfigPanel extends DataMapConfigPanel<GlobalSessionData> {
	public static final ServerDataConfigPanel INSTANCE = new ServerDataConfigPanel();

	private ServerDataConfigPanel() {
		super("Server Data");
	}

	@Override
	public DataKeyStorage<GlobalSessionData> getDataKeyStorage() {
		return DataKey.GLOBAL;
	}

	@Override
	public DataMap<GlobalSessionData> getDataMap(Minecraft mc) {
		return ClientSessionData.of(mc).dataMap;
	}

	@Override
	public <T> void sendUpdate(Minecraft mc, DataKey<GlobalSessionData, T> key, T value) {
		mc.updateServerDataValue(key, value);
	}
}
