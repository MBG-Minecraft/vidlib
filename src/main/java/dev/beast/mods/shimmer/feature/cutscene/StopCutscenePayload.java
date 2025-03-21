package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.network.codec.StreamCodec;

public enum StopCutscenePayload implements ShimmerPacketPayload {
	INSTANCE;

	@AutoPacket
	public static final ShimmerPacketType<StopCutscenePayload> TYPE = ShimmerPacketType.internal("stop_cutscene", StreamCodec.unit(INSTANCE));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().stopCutscene();
	}
}
