package dev.latvian.mods.vidlib.core.mixin;

import com.mojang.authlib.GameProfile;
import dev.latvian.mods.vidlib.core.VLServerPlayer;
import dev.latvian.mods.vidlib.feature.session.ServerSessionData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements VLServerPlayer {
	@Shadow
	public ServerGamePacketListenerImpl connection;

	@Unique
	private ServerSessionData vl$sessionData;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void vl$init(MinecraftServer server, ServerLevel level, GameProfile gameProfile, ClientInformation clientInformation, CallbackInfo ci) {
		vl$sessionData = server.vl$getOrLoadServerSession(gameProfile.getId());
	}

	@Override
	public ServerSessionData vl$sessionData() {
		return vl$sessionData;
	}

	@Redirect(method = "startSleepInBed", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;displayClientMessage(Lnet/minecraft/network/chat/Component;Z)V"))
	private void vl$sleepMessage(ServerPlayer instance, Component text, boolean overlay) {
	}
}
