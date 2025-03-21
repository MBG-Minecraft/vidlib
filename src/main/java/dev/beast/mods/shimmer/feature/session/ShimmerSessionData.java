package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.feature.clothing.Clothing;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataMapValue;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.data.InternalPlayerData;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.entity.EntityOverrideValue;
import dev.beast.mods.shimmer.feature.icon.IconHolder;
import dev.beast.mods.shimmer.feature.input.PlayerInput;
import dev.beast.mods.shimmer.feature.worldsync.WorldSyncAuthResponsePayload;
import dev.beast.mods.shimmer.feature.zone.ZoneInstance;
import dev.beast.mods.shimmer.util.registry.SyncedRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ShimmerSessionData {
	public final UUID uuid;
	public final DataMap dataMap;
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
	public Component namePrefix;
	public boolean nameHidden;
	public float flightSpeedMod;

	public ShimmerSessionData(UUID uuid) {
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
		this.namePrefix = null;
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
		namePrefix = EntityOverride.NAME_PREFIX.get(player);
		nameHidden = EntityOverride.NAME_HIDDEN.get(player, false);
		flightSpeedMod = dataMap.get(InternalPlayerData.FLIGHT_SPEED);

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

	public void updateClockInstance(ResourceLocation id, int tick, boolean ticking) {
	}

	public void updateSessionData(Player self, UUID player, List<DataMapValue> playerData) {
	}

	public void removeSessionData(UUID id) {
	}

	public void updatePlayerTags(UUID player, List<String> tags) {
	}

	public void updateServerData(List<DataMapValue> serverData) {
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + uuid + "]";
	}

	public void refreshBlockZones() {
	}

	public void updateInput(UUID player, PlayerInput input) {
	}

	public void worldSyncAuthResponse(WorldSyncAuthResponsePayload payload) {
	}

	public void updateSkyboxes() {
	}

	public void refreshListedPlayers() {
	}
}
