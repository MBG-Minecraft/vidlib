package dev.latvian.mods.vidlib.feature.data;

import dev.latvian.mods.klib.codec.CompositeStreamCodec;
import dev.latvian.mods.klib.codec.MCStreamCodecs;
import dev.latvian.mods.vidlib.feature.auto.AutoPacket;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.SimplePayload;
import dev.latvian.mods.vidlib.feature.net.SimplePayloadType;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;

public record SyncLevelDataPayload(ResourceKey<Level> dimension, List<DataMapValue<LevelSessionData<?>>> update) implements SimplePayload {
	@AutoPacket
	public static final SimplePayloadType<SyncLevelDataPayload> TYPE = SimplePayloadType.vidlib("sync_level_data", CompositeStreamCodec.of(
		MCStreamCodecs.DIMENSION, SyncLevelDataPayload::dimension,
		DataKey.LEVEL.valueListStreamCodec, SyncLevelDataPayload::update,
		SyncLevelDataPayload::new
	));

	public SyncLevelDataPayload(LevelSessionData<?> data, List<DataMapValue<LevelSessionData<?>>> update) {
		this(data.dimension, update);
	}

	@Override
	public SimplePayloadType<?> getType() {
		return TYPE;
	}

	@Override
	public boolean allowDebugLogging() {
		for (var u : update) {
			if (u.key() != null && !u.key().skipLogging) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void handle(Context ctx) {
		var session = ctx.globalSessionData();
		session.getLevel(dimension).dataMap.update(session, update);
	}
}
