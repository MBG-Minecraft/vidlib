package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.ServerDataMapHolder;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;

public interface VLMinecraftEnvironmentDataHolder extends VLLevelContainer, ServerDataMapHolder {
	@Override
	default DataMap getDataMap() {
		throw new NoMixinException(this);
	}

	default FeatureSet getServerFeatures() {
		throw new NoMixinException(this);
	}
}
