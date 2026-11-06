package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.network.codec.ByteBufCodecs;

public record AddPropPayload(PropType<?> propType, PropSpawnType spawnType, int id, long createdTime, byte[] update) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<AddPropPayload> TYPE = SimplePayloadType.vidlib("prop/add", CompositeStreamCodec.of(
		PropType.STREAM_CODEC, AddPropPayload::propType,
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
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ReplayProp.LIST != null) {
			return;
		}

		var level = ctx.level();
		var props = VLPlatformHelper.INSTANCE.getProps(level);

		var prop = props.levelProps.get(id);

		if (prop != null) {
			prop.update(level.registryAccess(), update, true);
		} else {
			prop = propType.factory().create(props.context(propType, spawnType, createdTime));
			prop.id = id;
			prop.update(level.registryAccess(), update, true);
			prop.handleAddPacket(props);
		}
	}
}
