package dev.latvian.mods.vidlib.feature.prop;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePacketPayload;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketType;
import net.minecraft.network.codec.ByteBufCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public record UpdatePropRequestPayload(PropListType type, int id, byte[] update) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<UpdatePropRequestPayload> TYPE = VidLibPacketType.internal("prop/update_request", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, UpdatePropRequestPayload::type,
		ByteBufCodecs.VAR_INT, UpdatePropRequestPayload::id,
		ByteBufCodecs.BYTE_ARRAY, UpdatePropRequestPayload::update,
		UpdatePropRequestPayload::new
	));

	@Nullable
	public static UpdatePropRequestPayload of(Prop prop, Collection<PropData<?, ?>> keys) {
		var data = new ArrayList<PropDataEntry>(keys.size());

		for (var key : keys) {
			var entry = prop.type.reverseData().get(key);

			if (entry != null) {
				data.add(entry);
			}
		}

		var update = prop.isRemoved() ? null : prop.getDataUpdates(data);

		if (update != null) {
			return new UpdatePropRequestPayload(prop.spawnType.listType, prop.id, update);
		}

		return null;
	}

	@Override
	public VidLibPacketType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			var prop = ctx.level().getProps().propLists.get(type).get(id);

			if (prop != null) {
				prop.update(ctx.level().registryAccess(), update, false);
				ctx.level().s2c(new UpdatePropPayload(type, id, update));
			}
		}
	}
}
