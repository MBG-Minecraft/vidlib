package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.entity.PlayerInput;
import dev.latvian.mods.klib.icon.Icon;
import dev.latvian.mods.klib.net.S2CPacketConsumer;
import dev.latvian.mods.klib.registry.CustomRegistry;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.SkinTexture;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.data.SyncPlayerDataPayload;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.input.SyncPlayerInputToClient;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.prop.PropRemoveType;
import dev.latvian.mods.vidlib.feature.prop.RemoveAllPropsPayload;
import dev.latvian.mods.vidlib.feature.zone.ResolvedZone;
import dev.latvian.mods.vidlib.feature.zone.ZoneTag;
import dev.latvian.mods.vidlib.math.knumber.SyncGlobalNumberVariablesPayload;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PlayerSessionData implements Comparable<PlayerSessionData> {
	public static PlayerSessionData of(Player player) {
		return VLPlatformHelper.INSTANCE.getPlayerData(player);
	}

	public final GlobalSessionData worldData;
	public final UUID uuid;
	public final DataMap<PlayerSessionData> dataMap;
	public final long startTime;
	public PlayerInput prevInput;
	public PlayerInput input;
	public List<ResolvedZone> zonesIn;
	public Set<ZoneTag> zoneTagsIn;

	public PlayerSessionData(GlobalSessionData worldData, UUID uuid) {
		this.worldData = worldData;
		this.uuid = uuid;
		this.dataMap = new DataMap<>(worldData, this, DataKey.PLAYER);
		this.startTime = System.currentTimeMillis();
		this.prevInput = PlayerInput.NONE;
		this.input = PlayerInput.NONE;
		this.zonesIn = List.of();
		this.zoneTagsIn = Set.of();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + uuid + "]";
	}

	/**
	 * syncType 0 = reload
	 * syncType 1 = replay snapshot
	 * syncType 2 = login
	 */
	public void sync(S2CPacketConsumer packets, Player player, int syncType) {
		var level = player.level();

		if (level.getServer() != null) {
			packets.s2c(level.getServer().clockManager().createFullSyncPacket());
		}
		// packets.s2c(new ServerFeaturesPayload(FeatureSet.SERVER_FEATURES.get()));

		CustomRegistry.syncAll(player, packets);

		packets.s2c(new SyncGlobalNumberVariablesPayload(worldData.globalVariables));

		if (syncType > 0) {
			player.refreshDisplayName();

			if (player instanceof ServerPlayer serverPlayer) {
				serverPlayer.refreshTabListName();
			}
		}

		worldData.sync(packets);

		dataMap.syncAll(packets, SyncPlayerDataPayload::new);

		for (var s : worldData.playerMap.values()) {
			if (s != this) {
				packets.s2c(new SyncPlayerInputToClient(s.uuid, s.input));
				s.dataMap.syncAll(packets, SyncPlayerDataPayload::new);
			}
		}

		if (syncType > 0) {
			var props = LevelSessionData.of(level).props;

			if (syncType == 1) {
				packets.s2c(new RemoveAllPropsPayload(PropRemoveType.REPLAY));
			}

			for (var prop : props.levelProps) {
				packets.s2c(prop.createAddPacket());
			}
		}
	}

	public boolean isOnline() {
		return dataMap.get(InternalPlayerData.ONLINE);
	}

	public void setOnline(boolean value) {
		dataMap.set(InternalPlayerData.ONLINE, value);
	}

	public boolean isStaff() {
		return dataMap.get(InternalPlayerData.STAFF);
	}

	public void setStaff(boolean value) {
		dataMap.set(InternalPlayerData.STAFF, value);
	}

	public boolean isTalent() {
		return dataMap.get(InternalPlayerData.TALENT);
	}

	public void setTalent(boolean value) {
		dataMap.set(InternalPlayerData.TALENT, value);
	}

	public boolean isStaffOrTalent() {
		return isStaff() || isTalent();
	}

	public boolean getCanFly() {
		return dataMap.get(InternalPlayerData.CAN_FLY);
	}

	public void setCanFly(boolean value) {
		dataMap.set(InternalPlayerData.CAN_FLY, value);
	}

	public String getName() {
		var name = dataMap.get(InternalPlayerData.NAME);

		if (name.isEmpty()) {
			name = PlayerProfiles.getName(uuid);
			dataMap.set(InternalPlayerData.NAME, name);
		}

		return name;
	}

	public void setName(String name) {
		dataMap.set(InternalPlayerData.NAME, name);
	}

	public boolean isSuspended() {
		return dataMap.get(InternalPlayerData.SUSPENDED);
	}

	public void setSuspended(boolean value) {
		dataMap.set(InternalPlayerData.SUSPENDED, value);
	}

	public Component getNickname() {
		return dataMap.get(InternalPlayerData.NICKNAME);
	}

	public void setNickname(Component nickname) {
		dataMap.set(InternalPlayerData.NICKNAME, Empty.isEmpty(nickname) ? Empty.COMPONENT : nickname);
	}

	public Ref<Icon> getPlumbob() {
		return dataMap.get(InternalPlayerData.PLUMBOB);
	}

	public void setPlumbob(Ref<Icon> icon) {
		dataMap.set(InternalPlayerData.PLUMBOB, icon);
	}

	public PlayerClothing getClothing() {
		return dataMap.get(InternalPlayerData.CLOTHING);
	}

	public void setClothing(PlayerClothing clothing) {
		dataMap.set(InternalPlayerData.CLOTHING, clothing);
	}

	@Nullable
	public SkinTexture getSkinOverride() {
		return dataMap.getOptional(InternalPlayerData.SKIN_OVERRIDE);
	}

	public void setSkinOverride(@Nullable SkinTexture skin) {
		dataMap.set(InternalPlayerData.SKIN_OVERRIDE, skin);
	}

	@Nullable
	public ClientAsset.Texture getCapeOverride() {
		return dataMap.getOptional(InternalPlayerData.CAPE_OVERRIDE);
	}

	public void setCapeOverride(@Nullable ClientAsset.Texture cape) {
		dataMap.set(InternalPlayerData.CAPE_OVERRIDE, cape);
	}

	@Nullable
	public ClientAsset.Texture getElytraOverride() {
		return dataMap.getOptional(InternalPlayerData.ELYTRA_OVERRIDE);
	}

	public void setElytraOverride(@Nullable ClientAsset.Texture cape) {
		dataMap.set(InternalPlayerData.ELYTRA_OVERRIDE, cape);
	}

	public float getFlightSpeedMod() {
		return dataMap.get(InternalPlayerData.FLIGHT_SPEED_MOD);
	}

	public void setFlightSpeedMod(float value) {
		dataMap.set(InternalPlayerData.FLIGHT_SPEED_MOD, value);
	}

	@Nullable
	public Color getGlowColor() {
		return dataMap.getOptional(InternalPlayerData.GLOW_COLOR);
	}

	public void setGlowColor(@Nullable Color value) {
		dataMap.set(InternalPlayerData.GLOW_COLOR, value);
	}

	public boolean isTranslucent() {
		return dataMap.get(InternalPlayerData.TRANSLUCENT);
	}

	public void setTranslucent(boolean value) {
		dataMap.set(InternalPlayerData.TRANSLUCENT, value);
	}

	public float getBrightnessOverride() {
		return dataMap.get(InternalPlayerData.BRIGHTNESS_OVERRIDE);
	}

	public void setBrightnessOverride(float value) {
		dataMap.set(InternalPlayerData.BRIGHTNESS_OVERRIDE, value);
	}

	public boolean getHardcoreHearts() {
		return dataMap.get(InternalPlayerData.HARDCORE_HEARTS);
	}

	public void setHardcoreHearts(boolean value) {
		dataMap.set(InternalPlayerData.HARDCORE_HEARTS, value);
	}

	@Override
	public int compareTo(@NotNull PlayerSessionData o) {
		return getName().compareToIgnoreCase(o.getName());
	}

	public void closed() {
	}

	public void respawned(Level level, boolean login) {
	}
}
