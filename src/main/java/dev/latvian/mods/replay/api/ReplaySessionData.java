package dev.latvian.mods.replay.api;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

public interface ReplaySessionData {
	String id();

	default <O> void load(ReplaySession session, DynamicOps<O> ops, O input) {
	}

	default <O> DataResult<O> save(ReplaySession session, DynamicOps<O> ops) {
		return DataResult.error(() -> "No data");
	}
}
