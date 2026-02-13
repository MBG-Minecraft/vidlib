package dev.latvian.mods.vidlib.core;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.ServerDataMapHolder;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

public interface VLMinecraftEnvironmentDataHolder extends VLLevelContainer, ServerDataMapHolder {
	@Override
	default DataMap getDataMap() {
		throw new NoMixinException(this);
	}

	default DynamicOps<Tag> nbtOps() {
		var level = vl$level();
		return level == null ? NbtOps.INSTANCE : level.nbtOps();
	}

	default DynamicOps<JsonElement> jsonOps() {
		var level = vl$level();
		return level == null ? JsonOps.INSTANCE : level.jsonOps();
	}

	default FeatureSet getServerFeatures() {
		throw new NoMixinException(this);
	}
}
