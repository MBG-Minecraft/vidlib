package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.vidlib.feature.clock.ClockValue;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.data.DataMap;
import dev.latvian.mods.vidlib.feature.data.DataMapValue;
import dev.latvian.mods.vidlib.feature.data.DataType;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import dev.latvian.mods.vidlib.feature.entity.EntityOverrideValue;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.input.PlayerInput;
import dev.latvian.mods.vidlib.feature.registry.SyncedRegistry;
import dev.latvian.mods.vidlib.feature.zone.ZoneInstance;
import dev.latvian.mods.vidlib.util.Empty;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SessionData {
	public final UUID uuid;
	public final DataMap dataMap;
	public int tick;
	public PlayerInput prevInput;
	public PlayerInput input;
	public List<ZoneInstance> zonesIn;
	public Set<String> zonesTagsIn;
	public Map<EntityOverride<?>, EntityOverrideValue<?>> entityOverridesMap;

	public Boolean glowingOverride;
	public Integer teamColorOverride;
	public boolean suspended;
	public double gravityMod;
	public float speedMod;
	public float attackDamageMod;
	public boolean pvp;
	public IconHolder plumbobIcon;
	public Clothing clothing;
	public boolean unpushable;
	public Component nickname;
	public Component namePrefix;
	public Component nameSuffix;
	public Component scoreText;
	public boolean nameHidden;
	public float flightSpeedMod;

	public SessionData(UUID uuid) {
		this.uuid = uuid;
		this.dataMap = new DataMap(uuid, DataType.PLAYER);
		this.prevInput = PlayerInput.NONE;
		this.input = PlayerInput.NONE;
		this.zonesIn = List.of();
		this.zonesTagsIn = Set.of();

		this.glowingOverride = null;
		this.teamColorOverride = null;
		this.suspended = false;
		this.gravityMod = 1D;
		this.speedMod = 1F;
		this.attackDamageMod = 1F;
		this.pvp = true;
		this.plumbobIcon = IconHolder.EMPTY;
		this.clothing = null;
		this.unpushable = false;
		this.nickname = null;
		this.namePrefix = null;
		this.nameSuffix = null;
		this.scoreText = null;
		this.nameHidden = false;
		this.flightSpeedMod = 1F;
	}

	public void respawned(Level level, boolean loggedIn) {
	}

	public void closed() {
	}

	public void updateOverrides(Player player) {
		glowingOverride = EntityOverride.GLOWING.get(player);
		var teamColorOverrideCol = EntityOverride.TEAM_COLOR.get(player);
		teamColorOverride = teamColorOverrideCol == null ? null : teamColorOverrideCol.rgb();
		suspended = EntityOverride.SUSPENDED.get(player, null, InternalPlayerData.SUSPENDED);
		gravityMod = suspended ? 0F : EntityOverride.GRAVITY.get(player, 1D);
		speedMod = suspended ? 0F : EntityOverride.SPEED.get(player, 1F);
		attackDamageMod = suspended ? 0F : EntityOverride.ATTACK_DAMAGE.get(player, 1F);
		pvp = !suspended && EntityOverride.PVP.get(player, true);
		plumbobIcon = EntityOverride.PLUMBOB.get(player, IconHolder.EMPTY, InternalPlayerData.PLUMBOB);
		clothing = EntityOverride.CLOTHING.get(player, Clothing.NONE, InternalPlayerData.CLOTHING);
		unpushable = suspended || EntityOverride.UNPUSHABLE.get(player, false);
		nickname = EntityOverride.NICKNAME.get(player, Empty.COMPONENT, InternalPlayerData.NICKNAME);
		namePrefix = EntityOverride.NAME_PREFIX.get(player);
		nameSuffix = EntityOverride.NAME_SUFFIX.get(player);
		scoreText = EntityOverride.SCORE_TEXT.get(player);
		nameHidden = EntityOverride.NAME_HIDDEN.get(player, false);
		flightSpeedMod = player.get(InternalPlayerData.FLIGHT_SPEED);

		if (gravityMod <= 0D) {
			player.resetFallDistance();
		}

		if (suspended) {
			player.setDeltaMovement(Vec3.ZERO);
		}
	}

	public <V> void syncRegistry(Player player, SyncedRegistry<V> registry, Map<ResourceLocation, V> map) {
	}

	public void updateZones(Level level) {
	}

	public void updateClocks(Map<ResourceLocation, ClockValue> map) {
	}

	public void updateServerData(long gameTime, Player self, List<DataMapValue> update) {
	}

	public void updatePlayerData(long gameTime, Player self, UUID player, List<DataMapValue> update) {
	}

	public void updatePlayerTags(long gameTime, Player self, UUID player, List<String> tags) {
	}

	public void removeSessionData(UUID id) {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + uuid + "]";
	}

	public void refreshBlockZones() {
	}

	public void updateInput(Level level, UUID player, PlayerInput input) {
	}

	public void updateSkyboxes() {
	}

	public void refreshListedPlayers() {
	}
}
