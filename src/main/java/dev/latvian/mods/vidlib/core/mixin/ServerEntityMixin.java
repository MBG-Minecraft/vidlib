package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.bundle.PacketAndPayloadAcceptor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
	@Shadow
	@Final
	private Entity entity;

	@Inject(method = "sendPairingData", at = @At("RETURN"))
	private void vl$sendPairingData(ServerPlayer to, PacketAndPayloadAcceptor<ClientGamePacketListener> callback, CallbackInfo ci) {
		if (entity instanceof ServerPlayer p) {
			CommonGameEngine.INSTANCE.initialPlayerSync(p, callback);
		}
	}

	@Inject(method = "sendChanges", at = @At("RETURN"))
	private void vl$onTick(CallbackInfo ci) {
		if (entity instanceof ServerPlayer p) {
			CommonGameEngine.INSTANCE.tickPlayerSync(p);
		}
	}
}
