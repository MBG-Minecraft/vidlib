package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public record PropInteractionPayload(PropListType type, int id, int button, Vec3 pos, Direction side) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<PropInteractionPayload> TYPE = VidLibPacketType.internal("prop/interaction", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, PropInteractionPayload::type,
		ByteBufCodecs.VAR_INT, PropInteractionPayload::id,
		ByteBufCodecs.VAR_INT, PropInteractionPayload::button,
		MCStreamCodecs.VEC3, PropInteractionPayload::pos,
		Direction.STREAM_CODEC, PropInteractionPayload::side,
		PropInteractionPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var prop = ctx.level().getProps().propLists.get(type).get(id);

		if (prop != null) {
			prop.onServerInteraction((ServerPlayer) ctx.player(), button, pos, side);
		}
	}
}
