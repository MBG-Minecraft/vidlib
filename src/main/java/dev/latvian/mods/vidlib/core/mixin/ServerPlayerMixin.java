package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLServerPacketListener;
import dev.latvian.mods.vidlib.core.VLServerPlayer;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements VLServerPlayer {
	@Shadow
	public ServerGamePacketListenerImpl connection;

	@Override
	public ServerSessionData vl$sessionData() {
		if (connection instanceof VLServerPacketListener listener) {
			return listener.vl$sessionData();
		}

		return null;
	}
}
