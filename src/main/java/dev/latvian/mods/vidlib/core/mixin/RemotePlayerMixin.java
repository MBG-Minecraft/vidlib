package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.core.VLRemotePlayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RemotePlayer.class)
public class RemotePlayerMixin extends AbstractClientPlayer implements VLRemotePlayer {
	public RemotePlayerMixin(ClientLevel clientLevel, NameAndId NameAndId) {
		super(clientLevel, new GameProfile(NameAndId.id(), NameAndId.name()));
	}
}
