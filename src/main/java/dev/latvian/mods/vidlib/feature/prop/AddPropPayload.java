package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;

public record AddPropPayload(PropType<?> type, PropSpawnType spawnType, int id, long createdTime, byte[] update) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<AddPropPayload> TYPE = VidLibPacketType.internal("prop/add", CompositeStreamCodec.of(
		PropType.STREAM_CODEC, AddPropPayload::type,
		PropSpawnType.STREAM_CODEC, AddPropPayload::spawnType,
		ByteBufCodecs.VAR_INT, AddPropPayload::id,
		ByteBufCodecs.VAR_LONG, AddPropPayload::createdTime,
		ByteBufCodecs.BYTE_ARRAY, AddPropPayload::update,
		AddPropPayload::new
	));

	public AddPropPayload(Prop prop) {
		this(prop.type, prop.spawnType, prop.id, prop.createdTime, prop.getDataUpdates(true));
	}

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = ctx.level().getProps();

		var oldProp = props.levelProps.get(id);

		if (oldProp != null) {
			oldProp.update(ctx.level().registryAccess(), update, true);
		} else {
			var prop = type.factory().create(props.context(type, spawnType, createdTime));
			prop.id = id;
			prop.update(ctx.level().registryAccess(), update, true);
			props.add(prop);
		}
	}
}
