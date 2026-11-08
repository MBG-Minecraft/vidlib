package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;

public record UpdateLevelDataValuePayload(ResourceKey<Level> dimension, List<DataMapValue<LevelSessionData<?>>> update) implements SimplePayload {
	@AutoPacket(to = AutoPacket.To.SERVER)
	public static final SimplePayloadType<UpdateLevelDataValuePayload> TYPE = SimplePayloadType.vidlib("update_level_data_value", CompositeStreamCodec.of(
		MCStreamCodecs.DIMENSION, UpdateLevelDataValuePayload::dimension,
		DataKey.LEVEL.valueListStreamCodec, UpdateLevelDataValuePayload::update,
		UpdateLevelDataValuePayload::new
	));

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public void handle(Context ctx) {
		if (ctx.isAdmin()) {
			var dataMap = ctx.globalSessionData().getLevel(dimension).dataMap;

			for (var u : update) {
				if (u.key() != null) {
					dataMap.set(u.key(), Cast.to(u.value()));
				}
			}
		}
	}
}
