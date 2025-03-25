package dev.beast.mods.shimmer.feature.data;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import dev.beast.mods.shimmer.util.Cast;

import java.util.List;

public record UpdatePlayerDataValuePayload(List<DataMapValue> update) implements ShimmerPacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final ShimmerPacketType<UpdatePlayerDataValuePayload> TYPE = ShimmerPacketType.internal("update_player_data_value", DataType.PLAYER.valueListStreamCodec.map(UpdatePlayerDataValuePayload::new, UpdatePlayerDataValuePayload::update));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		for (var value : update) {
			if (value.type().allowClientUpdates()) {
				ctx.player().set(value.type(), Cast.to(value.value()));
			}
		}
	}
}
