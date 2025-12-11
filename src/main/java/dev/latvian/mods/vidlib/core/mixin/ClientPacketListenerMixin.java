package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.VLClientConfigPacketListener;
import dev.latvian.mods.vidlib.core.VLClientPlayPacketListener;
import dev.latvian.mods.vidlib.feature.entity.ExactEntitySpawnPayload;
import dev.latvian.mods.vidlib.feature.net.Context;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.OptionalInt;
import java.util.UUID;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements VLClientPlayPacketListener {
	@Shadow
	private ClientLevel level;

	@Shadow
	private OptionalInt removedPlayerVehicleId;

	@Shadow
	@Nullable
	public abstract PlayerInfo getPlayerInfo(UUID uniqueId);

	@Inject(method = "<init>", at = @At("RETURN"))
	private void vl$init(Minecraft mc, Connection connection, CommonListenerCookie cookie, CallbackInfo ci) {
		if (connection.getPacketListener() instanceof VLClientConfigPacketListener config) {
			config.vl$transfer(this, connection.getPacketListener());
		}
	}

	@ModifyExpressionValue(method = {"handleRespawn", "handleLogin"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/CommonPlayerSpawnInfo;isFlat()Z"))
	private boolean vl$isFlat(boolean original, @Local CommonPlayerSpawnInfo info) {
		return true;
	}

	@Override
	public Entity vl$addEntity(Context ctx, ExactEntitySpawnPayload payload) {
		if (removedPlayerVehicleId.isPresent() && removedPlayerVehicleId.getAsInt() == payload.id()) {
			removedPlayerVehicleId = OptionalInt.empty();
		}

		Entity entity;

		if (payload.type() == EntityType.PLAYER) {
			var playerinfo = getPlayerInfo(payload.uuid());

			if (playerinfo == null) {
				VidLib.LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", payload.uuid());
				return null;
			} else {
				entity = new RemotePlayer(level, playerinfo.getProfile());
			}
		} else {
			entity = payload.type().create(ctx.level(), EntitySpawnReason.LOAD);
		}

		if (entity != null) {
			payload.update(entity);
			level.addEntity(entity);
			return entity;
		} else {
			VidLib.LOGGER.warn("Skipping Entity with id {}", payload.type());
			return null;
		}
	}

	@Inject(method = "handleLogin", at = @At("RETURN"))
	private void vl$handleLogin(CallbackInfo ci) {
		vl$sessionData().respawned(level, true);
	}

	@Inject(method = "handleRespawn", at = @At("RETURN"))
	private void vl$handleRespawn(CallbackInfo ci) {
		vl$sessionData().respawned(level, false);
	}

	@Inject(method = "close", at = @At("RETURN"))
	private void vl$close(CallbackInfo ci) {
		vl$sessionData().closed();
	}

	@Inject(method = "applyPlayerInfoUpdate", at = @At("RETURN"))
	private void vl$applyPlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket.Action action, ClientboundPlayerInfoUpdatePacket.Entry entry, PlayerInfo playerInfo, CallbackInfo ci) {
		if (action == ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED) {
			vl$sessionData().refreshListedPlayers();
		}
	}
}
