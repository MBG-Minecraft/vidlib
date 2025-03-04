package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayCutscenePayload(Cutscene cutscene) implements ShimmerPacketPayload {
	public static final ShimmerPacketType<PlayCutscenePayload> TYPE = ShimmerPacketType.internal("play_cutscene", Cutscene.STREAM_CODEC.map(PlayCutscenePayload::new, PlayCutscenePayload::cutscene));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().playCutscene(cutscene);
	}
}
