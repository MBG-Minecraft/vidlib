package dev.beast.mods.shimmer.feature.zone;

import dev.beast.mods.shimmer.ShimmerNet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record UpdateZonesPayload(List<ZoneContainer> update) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<UpdateZonesPayload> TYPE = ShimmerNet.type("update_zones");

	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateZonesPayload> STREAM_CODEC = ZoneContainer.STREAM_CODEC.apply(ByteBufCodecs.list()).map(UpdateZonesPayload::new, UpdateZonesPayload::update);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> ctx.player().shimmer$sessionData().updateZones(ctx.player().level(), update));
	}
}
