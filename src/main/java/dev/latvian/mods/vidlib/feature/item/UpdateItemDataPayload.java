package dev.latvian.mods.vidlib.feature.item;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.vidlib.core.api.common.VLItem;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;

public record UpdateItemDataPayload(InteractionHand hand, CompoundTag tag) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<UpdateItemDataPayload> TYPE = SimplePayloadType.vidlib("update_item_data", CompositeStreamCodec.of(
		DataTypes.HAND.streamCodec(), UpdateItemDataPayload::hand,
		MCStreamCodecs.COMPOUND_TAG, UpdateItemDataPayload::tag,
		UpdateItemDataPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			VLItem.partiallyMergeCustomData(ctx.player().getItemInHand(hand), tag);
		}
	}
}
