package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.entity.EntityOverrideValue;
import dev.beast.mods.shimmer.util.CustomPacketHandler;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.Nullable;

public interface ShimmerEntity extends CustomPacketHandler {
	<T> T shimmer$getDirectOverride(EntityOverride<T> override);

	<T> void shimmer$setDirectOverride(EntityOverride<T> override, @Nullable EntityOverrideValue<T> value);

	boolean shimmer$isSaving();

	@Override
	default void send(CustomPacketPayload packet) {
	}
}
