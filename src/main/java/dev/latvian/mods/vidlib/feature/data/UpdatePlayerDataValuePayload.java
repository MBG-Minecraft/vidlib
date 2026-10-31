package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.KLibStreamCodecs;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.server.permissions.Permissions;

import java.util.List;
import java.util.UUID;

public record UpdatePlayerDataValuePayload(UUID uuid, List<DataMapValue> update) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<UpdatePlayerDataValuePayload> TYPE = SimplePayloadType.vidlib("update_player_data_value", CompositeStreamCodec.of(
		KLibStreamCodecs.UUID, UpdatePlayerDataValuePayload::uuid,
		DataKey.PLAYER.valueListStreamCodec, UpdatePlayerDataValuePayload::update,
		UpdatePlayerDataValuePayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		for (var u : update) {
			if (u.key() != null && (u.key().allowClientUpdates() && ctx.player().getUUID().equals(uuid) || ctx.player().permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))) {
				var session = ctx.level().getServer().vl$getOrLoadServerSession(uuid);
				session.dataMap.set(u.key(), Cast.to(u.value()));
			}
		}
	}
}
