package dev.latvian.mods.vidlib.feature.item;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.data.DataTypes;
import dev.latvian.mods.vidlib.core.VLItem;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;

public record UpdateItemDataPayload(InteractionHand hand, CompoundTag tag) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<UpdateItemDataPayload> TYPE = VidLibPacketType.internal("update_item_data", CompositeStreamCodec.of(
		DataTypes.HAND.streamCodec(), UpdateItemDataPayload::hand,
		MCStreamCodecs.COMPOUND_TAG, UpdateItemDataPayload::tag,
		UpdateItemDataPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.player().getServer().isSingleplayer() || ctx.player().hasPermissions(2)) {
			VLItem.partiallyMergeCustomData(ctx.player().getItemInHand(hand), tag);
		}
	}
}
