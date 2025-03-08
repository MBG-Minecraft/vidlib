package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.session.ShimmerSessionData;
import dev.beast.mods.shimmer.math.Line;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ShimmerPlayer extends ShimmerLivingEntity {
	@Override
	default List<? extends Player> shimmer$getPlayers() {
		return List.of((Player) this);
	}

	default ShimmerSessionData shimmer$sessionData() {
		throw new NoMixinException();
	}

	@Override
	default boolean shimmer$isCreative() {
		return ((Player) this).isCreative();
	}

	default <T> T get(DataType<T> type) {
		return shimmer$sessionData().dataMap.get(type);
	}

	@Nullable
	default <T> T getOrNull(DataType<T> type) {
		return shimmer$sessionData().dataMap.getOrNull(type);
	}

	default <T> void set(DataType<T> type, T value) {
		shimmer$sessionData().dataMap.set(type, value);
	}

	@Override
	default Line ray(float delta) {
		return ray(((Player) this).blockInteractionRange(), delta);
	}
}
