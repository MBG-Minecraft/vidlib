package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.Nullable;

public record UpdatePropPayload(int id, byte[] update) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<UpdatePropPayload> TYPE = SimplePayloadType.vidlib("prop/update", CompositeStreamCodec.of(
		ByteBufCodecs.VAR_INT, UpdatePropPayload::id,
		ByteBufCodecs.BYTE_ARRAY, UpdatePropPayload::update,
		UpdatePropPayload::new
	));

	@Nullable
	public static UpdatePropPayload of(Prop prop) {
		var update = prop.isRemoved() ? null : prop.getDataUpdates(false);

		if (update != null) {
			return new UpdatePropPayload(prop.id, update);
		}

		return null;
	}

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var props = VLPlatformHelper.INSTANCE.getProps(ctx.level());
		var prop = props.levelProps.get(id);

		if (prop != null) {
			prop.update(ctx.level().registryAccess(), update, false);
		}
	}
}
