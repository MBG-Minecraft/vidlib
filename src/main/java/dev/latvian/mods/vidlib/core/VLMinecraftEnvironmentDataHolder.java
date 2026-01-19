package dev.latvian.mods.vidlib.core;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.zone.Anchor;
import dev.latvian.mods.vidlib.util.NameDrawType;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface VLMinecraftEnvironmentDataHolder extends VLLevelContainer {
	default DataMap getServerData() {
		throw new NoMixinException(this);
	}

	default long getGameTime() {
		return vl$level().getGameTime();
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

	@Nullable
	default <T> T getOptional(DataKey<T> type) {
		return getServerData().get(type, getGameTime());
	}

	default <T> T get(DataKey<T> type) {
		var value = getOptional(type);
		return value == null ? type.defaultValue() : value;
	}

	default <T> void set(DataKey<T> type, T value) {
		getServerData().set(type, value);
	}

	default <T> void reset(DataKey<T> type) {
		getServerData().reset(type);
	}

	default ResourceLocation getSkybox() {
		return getOptional(InternalServerData.SKYBOX);
	}

	default void setSkybox(ResourceLocation skybox) {
		set(InternalServerData.SKYBOX, skybox);
	}

	default Anchor getAnchor() {
		return get(InternalServerData.ANCHOR);
	}

	default void setAnchor(Anchor anchor) {
		set(InternalServerData.ANCHOR, anchor);
	}

	default NameDrawType getNameDrawType() {
		return getOptional(InternalServerData.NAME_DRAW_TYPE);
	}

	default void setNameDrawType(NameDrawType type) {
		set(InternalServerData.NAME_DRAW_TYPE, type);
	}
}
