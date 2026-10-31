package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.Nullable;

public record UpdatePropPayload(PropListType propListType, int id, byte[] update) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<UpdatePropPayload> TYPE = SimplePayloadType.vidlib("prop/update", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, UpdatePropPayload::propListType,
		ByteBufCodecs.VAR_INT, UpdatePropPayload::id,
		ByteBufCodecs.BYTE_ARRAY, UpdatePropPayload::update,
		UpdatePropPayload::new
	));

	@Nullable
	public static UpdatePropPayload of(Prop prop) {
		var update = prop.isRemoved() ? null : prop.getDataUpdates(false);

		if (update != null) {
			return new UpdatePropPayload(prop.spawnType.listType, prop.id, update);
		}

		return null;
	}

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = VLPlatformHelper.CURRENT.getProps(ctx.level());
		var prop = props.propLists.get(propListType).get(id);

		if (prop != null) {
			prop.update(ctx.level().registryAccess(), update, false);
		}
	}
}
