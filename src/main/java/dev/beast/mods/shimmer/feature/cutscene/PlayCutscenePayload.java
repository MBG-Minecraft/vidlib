package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayCutscenePayload(Cutscene cutscene, WorldNumberVariables variables) implements ShimmerPacketPayload {
	public static final ShimmerPacketType<PlayCutscenePayload> TYPE = ShimmerPacketType.internal("play_cutscene", StreamCodec.composite(
		Cutscene.STREAM_CODEC,
		PlayCutscenePayload::cutscene,
		WorldNumberVariables.STREAM_CODEC,
		PlayCutscenePayload::variables,
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
