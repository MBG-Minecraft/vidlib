package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataKeyStorage;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class LevelDataConfigPanel extends DataMapConfigPanel<LevelSessionData<?>> {
	public final ResourceKey<Level> dimension;
	public final DataMap<LevelSessionData<?>> dataMap;

	public LevelDataConfigPanel(ResourceKey<Level> dimension, DataMap<LevelSessionData<?>> dataMap) {
		super("Level Data of " + dimension.identifier());
		this.dimension = dimension;
		this.dataMap = dataMap;
	}

	@Override
	public DataKeyStorage<LevelSessionData<?>> getDataKeyStorage() {
		return DataKey.LEVEL;
	}

	@Override
	public DataMap<LevelSessionData<?>> getDataMap(Minecraft mc) {
		return dataMap;
	}

	@Override
	public <T> void sendUpdate(Minecraft mc, DataKey<LevelSessionData<?>, T> key, T value) {
		mc.updateLevelDataValue(dimension, key, value);
	}
}
