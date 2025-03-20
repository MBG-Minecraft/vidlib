package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.InternalServerData;
import net.minecraft.resources.ResourceLocation;

public interface ShimmerMinecraftEnvironmentDataHolder {
	default DataMap getServerData() {
		throw new NoMixinException();
	}

	default ResourceLocation getSkybox() {
		return getServerData().get(InternalServerData.SKYBOX);
	}

	default void setSkybox(ResourceLocation skybox) {
		getServerData().set(InternalServerData.SKYBOX, skybox);
	}
}
