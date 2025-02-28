package dev.beast.mods.shimmer.feature.misc;

import dev.beast.mods.shimmer.ShimmerNet;
import dev.beast.mods.shimmer.util.ShimmerStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FakeBlockPayload(BlockPos pos, BlockState state) implements CustomPacketPayload {
	public static final Type<FakeBlockPayload> TYPE = ShimmerNet.type("fake_block");
	public static final StreamCodec<RegistryFriendlyByteBuf, FakeBlockPayload> STREAM_CODEC = StreamCodec.composite(
		BlockPos.STREAM_CODEC,
		FakeBlockPayload::pos,
		ShimmerStreamCodecs.BLOCK_STATE,
		FakeBlockPayload::state,
		FakeBlockPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(IPayloadContext ctx) {
		ctx.enqueueWork(() -> ctx.player().level().setFakeBlock(pos, state));
	}
}
