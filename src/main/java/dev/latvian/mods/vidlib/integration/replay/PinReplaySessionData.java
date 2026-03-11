package dev.latvian.mods.vidlib.integration.replay;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.latvian.mods.replay.api.ReplaySession;
import dev.latvian.mods.replay.api.ReplaySessionData;
import dev.latvian.mods.vidlib.feature.pin.Pin;
import dev.latvian.mods.vidlib.feature.pin.Pins;

public class PinReplaySessionData implements ReplaySessionData {
	@Override
	public String id() {
		return "vidlib:pins";
	}

	@Override
	public <O> void load(ReplaySession session, DynamicOps<O> ops, O input) {
		Pins.PINS.clear();
		Pin.UNBOUND_MAP_CODEC.parse(ops, input).ifSuccess(Pins.PINS::putAll);
	}

	@Override
	public <O> DataResult<O> save(ReplaySession session, DynamicOps<O> ops) {
		return Pin.UNBOUND_MAP_CODEC.encodeStart(ops, Pins.PINS);
	}
}
