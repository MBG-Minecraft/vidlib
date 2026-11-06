package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLRemotePlayer;
import dev.latvian.mods.vidlib.feature.session.RemoteClientPlayerSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;

@Mixin(RemotePlayer.class)
public class RemotePlayerMixin extends AbstractClientPlayer implements VLRemotePlayer {
	@Unique
	private RemoteClientPlayerSessionData vl$sessionData;

	public RemotePlayerMixin(ClientLevel clientLevel, NameAndId NameAndId) {
		super(clientLevel, new com.mojang.authlib.GameProfile(NameAndId.id(), NameAndId.name()));
	}

	@Override
	public RemoteClientPlayerSessionData vl$sessionData() {
		if (vl$sessionData == null) {
			vl$sessionData = Minecraft.getInstance().player.vl$sessionData().getRemoteSessionData(getUUID());
		}

		return vl$sessionData;
	}

	@Override
	public Set<String> entityTags() {
		return vl$sessionData().getTags();
	}
}
