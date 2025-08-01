package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.Nullable;

public record UpdatePropPayload(PropListType type, int id, byte[] update) implements SimplePacketPayload {
	@AutoPacket
	public static final VidLibPacketType<UpdatePropPayload> TYPE = VidLibPacketType.internal("prop/update", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, UpdatePropPayload::type,
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
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		var prop = ctx.level().getProps().propLists.get(type).get(id);

		if (prop != null) {
			prop.update(ctx.level().registryAccess(), update, false);
		}
	}
}
