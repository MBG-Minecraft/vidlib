package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public record PropInteractionPayload(PropListType type, int id, int button, Vec3 pos, Direction side) implements SimplePacketPayload {

	private static StreamCodec<ByteBuf, Vec3> VEC3_STREAM_CODEC = new StreamCodec<ByteBuf, Vec3>() {
		public Vec3 decode(ByteBuf p_361466_) {
			return FriendlyByteBuf.readVec3(p_361466_);
		}

		public void encode(ByteBuf p_364962_, Vec3 p_364468_) {
			FriendlyByteBuf.writeVec3(p_364962_, p_364468_);
		}
	};

	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<PropInteractionPayload> TYPE = VidLibPacketType.internal("prop/interaction", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, PropInteractionPayload::type,
		ByteBufCodecs.VAR_INT, PropInteractionPayload::id,
		ByteBufCodecs.VAR_INT, PropInteractionPayload::button,
		VEC3_STREAM_CODEC, PropInteractionPayload::pos,
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
