package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.InternalServerData;
import dev.beast.mods.shimmer.feature.zone.Anchor;
import net.minecraft.resources.ResourceLocation;

public interface ShimmerMinecraftEnvironmentDataHolder {
	default DataMap getServerData() {
		throw new NoMixinException(this);
	}

	default ResourceLocation getSkybox() {
		return getServerData().get(InternalServerData.SKYBOX);
	}

	default void setSkybox(ResourceLocation skybox) {
		getServerData().set(InternalServerData.SKYBOX, skybox);
	}

	default Anchor getAnchor() {
		return getServerData().get(InternalServerData.ANCHOR);
	}

	default void setAnchor(Anchor anchor) {
		getServerData().set(InternalServerData.ANCHOR, anchor);
	}
}
