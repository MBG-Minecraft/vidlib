package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.util.NameDrawType;
import net.minecraft.resources.ResourceLocation;

public interface VLMinecraftEnvironmentDataHolder extends VLLevelContainer {
	default DataMap getServerData() {
		throw new NoMixinException(this);
	}

	default long getGameTime() {
		return vl$level().getGameTime();
	}

	default FeatureSet getServerFeatures() {
		throw new NoMixinException(this);
	}

	default <T> T get(DataKey<T> type) {
		return getServerData().get(type, getGameTime());
	}

	default <T> void set(DataKey<T> type, T value) {
		getServerData().set(type, value);
	}

	default <T> void reset(DataKey<T> type) {
		getServerData().reset(type);
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

	default NameDrawType getNameDrawType() {
		return get(InternalServerData.NAME_DRAW_TYPE);
	}

	default void setNameDrawType(NameDrawType type) {
		set(InternalServerData.NAME_DRAW_TYPE, type);
	}
}
