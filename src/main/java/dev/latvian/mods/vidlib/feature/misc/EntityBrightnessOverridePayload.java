package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record EntityBrightnessOverridePayload(int entityId, int override) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<EntityBrightnessOverridePayload> TYPE = VidLibPacketType.internal("entity_brightness_override", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, EntityBrightnessOverridePayload::entityId,
		ByteBufCodecs.INT, EntityBrightnessOverridePayload::override,
		EntityBrightnessOverridePayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.player().vl$sessionData().setEntityBrightnessOverride(entityId, override);
	}
}
