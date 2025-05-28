package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record AddPropPayload(PropType<?> type, PropSpawnType spawnType, int id, byte[] update) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<AddPropPayload> TYPE = VidLibPacketType.internal("add_prop", CompositeStreamCodec.of(
		PropType.STREAM_CODEC, AddPropPayload::type,
		PropSpawnType.STREAM_CODEC, AddPropPayload::spawnType,
		ByteBufCodecs.VAR_INT, AddPropPayload::id,
		ByteBufCodecs.BYTE_ARRAY, AddPropPayload::update,
		AddPropPayload::new
	));

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = ctx.level().getProps();
		var prop = type.factory().create(new PropContext(props, type, spawnType, null));
		prop.id = id;
		prop.update(ctx.level().registryAccess(), update);
		props.add(prop);
	}
}
