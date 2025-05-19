package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLClientPlayer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.level.GameType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin implements VLClientPlayer {
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
