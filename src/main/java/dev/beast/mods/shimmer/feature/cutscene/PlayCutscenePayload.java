package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayCutscenePayload(Cutscene cutscene, WorldNumberVariables variables) implements ShimmerPacketPayload {
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
	public void handle(IPayloadContext ctx) {
		ctx.player().playCutscene(cutscene, variables);
	}
}
