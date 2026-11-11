package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.core.api.client.VLClientConfigPacketListener;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
	@Shadow
	private ClientLevel level;

	protected ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie cookie) {
		super(minecraft, connection, cookie);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void vl$init(Minecraft mc, Connection connection, CommonListenerCookie cookie, CallbackInfo ci) {
		if (connection.getPacketListener() instanceof VLClientConfigPacketListener config) {
			mc.vl$setGlobalData((ClientSessionData) config.vl$getGlobalData());
		}
	}

	@ModifyExpressionValue(method = {"handleRespawn", "handleLogin"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/CommonPlayerSpawnInfo;isFlat()Z"))
	private boolean vl$isFlat(boolean original, @Local CommonPlayerSpawnInfo info) {
		return true;
	}

	@Inject(method = "handleLogin", at = @At("RETURN"))
	private void vl$handleLogin(CallbackInfo ci) {
		ClientSessionData.of(Minecraft.getInstance()).localPlayer.respawned(level, true);
	}

	@Inject(method = "handleRespawn", at = @At("RETURN"))
	private void vl$handleRespawn(CallbackInfo ci) {
		ClientSessionData.of(Minecraft.getInstance()).localPlayer.respawned(level, false);
	}

	@ModifyExpressionValue(method = "handleSetEntityPassengersPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;[Ljava/lang/Object;)Lnet/minecraft/network/chat/MutableComponent;"))
	private MutableComponent vl$setOverlayMessage(MutableComponent original, @Local(ordinal = 0) Entity entity) {
		var customMessage = entity.getCustomMountMessage();
		return customMessage == null ? original : (MutableComponent) customMessage;
	}
}
