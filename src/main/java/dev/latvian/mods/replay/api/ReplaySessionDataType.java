package dev.latvian.mods.replay.api;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record ReplaySessionDataType<T extends ReplaySessionData>(ResourceLocation id, Supplier<T> factory) {
	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof ReplaySessionDataType<?> t && id.equals(t.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public @NotNull String toString() {
		return id.toString();
	}
}
