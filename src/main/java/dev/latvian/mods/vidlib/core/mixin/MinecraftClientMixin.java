package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.core.VLMinecraftClient;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin implements VLMinecraftClient {
	@Shadow
	@Nullable
	public LocalPlayer player;

	@Unique
	private final Map<String, GameProfile> vl$profileByNameCache = new HashMap<>();

	@Unique
	private final Map<UUID, GameProfile> vl$profileByUUIDCache = new HashMap<>();

	@Override
	public GameProfile retrieveGameProfile(UUID uuid) {
		return vl$profileByUUIDCache.computeIfAbsent(uuid, VLMinecraftClient.super::retrieveGameProfile);
	}

	@Override
	public GameProfile retrieveGameProfile(String name) {
		return vl$profileByNameCache.computeIfAbsent(name, VLMinecraftClient.super::retrieveGameProfile);
	}

	@Override
	public void vl$clearProfileCache() {
		vl$profileByUUIDCache.clear();
		vl$profileByNameCache.clear();
	}

	@Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"))
	private void vl$swap(ClientPacketListener instance, Packet packet) {
		if (!PlayerActionHandler.handle(player, PlayerActionType.SWAP, true)) {
			instance.send(packet);
		}
	}

	@Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
	private void vl$startAttack(CallbackInfoReturnable<Boolean> cir) {
		if (PlayerActionHandler.handle(player, PlayerActionType.ATTACK, true)) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "continueAttack", at = @At("HEAD"), cancellable = true)
	private void vl$continueAttack(boolean leftClick, CallbackInfo ci) {
		if (PlayerActionHandler.handle(player, PlayerActionType.ATTACK, false)) {
			ci.cancel();
		}
	}

	@Inject(method = "startUseItem", at = @At("HEAD"), cancellable = true)
	private void vl$startUseItem(CallbackInfo ci) {
		if (PlayerActionHandler.handle(player, PlayerActionType.INTERACT, true)) {
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 1))
	private boolean vl$handleKeybindsUsingItem(boolean original) {
		return original || PlayerActionHandler.handle(player, PlayerActionType.INTERACT, false);

	}
}
