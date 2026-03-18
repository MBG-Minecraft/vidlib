package dev.latvian.mods.vidlib.feature.maptextureoverride;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.replay.api.ReplaySession;
import dev.latvian.mods.replay.api.ReplaySessionData;
import dev.latvian.mods.replay.api.ReplaySessionDataType;
import dev.latvian.mods.vidlib.VidLib;
import org.jetbrains.annotations.Nullable;

public class MapTextureOverridesReplaySessionData implements ReplaySessionData {
	public static final ReplaySessionDataType<MapTextureOverridesReplaySessionData> TYPE = new ReplaySessionDataType<>(VidLib.id("map_texture_overrides"), MapTextureOverridesReplaySessionData::new);

	public final MapTextureOverrides overrides;

	public MapTextureOverridesReplaySessionData() {
		this.overrides = new MapTextureOverrides();
	}

	@Override
	public ReplaySessionDataType<?> getType() {
		return TYPE;
	}

	@Override
	public <O> void load(ReplaySession session, DynamicOps<O> ops, @Nullable O input) {
		overrides.list.clear();

		if (input != null) {
			MapTextureOverrides.CODEC.parse(ops, input).ifSuccess(overrides::join);
		}

		overrides.update();
	}

	@Override
	public <O> DataResult<O> save(ReplaySession session, DynamicOps<O> ops) {
		return MapTextureOverrides.CODEC.encodeStart(ops, overrides);
	}
}
