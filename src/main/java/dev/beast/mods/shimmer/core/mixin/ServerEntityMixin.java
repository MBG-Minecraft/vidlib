package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.feature.misc.SyncPlayerTagsPayload;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.bundle.PacketAndPayloadAcceptor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;

@Mixin(ServerEntity.class)
public class ServerEntityMixin {
	@Shadow
	@Final
	private Entity entity;

	@Unique
	private Set<String> shimmer$prevTags;

	@Inject(method = "sendPairingData", at = @At("RETURN"))
	private void shimmer$sendPairingData(ServerPlayer to, PacketAndPayloadAcceptor<ClientGamePacketListener> callback, CallbackInfo ci) {
		if (entity instanceof ServerPlayer p) {
			p.shimmer$initialSync(callback);
		}
	}

	@Inject(method = "sendChanges", at = @At("RETURN"))
	private void shimmer$onTick(CallbackInfo ci) {
		if (entity instanceof ServerPlayer) {
			var tags = entity.getTags();

			if (shimmer$prevTags == null || !shimmer$prevTags.equals(tags)) {
				shimmer$prevTags = Set.copyOf(tags);
				entity.getServer().s2c(new SyncPlayerTagsPayload(entity.getUUID(), List.copyOf(tags)).toS2C(entity.level()));
			}
		}
	}
}
