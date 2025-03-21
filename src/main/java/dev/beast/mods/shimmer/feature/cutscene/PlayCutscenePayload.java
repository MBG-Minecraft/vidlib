package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;

public record PlayCutscenePayload(Cutscene cutscene, WorldNumberVariables variables) implements ShimmerPacketPayload {
	@AutoPacket
	public static final ShimmerPacketType<PlayCutscenePayload> TYPE = ShimmerPacketType.internal("play_cutscene", CompositeStreamCodec.of(
		Cutscene.STREAM_CODEC, PlayCutscenePayload::cutscene,
		WorldNumberVariables.STREAM_CODEC, PlayCutscenePayload::variables,
		PlayCutscenePayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().playCutscene(cutscene, variables);
	}
}
