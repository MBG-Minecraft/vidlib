package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerRemotePlayer;
import dev.beast.mods.shimmer.feature.session.ShimmerRemoteClientSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(RemotePlayer.class)
public class RemotePlayerMixin implements ShimmerRemotePlayer {
	@Unique
	private ShimmerRemoteClientSessionData shimmer$sessionData;

	@Override
	public ShimmerRemoteClientSessionData shimmer$sessionData() {
		if (shimmer$sessionData == null) {
			shimmer$sessionData = Minecraft.getInstance().player.shimmer$sessionData().getRemoteSessionData(((RemotePlayer) (Object) this).getUUID());
		}

		return shimmer$sessionData;
	}
}
