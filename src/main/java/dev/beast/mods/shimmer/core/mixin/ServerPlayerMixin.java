package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerServerPacketListener;
import dev.beast.mods.shimmer.core.ShimmerServerPlayer;
import dev.beast.mods.shimmer.feature.session.ShimmerServerSessionData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements ShimmerServerPlayer {
	@Shadow
	public ServerGamePacketListenerImpl connection;

	@Override
	public ShimmerServerSessionData shimmer$sessionData() {
		if (connection instanceof ShimmerServerPacketListener listener) {
			return listener.shimmer$sessionData();
		}

		return null;
	}
}
