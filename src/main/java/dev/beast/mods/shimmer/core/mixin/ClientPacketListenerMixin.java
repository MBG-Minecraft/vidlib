package dev.beast.mods.shimmer.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.GameProfile;
import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.core.ShimmerClientPacketListener;
import dev.beast.mods.shimmer.feature.entity.ExactEntitySpawnPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import dev.beast.mods.shimmer.feature.session.ShimmerLocalClientSessionData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.CommonPlayerSpawnInfo;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.OptionalInt;
import java.util.UUID;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin implements ShimmerClientPacketListener {
	@Shadow
	private ClientLevel level;

	@Shadow
	@Final
	private GameProfile localGameProfile;

	@Shadow
	private OptionalInt removedPlayerVehicleId;

	@Shadow
	@Nullable
	public abstract PlayerInfo getPlayerInfo(UUID uniqueId);

	@Unique
	private ShimmerLocalClientSessionData shimmer$sessionData;

	@ModifyExpressionValue(method = {"handleRespawn", "handleLogin"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/game/CommonPlayerSpawnInfo;isFlat()Z"))
	private boolean shimmer$isFlat(boolean original, @Local CommonPlayerSpawnInfo info) {
		return true;
	}

	@Override
	public ShimmerLocalClientSessionData shimmer$sessionData() {
		if (shimmer$sessionData == null) {
			shimmer$sessionData = new ShimmerLocalClientSessionData(Minecraft.getInstance(), localGameProfile.getId(), (ClientPacketListener) (Object) this);
		}

		return shimmer$sessionData;
	}

	@Override
	public Entity shimmer$addEntity(ShimmerPayloadContext ctx, ExactEntitySpawnPayload payload) {
		if (removedPlayerVehicleId.isPresent() && removedPlayerVehicleId.getAsInt() == payload.id()) {
			removedPlayerVehicleId = OptionalInt.empty();
		}

		Entity entity;

		if (payload.type() == EntityType.PLAYER) {
			var playerinfo = getPlayerInfo(payload.uuid());

			if (playerinfo == null) {
				Shimmer.LOGGER.warn("Server attempted to add player prior to sending player info (Player id {})", payload.uuid());
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
			Shimmer.LOGGER.warn("Skipping Entity with id {}", payload.type());
			return null;
		}
	}

	@Inject(method = "handleLogin", at = @At("RETURN"))
	private void shimmer$handleLogin(CallbackInfo ci) {
		shimmer$sessionData().respawned(level, true);
	}

	@Inject(method = "handleRespawn", at = @At("RETURN"))
	private void shimmer$handleRespawn(CallbackInfo ci) {
		shimmer$sessionData().respawned(level, false);
	}

	@Inject(method = "close", at = @At("RETURN"))
	private void shimmer$close(CallbackInfo ci) {
		shimmer$sessionData().closed();
	}

	@Inject(method = "applyPlayerInfoUpdate", at = @At("RETURN"))
	private void shimmer$applyPlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket.Action action, ClientboundPlayerInfoUpdatePacket.Entry entry, PlayerInfo playerInfo, CallbackInfo ci) {
		if (action == ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED) {
			shimmer$sessionData().refreshListedPlayers();
		}
	}
}
