package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import net.minecraft.network.codec.ByteBufCodecs;

public record EntityBrightnessOverridePayload(int entityId, int override) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<EntityBrightnessOverridePayload> TYPE = SimplePayloadType.vidlib("entity_brightness_override", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, EntityBrightnessOverridePayload::entityId,
		ByteBufCodecs.INT, EntityBrightnessOverridePayload::override,
		EntityBrightnessOverridePayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		ctx.globalSessionData().setEntityBrightnessOverride(entityId, override);
	}
}
