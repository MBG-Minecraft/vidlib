package dev.beast.mods.shimmer.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import dev.beast.mods.shimmer.core.ShimmerClientPacketListener;
import dev.beast.mods.shimmer.core.ShimmerLocalPlayer;
import dev.beast.mods.shimmer.feature.item.ItemScreen;
import dev.beast.mods.shimmer.feature.session.ShimmerLocalClientSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements ShimmerLocalPlayer {
	@Shadow
	@Final
	public ClientPacketListener connection;

	@Shadow
	@Final
	protected Minecraft minecraft;

	@Unique
	private final boolean shimmer$isReplayCamera;

	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
		this.shimmer$isReplayCamera = ((Object) this).getClass() != LocalPlayer.class || gameProfile.getName().equals("Replay Viewer");
	}

	@Override
	public ShimmerLocalClientSessionData shimmer$sessionData() {
		return ((ShimmerClientPacketListener) connection).shimmer$sessionData();
	}

	@Override
	public Set<String> getTags() {
		return shimmer$sessionData().tags;
	}

	@Inject(method = "openItemGui", at = @At("HEAD"), cancellable = true)
	private void shimmer$openItemGui(ItemStack stack, InteractionHand hand, CallbackInfo ci) {
		var s = ItemScreen.create((LocalPlayer) (Object) this, stack, hand);

		if (s != null) {
			minecraft.setScreen(s);
			ci.cancel();
		}
	}

	@Override
	public boolean isReplayCamera() {
		return shimmer$isReplayCamera;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getFlyingSpeed()F"))
	private float shimmer$getFlyingSpeed(float original) {
		return original * getFlightSpeedMod();
	}
}
