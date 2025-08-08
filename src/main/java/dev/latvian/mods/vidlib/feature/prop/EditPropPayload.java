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

public record EditPropPayload(PropListType type, int id, byte[] update) implements SimplePacketPayload {
	@AutoPacket(AutoPacket.To.SERVER)
	public static final VidLibPacketType<EditPropPayload> TYPE = VidLibPacketType.internal("prop/edit", CompositeStreamCodec.of(
		PropListType.STREAM_CODEC, EditPropPayload::type,
		ByteBufCodecs.VAR_INT, EditPropPayload::id,
		ByteBufCodecs.BYTE_ARRAY, EditPropPayload::update,
		EditPropPayload::new
	));

	@Nullable
	public static EditPropPayload of(Prop prop, Collection<PropData<?, ?>> keys) {
		var data = new ArrayList<PropType.PropDataEntry>(keys.size());

		for (var key : keys) {
			var entry = prop.type.reverseData().get(key);

			if (entry != null) {
				data.add(entry);
			}
		}

		var update = prop.isRemoved() ? null : prop.getDataUpdates(data);

		if (update != null) {
			return new EditPropPayload(prop.spawnType.listType, prop.id, update);
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
