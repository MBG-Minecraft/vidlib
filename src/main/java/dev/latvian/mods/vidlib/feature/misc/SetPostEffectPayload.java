package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.resources.Identifier;

public record SetPostEffectPayload(Identifier id) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SetPostEffectPayload> TYPE = SimplePayloadType.vidlib("set_post_effect", Identifier.STREAM_CODEC.map(SetPostEffectPayload::new, SetPostEffectPayload::id));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().setPostEffect(id);
	}
}
