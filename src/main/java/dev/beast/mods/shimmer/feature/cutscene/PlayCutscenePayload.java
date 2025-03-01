package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.ShimmerNet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayCutscenePayload(Cutscene cutscene) implements CustomPacketPayload {
	public static final Type<PlayCutscenePayload> TYPE = ShimmerNet.type("play_cutscene");
	public static final StreamCodec<RegistryFriendlyByteBuf, PlayCutscenePayload> STREAM_CODEC = Cutscene.STREAM_CODEC.map(PlayCutscenePayload::new, PlayCutscenePayload::cutscene);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> ctx.player().playCutscene(cutscene));
	}
}
