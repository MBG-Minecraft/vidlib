package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.math.Line;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.PlayerDataMapHolder;
import dev.latvian.mods.vidlib.feature.session.SessionData;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface VLPlayer extends VLLivingEntity, VLPlayerContainer, PlayerDataMapHolder {
	@Override
	default Player vl$self() {
		return (Player) this;
	}

	default SessionData vl$sessionData() {
		throw new NoMixinException(this);
	}

	@Override
	default boolean vl$isCreative() {
		return vl$self().isCreative();
	}

	@Override
	@Nullable
	default DataMap getDataMap() {
		var session = vl$sessionData();
		return session == null ? null : session.dataMap;
	}

	@Override
	default boolean vl$isSuspended() {
		return vl$sessionData().suspended;
	}

	@Override
	default Line ray(float delta) {
		return ray(vl$self().blockInteractionRange(), delta);
	}

	@Override
	default List<ZoneInstance> getZones() {
		return vl$sessionData().zonesIn;
	}

	default Set<String> getZoneTags() {
		return vl$sessionData().zonesTagsIn;
	}

	default boolean isReplayCamera() {
		return false;
	}
}
