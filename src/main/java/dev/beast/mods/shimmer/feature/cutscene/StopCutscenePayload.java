package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.ShimmerNet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class StopCutscenePayload implements CustomPacketPayload {
	public static final Type<StopCutscenePayload> TYPE = ShimmerNet.type("stop_cutscene");
	public static final StopCutscenePayload INSTANCE = new StopCutscenePayload();
	public static final StreamCodec<RegistryFriendlyByteBuf, StopCutscenePayload> STREAM_CODEC = StreamCodec.unit(INSTANCE);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> ctx.player().stopCutscene());
	}
}
