package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public record PropInteractionPayload(int id, int button, Vec3 pos, Direction side) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<PropInteractionPayload> TYPE = SimplePayloadType.vidlib("prop/interaction", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, PropInteractionPayload::id,
		ByteBufCodecs.VAR_INT, PropInteractionPayload::button,
		MCStreamCodecs.VEC3, PropInteractionPayload::pos,
		Direction.STREAM_CODEC, PropInteractionPayload::side,
		PropInteractionPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = VLPlatformHelper.INSTANCE.getProps(ctx.level());
		var prop = props.levelProps.get(id);

		if (prop != null) {
			prop.onServerInteraction((ServerPlayer) ctx.player(), button, pos, side);
		}
	}
}
