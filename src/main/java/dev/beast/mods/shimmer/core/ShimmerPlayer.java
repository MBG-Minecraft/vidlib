package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.clothing.Clothing;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.data.InternalPlayerData;
import dev.beast.mods.shimmer.feature.icon.Icon;
import dev.beast.mods.shimmer.feature.icon.IconHolder;
import dev.beast.mods.shimmer.feature.session.ShimmerSessionData;
import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import dev.beast.mods.shimmer.feature.zone.ZoneRenderType;
import dev.beast.mods.shimmer.util.Empty;
import dev.latvian.mods.kmath.Line;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface ShimmerPlayer extends ShimmerLivingEntity, ShimmerPlayerContainer {
	default ShimmerSessionData shimmer$sessionData() {
		throw new NoMixinException(this);
	}

	default void shimmer$sessionData(ShimmerSessionData data) {
		throw new NoMixinException(this);
	}

	@Override
	default boolean shimmer$isCreative() {
		return ((Player) this).isCreative();
	}

	default <T> T get(DataType<T> type) {
		return shimmer$sessionData().dataMap.get(type, shimmer$level().getGameTime());
	}

	default <T> void set(DataType<T> type, T value) {
		shimmer$sessionData().dataMap.set(type, value);
	}

	@Override
	default boolean isSuspended() {
		return shimmer$sessionData().suspended;
	}

	default void setSuspended(boolean value) {
		set(InternalPlayerData.SUSPENDED, value);
	}

	default Component getNickname() {
		return shimmer$sessionData().nickname;
	}

	default void setNickname(Component nickname) {
		set(InternalPlayerData.NICKNAME, Empty.isEmpty(nickname) ? Empty.COMPONENT : nickname);
	}

	@Nullable
	default IconHolder getPlumbobHolder() {
		return shimmer$sessionData().plumbobIcon;
	}

	default void setPlumbob(Icon icon) {
		set(InternalPlayerData.PLUMBOB, icon.holder());
	}

	default Clothing getClothing() {
		return shimmer$sessionData().clothing;
	}

	default void setClothing(Clothing clothing) {
		set(InternalPlayerData.CLOTHING, clothing);
	}

	default boolean getShowZones() {
		return get(InternalPlayerData.SHOW_ZONES);
	}

	default void setShowZones(boolean show) {
		set(InternalPlayerData.SHOW_ZONES, show);
	}

	default ZoneRenderType getZoneRenderType() {
		return get(InternalPlayerData.ZONE_RENDER_TYPE);
	}

	default void setZoneRenderType(ZoneRenderType type) {
		set(InternalPlayerData.ZONE_RENDER_TYPE, type);
	}

	default BlockFilter getZoneBlockFilter() {
		return get(InternalPlayerData.ZONE_BLOCK_FILTER);
	}

	default void setZoneBlockFilter(BlockFilter filter) {
		set(InternalPlayerData.ZONE_BLOCK_FILTER, filter);
	}

	default float getFlightSpeedMod() {
		return shimmer$sessionData().flightSpeedMod;
	}

	default void setFlightSpeedMod(float value) {
		set(InternalPlayerData.FLIGHT_SPEED, value);
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

	@Override
	default boolean shimmer$unpushable() {
		return shimmer$sessionData().unpushable;
	}
}
