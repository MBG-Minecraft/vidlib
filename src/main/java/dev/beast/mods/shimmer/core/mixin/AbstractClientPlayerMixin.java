package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerClientPlayer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin implements ShimmerClientPlayer {
	@Shadow
	@Nullable
	protected abstract PlayerInfo getPlayerInfo();

	@Override
	@Nullable
	public GameType getGameMode() {
		var info = getPlayerInfo();
		return info == null ? null : info.getGameMode();
	}
}
