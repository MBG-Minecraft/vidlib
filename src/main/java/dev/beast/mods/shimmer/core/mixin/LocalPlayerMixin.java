package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerClientPacketListener;
import dev.beast.mods.shimmer.core.ShimmerClientSessionData;
import dev.beast.mods.shimmer.core.ShimmerLocalPlayer;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements ShimmerLocalPlayer {
	@Shadow
	@Final
	public ClientPacketListener connection;

	@Override
	public ShimmerClientSessionData shimmer$sessionData() {
		return ((ShimmerClientPacketListener) connection).shimmer$sessionData();
	}
}
