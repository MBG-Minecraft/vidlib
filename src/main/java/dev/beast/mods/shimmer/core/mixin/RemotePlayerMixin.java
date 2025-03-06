package dev.beast.mods.shimmer.core.mixin;

import com.mojang.authlib.GameProfile;
import dev.beast.mods.shimmer.core.ShimmerRemotePlayer;
import dev.beast.mods.shimmer.feature.session.ShimmerRemoteClientSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

@Mixin(RemotePlayer.class)
public class RemotePlayerMixin extends AbstractClientPlayer implements ShimmerRemotePlayer {
	@Unique
	private ShimmerRemoteClientSessionData shimmer$sessionData;

	public RemotePlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Override
	public ShimmerRemoteClientSessionData shimmer$sessionData() {
		if (shimmer$sessionData == null) {
			shimmer$sessionData = Minecraft.getInstance().player.shimmer$sessionData().getRemoteSessionData(getUUID());
		}

		return shimmer$sessionData;
	}

	@Override
	public Set<String> getTags() {
		return shimmer$sessionData().tags;
	}
}
