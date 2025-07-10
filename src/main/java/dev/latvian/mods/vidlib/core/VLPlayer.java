package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.klib.math.Line;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.session.SessionData;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface VLPlayer extends VLLivingEntity, VLPlayerContainer {
	@Override
	default Player vl$self() {
		return (Player) this;
	}

	default SessionData vl$sessionData() {
		throw new NoMixinException(this);
	}

	default void vl$sessionData(SessionData data) {
		throw new NoMixinException(this);
	}

	@Override
	default boolean vl$isCreative() {
		return vl$self().isCreative();
	}

	default <T> T get(DataKey<T> type) {
		return vl$sessionData().dataMap.get(type, vl$level().getGameTime());
	}

	default <T> void set(DataKey<T> type, T value) {
		vl$sessionData().dataMap.set(type, value);
	}

	default <T> void reset(DataKey<T> type) {
		vl$sessionData().dataMap.reset(type);
	}

	@Override
	default boolean isSuspended() {
		return vl$sessionData().suspended;
	}

	default void setSuspended(boolean value) {
		set(InternalPlayerData.SUSPENDED, value);
	}

	default Component getNickname() {
		var s = vl$sessionData();
		return s == null ? Empty.COMPONENT : s.nickname;
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
		return ray(vl$self().blockInteractionRange(), delta);
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
