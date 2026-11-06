package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;

public record RemoveAllPropsPayload(PropRemoveType removeType) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<RemoveAllPropsPayload> TYPE = SimplePayloadType.vidlib("prop/remove_all", CompositeStreamCodec.of(
		PropRemoveType.STREAM_CODEC, RemoveAllPropsPayload::removeType,
		RemoveAllPropsPayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = VLPlatformHelper.INSTANCE.getProps(ctx.level());
		props.levelProps.removeAll(removeType);
	}
}
