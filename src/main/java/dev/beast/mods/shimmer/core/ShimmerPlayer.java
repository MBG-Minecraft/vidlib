package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.session.ShimmerSessionData;
import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import dev.beast.mods.shimmer.math.Line;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

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
	default boolean isSuspended() {
		return shimmer$sessionData().suspended;
	}

	@Override
	@Nullable
	default Boolean shimmer$glowingOverride() {
		return shimmer$sessionData().glowingOverride;
	}

	@Override
	@Nullable
	default Integer shimmer$teamColorOverride() {
		return shimmer$sessionData().teamColorOverride;
	}

	@Override
	default double shimmer$gravityMod() {
		return shimmer$sessionData().gravityMod;
	}

	@Override
	default float shimmer$speedMod() {
		return shimmer$sessionData().speedMod;
	}

	@Override
	default float shimmer$attackDamageMod() {
		return shimmer$sessionData().attackDamageMod;
	}

	default boolean shimmer$pvp(Player other) {
		return shimmer$sessionData().pvp && other.shimmer$sessionData().pvp;
	}

	@Override
	default Line ray(float delta) {
		return ray(((Player) this).blockInteractionRange(), delta);
	}

	@Override
	default List<ZoneInstance> getZones() {
		return shimmer$sessionData().zonesIn;
	}

	default Set<String> getZoneTags() {
		return shimmer$sessionData().zonesTagsIn;
	}

	default boolean isReplayCamera() {
		return false;
	}
}
