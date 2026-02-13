package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.core.VLClientPlayPacketListener;
import dev.latvian.mods.vidlib.core.VLLocalPlayer;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.item.ItemScreen;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import dev.latvian.mods.vidlib.feature.session.LocalClientSessionData;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements VLLocalPlayer {
	@Shadow
	@Final
	public ClientPacketListener connection;

	@Shadow
	@Final
	protected Minecraft minecraft;

	@Unique
	private LocalClientSessionData vl$sessionData;

	@Unique
	private Boolean vl$isReplayCamera;

	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}

	@Override
	public LocalClientSessionData vl$sessionData() {
		if (vl$sessionData == null && connection instanceof VLClientPlayPacketListener listener) {
			vl$sessionData = listener.vl$sessionData();
		}

		return vl$sessionData;
	}

	@Override
	public Set<String> getTags() {
		return vl$sessionData().getTags();
	}

	@Inject(method = "openItemGui", at = @At("HEAD"), cancellable = true)
	private void vl$openItemGui(ItemStack stack, InteractionHand hand, CallbackInfo ci) {
		var s = ItemScreen.create((LocalPlayer) (Object) this, stack, hand);

		if (s != null) {
			minecraft.setScreen(s);
			ci.cancel();
		}
	}

	@Override
	public boolean isReplayCamera() {
		if (vl$isReplayCamera == null) {
			vl$isReplayCamera = ((Object) this).getClass() != LocalPlayer.class || getScoreboardName().equals("Replay Viewer") || getGameProfile().getProperties().containsKey("IsReplayViewer");
		}

		return vl$isReplayCamera;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getFlyingSpeed()F"))
	private float vl$getFlyingSpeed(float original) {
		return original * CommonGameEngine.INSTANCE.getFlightSpeedModifier(vl$self());
	}

	@Inject(method = "drop", at = @At("HEAD"), cancellable = true)
	private void vl$drop(boolean fullStack, CallbackInfoReturnable<Boolean> cir) {
		if (PlayerActionHandler.handle(this, PlayerActionType.DROP, true)) {
			cir.setReturnValue(false);
		}
	}
}
