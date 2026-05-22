package dev.latvian.mods.vidlib.feature.data;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

public interface DataMapHolder {
	@Nullable
	DataMap getDataMap();

	@Nullable
	default <T> T getOptional(DataKey<T> type) {
		var dataMap = getDataMap();
		var value = dataMap == null ? null : dataMap.get(type);
		return value == null ? type.defaultValue() : value;
	}

	default <T> T get(DataKey<T> type) {
		var dataMap = getDataMap();
		var value = dataMap == null ? null : dataMap.get(type);
		return value == null ? type.defaultValue() : value;
	}

	default <T> void set(DataKey<T> type, T value) {
		var dataMap = getDataMap();

		if (dataMap != null) {
			dataMap.set(type, value);
		}
	}

	default <T> void reset(DataKey<T> type) {
		set(type, type.defaultValue());
	}

	// Overridden on Bukkit
	default DynamicOps<Tag> getNbtOps(MinecraftServer server) {
		return server.nbtOps();
	}
}
