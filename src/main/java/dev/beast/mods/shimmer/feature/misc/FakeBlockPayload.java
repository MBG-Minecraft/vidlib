package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import dev.beast.mods.shimmer.util.CompositeStreamCodec;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FakeBlockPayload(BlockPos pos, BlockState state) implements ShimmerPacketPayload {
	public static final ShimmerPacketType<FakeBlockPayload> TYPE = ShimmerPacketType.internal("fake_block", CompositeStreamCodec.of(
		BlockPos.STREAM_CODEC,
		FakeBlockPayload::pos,
		ShimmerStreamCodecs.BLOCK_STATE,
		FakeBlockPayload::state,
		FakeBlockPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		ctx.player().level().setFakeBlock(pos, state);
	}
}
