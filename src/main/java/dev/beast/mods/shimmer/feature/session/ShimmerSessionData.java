package dev.beast.mods.shimmer.feature.session;

import dev.beast.mods.shimmer.feature.clock.ClockFont;
import dev.beast.mods.shimmer.feature.clock.ClockInstance;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataMapValue;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.entity.EntityOverride;
import dev.beast.mods.shimmer.feature.icon.IconHolder;
import dev.beast.mods.shimmer.feature.input.PlayerInput;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

public class ShimmerSessionData {
	public final UUID uuid;
	public final DataMap dataMap;
	public PlayerInput prevInput;
	public PlayerInput input;
	public Boolean glowingOverride;
	public Integer teamColorOverride;
	public double gravityMod;
	public float speedMod;
	public boolean suspended;
	public boolean pvp;
	public IconHolder plumbobIcon;

	public ShimmerSessionData(UUID uuid) {
		this.uuid = uuid;
		this.dataMap = new DataMap(uuid, DataType.PLAYER);
		this.prevInput = PlayerInput.NONE;
		this.input = PlayerInput.NONE;

		this.glowingOverride = null;
		this.teamColorOverride = null;
		this.gravityMod = 1D;
		this.speedMod = 1F;
		this.suspended = false;
		this.pvp = true;
		this.plumbobIcon = IconHolder.EMPTY;
	}

	public void respawned(Level level, boolean loggedIn) {
	}

	public void closed() {
	}

	public void updateOverrides(Player player) {
		glowingOverride = EntityOverride.GLOWING.get(player);
		var teamColorOverrideCol = EntityOverride.TEAM_COLOR.get(player);
		teamColorOverride = teamColorOverrideCol == null ? null : teamColorOverrideCol.rgb();
		gravityMod = EntityOverride.GRAVITY.get(player, 1D);
		speedMod = EntityOverride.SPEED.get(player, 1F);
		suspended = EntityOverride.SUSPENDED.get(player, false);
		pvp = EntityOverride.PVP.get(player, true);
		plumbobIcon = EntityOverride.PLUMBOB.get(player, IconHolder.EMPTY);

		if (plumbobIcon == IconHolder.EMPTY) {
			plumbobIcon = dataMap.get(InternalPlayerData.PLUMBOB);
		}

		if (gravityMod <= 0D) {
			player.resetFallDistance();
		}
	}

	public boolean pvp() {
		return pvp && !suspended;
	}

	public void updateZones(Level level, List<ZoneContainer> update) {
	}

	public void updateClockFonts(List<ClockFont> update) {
	}

	public void updateClocks(Level level, List<ClockInstance> update) {
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
}
