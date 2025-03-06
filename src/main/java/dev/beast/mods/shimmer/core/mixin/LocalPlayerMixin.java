package dev.beast.mods.shimmer.core.mixin;

import com.mojang.authlib.GameProfile;
import dev.beast.mods.shimmer.core.ShimmerClientPacketListener;
import dev.beast.mods.shimmer.core.ShimmerLocalPlayer;
import dev.beast.mods.shimmer.feature.session.ShimmerLocalClientSessionData;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements ShimmerLocalPlayer {
	@Shadow
	@Final
	public ClientPacketListener connection;

	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Override
	public ShimmerLocalClientSessionData shimmer$sessionData() {
		return ((ShimmerClientPacketListener) connection).shimmer$sessionData();
	}

	@Override
	public Set<String> getTags() {
		return shimmer$sessionData().tags;
	}
}
