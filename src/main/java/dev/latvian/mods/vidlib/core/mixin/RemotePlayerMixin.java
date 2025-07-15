package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.core.VLRemotePlayer;
import dev.latvian.mods.vidlib.feature.session.RemoteClientSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

@Mixin(RemotePlayer.class)
public class RemotePlayerMixin extends AbstractClientPlayer implements VLRemotePlayer {
	@Unique
	private RemoteClientSessionData vl$sessionData;

	public RemotePlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Override
	public RemoteClientSessionData vl$sessionData() {
		if (vl$sessionData == null) {
			vl$sessionData = Minecraft.getInstance().player.vl$sessionData().getRemoteSessionData(getUUID());
		}

		return vl$sessionData;
	}

	@Override
	public Set<String> getTags() {
		return vl$sessionData().getTags(vl$level().getGameTime());
	}
}
