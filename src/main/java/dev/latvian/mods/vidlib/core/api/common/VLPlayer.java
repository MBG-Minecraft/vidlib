package dev.latvian.mods.vidlib.core.api.common;

import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import net.minecraft.world.entity.player.Player;

public interface VLPlayer extends VLLivingEntity {
	@Override
	default Player vl$self() {
		return (Player) this;
	}

	default PlayerSessionData vl$getPlayerData() {
		throw new NoMixinException(this);
	}
}
