package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.feature.misc.SyncPlayerTagsPayload;
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
	private Set<String> vl$prevTags;

	@Inject(method = "sendPairingData", at = @At("RETURN"))
	private void vl$sendPairingData(ServerPlayer to, PacketAndPayloadAcceptor<ClientGamePacketListener> callback, CallbackInfo ci) {
		if (entity instanceof ServerPlayer p) {
			p.vl$initialSync(callback);
		}
	}

	@Inject(method = "sendChanges", at = @At("RETURN"))
	private void vl$onTick(CallbackInfo ci) {
		if (entity instanceof ServerPlayer) {
			var tags = entity.getTags();

			if (vl$prevTags == null || !vl$prevTags.equals(tags)) {
				vl$prevTags = Set.copyOf(tags);
				entity.getServer().s2c(new SyncPlayerTagsPayload(entity.getUUID(), List.copyOf(tags)).toS2C(entity.level()));
			}
		}
	}
}
