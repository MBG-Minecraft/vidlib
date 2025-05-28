package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import net.minecraft.resources.ResourceLocation;

public interface VLMinecraftEnvironmentDataHolder extends VLLevelContainer {
	default DataMap getServerData() {
		throw new NoMixinException(this);
	}

	default <T> T get(DataKey<T> type) {
		return getServerData().get(type, vl$level().getGameTime());
	}

	default <T> void set(DataKey<T> type, T value) {
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
