package dev.beast.mods.shimmer.feature.skybox;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SyncSkyboxesPayload(List<SkyboxData> update) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<SyncSkyboxesPayload> TYPE = ShimmerPacketType.internal("sync_skyboxes", SkyboxData.STREAM_CODEC.list().map(SyncSkyboxesPayload::new, SyncSkyboxesPayload::update));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().shimmer$sessionData().updateSkyboxes(update);
	}
}
