package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.resources.Identifier;

public record SetPostEffectPayload(Identifier id) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<SetPostEffectPayload> TYPE = VidLibPacketType.internal("set_post_effect", Identifier.STREAM_CODEC.map(SetPostEffectPayload::new, SetPostEffectPayload::id));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().setPostEffect(id);
	}
}
