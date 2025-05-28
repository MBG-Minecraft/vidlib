package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record UpdatePropPayload(int id, byte[] update) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<UpdatePropPayload> TYPE = VidLibPacketType.internal("update_prop", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, UpdatePropPayload::id,
		ByteBufCodecs.BYTE_ARRAY, UpdatePropPayload::update,
		UpdatePropPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var prop = ctx.level().getProps().active.get(id);

		if (prop != null) {
			prop.update(ctx.level().registryAccess(), update);
		}
	}
}
