package dev.beast.mods.shimmer.core.mixin;

import com.mojang.serialization.DataResult;
import dev.beast.mods.shimmer.feature.codec.DataResultError;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(DataResult.class)
public interface DataResultMixin<R> {
	@Shadow
	<E extends Throwable> R getOrThrow(Function<String, E> exceptionSupplier) throws E;

	@Shadow
	<E extends Throwable> R getPartialOrThrow(Function<String, E> exceptionSupplier) throws E;

	/**
	 * @author Lat
	 * @reason Better Exception
	 */
	@Overwrite
	default R getOrThrow() {
		return getOrThrow(DataResultError::new);
	}

	/**
	 * @author Lat
	 * @reason Better Exception
	 */
	@Overwrite
	default R getPartialOrThrow() {
		return getPartialOrThrow(DataResultError::new);
	}
}
