package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.feature.net.ShimmerPayloadContext;
import net.minecraft.network.codec.StreamCodec;

public enum CloseScreenPayload implements ShimmerPacketPayload {
	INSTANCE;

	@AutoPacket
	public static final ShimmerPacketType<CloseScreenPayload> TYPE = ShimmerPacketType.internal("close_screen", StreamCodec.unit(INSTANCE));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(ShimmerPayloadContext ctx) {
		ctx.player().shimmer$closeScreen();
	}
}
