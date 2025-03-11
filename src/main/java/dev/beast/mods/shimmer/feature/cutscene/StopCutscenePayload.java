package dev.beast.mods.shimmer.feature.cutscene;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class StopCutscenePayload implements ShimmerPacketPayload {
	public static final StopCutscenePayload INSTANCE = new StopCutscenePayload();

	@AutoPacket
	public static final ShimmerPacketType<StopCutscenePayload> TYPE = ShimmerPacketType.internal("stop_cutscene", StreamCodec.unit(INSTANCE));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().stopCutscene();
	}
}
