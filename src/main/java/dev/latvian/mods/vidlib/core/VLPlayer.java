package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.kmath.Line;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.data.DataType;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.session.SessionData;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import dev.latvian.mods.vidlib.feature.zone.ZoneRenderType;
import dev.latvian.mods.vidlib.util.Empty;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface VLPlayer extends VLLivingEntity, VLPlayerContainer {
	default SessionData vl$sessionData() {
		throw new NoMixinException(this);
	}

	default void vl$sessionData(SessionData data) {
		throw new NoMixinException(this);
	}

	@Override
	default boolean vl$isCreative() {
		return ((Player) this).isCreative();
	}

	default <T> T get(DataType<T> type) {
		return vl$sessionData().dataMap.get(type);
	}

	default <T> void set(DataType<T> type, T value) {
		vl$sessionData().dataMap.set(type, value);
	}

	@Override
	default boolean isSuspended() {
		return vl$sessionData().suspended;
	}

	default void setSuspended(boolean value) {
		set(InternalPlayerData.SUSPENDED, value);
	}

	default Component getNickname() {
		return vl$sessionData().nickname;
	}

	default void setNickname(Component nickname) {
		set(InternalPlayerData.NICKNAME, Empty.isEmpty(nickname) ? Empty.COMPONENT : nickname);
	}

	@Nullable
	default IconHolder getPlumbobHolder() {
		return vl$sessionData().plumbobIcon;
	}

	default void setPlumbob(Icon icon) {
		set(InternalPlayerData.PLUMBOB, icon.holder());
	}

	default Clothing getClothing() {
		return vl$sessionData().clothing;
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

	default boolean getShowAnchor() {
		return get(InternalPlayerData.SHOW_ANCHOR);
	}

	default void setShowAnchor(boolean show) {
		set(InternalPlayerData.SHOW_ANCHOR, show);
	}

	default float getFlightSpeedMod() {
		return vl$sessionData().flightSpeedMod;
	}

	default void setFlightSpeedMod(float value) {
		set(InternalPlayerData.FLIGHT_SPEED, value);
	}

	@Override
	@Nullable
	default Boolean vl$glowingOverride() {
		return vl$sessionData().glowingOverride;
	}

	@Override
	@Nullable
	default Integer vl$teamColorOverride() {
		return vl$sessionData().teamColorOverride;
	}

	@Override
	default double vl$gravityMod() {
		return vl$sessionData().gravityMod;
	}

	@Override
	default float vl$speedMod() {
		return vl$sessionData().speedMod;
	}

	@Override
	default float vl$attackDamageMod() {
		return vl$sessionData().attackDamageMod;
	}

	default boolean vl$pvp(Player other) {
		return vl$sessionData().pvp && other.vl$sessionData().pvp;
	}

	@Override
	default Line ray(float delta) {
		return ray(((Player) this).blockInteractionRange(), delta);
	}

	@Override
	default List<ZoneInstance> getZones() {
		return vl$sessionData().zonesIn;
	}

	default Set<String> getZoneTags() {
		return vl$sessionData().zonesTagsIn;
	}

	default boolean isReplayCamera() {
		return false;
	}

	@Override
	default boolean vl$unpushable() {
		return vl$sessionData().unpushable;
	}
}
