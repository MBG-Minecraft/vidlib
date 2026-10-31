package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.network.codec.ByteBufCodecs;

public record PausePropPayload(PropListType propListType, int id, boolean paused) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<PausePropPayload> TYPE = SimplePayloadType.vidlib("prop/pause", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, PausePropPayload::propListType,
		ByteBufCodecs.VAR_INT, PausePropPayload::id,
		ByteBufCodecs.BOOL, PausePropPayload::paused,
		PausePropPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = VLPlatformHelper.CURRENT.getProps(ctx.level());
		var prop = props.propLists.get(propListType).get(id);

		if (prop != null) {
			prop.paused = paused;
		}
	}
}
