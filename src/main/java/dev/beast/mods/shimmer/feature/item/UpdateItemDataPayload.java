package dev.beast.mods.shimmer.feature.item;

import dev.beast.mods.shimmer.core.ShimmerItem;
import dev.beast.mods.shimmer.feature.auto.AutoPacket;
import dev.beast.mods.shimmer.feature.codec.CompositeStreamCodec;
import dev.beast.mods.shimmer.feature.codec.KnownCodec;
import dev.beast.mods.shimmer.feature.codec.ShimmerStreamCodecs;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketPayload;
import dev.beast.mods.shimmer.feature.net.ShimmerPacketType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateItemDataPayload(InteractionHand hand, CompoundTag tag) implements ShimmerPacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final ShimmerPacketType<UpdateItemDataPayload> TYPE = ShimmerPacketType.internal("update_item_data", CompositeStreamCodec.of(
		KnownCodec.HAND.streamCodec(), UpdateItemDataPayload::hand,
		ShimmerStreamCodecs.COMPOUND_TAG, UpdateItemDataPayload::tag,
		UpdateItemDataPayload::new
	));

	@Override
	public ShimmerPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(IPayloadContext ctx) {
		if (ctx.player().getServer().isSingleplayer() || ctx.player().hasPermissions(2)) {
			ShimmerItem.partiallyMergeCustomData(ctx.player().getItemInHand(hand), tag);
		}
	}
}
