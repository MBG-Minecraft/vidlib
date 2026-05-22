package dev.latvian.mods.replay.api;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.Nullable;

public interface ReplaySessionData {
	ReplaySessionDataType<?> getType();

	default <O> void load(ReplaySession session, DynamicOps<O> ops, @Nullable O input) {
	}

	@Nullable
	default <O> DataResult<O> save(ReplaySession session, DynamicOps<O> ops) {
		return null;
	}
}
