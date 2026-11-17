package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public record PropCollisionPayload(int id, Vec3 pos, boolean inside) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<PropCollisionPayload> TYPE = SimplePayloadType.vidlib("prop/collision", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, PropCollisionPayload::id,
		MCStreamCodecs.VEC3, PropCollisionPayload::pos,
		ByteBufCodecs.BOOL, PropCollisionPayload::inside,
		PropCollisionPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = ctx.levelSessionData().props;
		var prop = props.levelProps.get(id);

		if (prop != null) {
			prop.onServerCollision((ServerPlayer) ctx.player(), pos, inside);
		}
	}
}
