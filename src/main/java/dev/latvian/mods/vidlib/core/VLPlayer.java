package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.PlayerDataMapHolder;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface VLPlayer extends VLLivingEntity, VLPlayerContainer, PlayerDataMapHolder {
	@Override
	default Player vl$self() {
		return (Player) this;
	}

	default PlayerSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}

	@Override
	@Nullable
	default DataMap getDataMap() {
		var session = vl$sessionData();
		return session == null ? null : session.dataMap;
	}

	default boolean isReplayCamera() {
		return false;
	}
}
