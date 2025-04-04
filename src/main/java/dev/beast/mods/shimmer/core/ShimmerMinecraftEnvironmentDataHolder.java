package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.data.InternalServerData;
import dev.beast.mods.shimmer.feature.zone.Anchor;
import net.minecraft.resources.ResourceLocation;

public interface ShimmerMinecraftEnvironmentDataHolder {
	default DataMap getServerData() {
		throw new NoMixinException(this);
	}

	default <T> T get(DataType<T> type) {
		return getServerData().get(type);
	}

	default <T> void set(DataType<T> type, T value) {
		getServerData().set(type, value);
	}

	default ResourceLocation getSkybox() {
		return get(InternalServerData.SKYBOX);
	}

	default void setSkybox(ResourceLocation skybox) {
		set(InternalServerData.SKYBOX, skybox);
	}

	default boolean isImmutableWorld() {
		return get(InternalServerData.IMMUTABLE_WORLD);
	}

	default void setImmutableWorld(boolean immutable) {
		set(InternalServerData.IMMUTABLE_WORLD, immutable);
	}

	default Anchor getAnchor() {
		return get(InternalServerData.ANCHOR);
	}

	default void setAnchor(Anchor anchor) {
		set(InternalServerData.ANCHOR, anchor);
	}
}
