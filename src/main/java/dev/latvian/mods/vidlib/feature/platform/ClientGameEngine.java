package dev.latvian.mods.vidlib.feature.platform;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.util.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.FogType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ClientGameEngine {
	public static ClientGameEngine INSTANCE = new ClientGameEngine();

	public boolean isStaff(Collection<String> tags, GameType gameMode) {
		return gameMode == GameType.SPECTATOR || tags.contains("staff");
	}

	public boolean canSeeAllPlayersInList(LocalPlayer self) {
		return isStaff(self.getTags(), self.getGameMode());
	}

	public boolean canSeePlayerInList(LocalPlayer self, PlayerInfo playerInfo) {
		var sessionData = self.vl$sessionData().getClientSessionData(playerInfo.getProfile().getId());
		return !isStaff(sessionData.getTags(self.level().getGameTime()), playerInfo.getGameMode());
	}

	public Component getPlayerListName(Minecraft minecraft, PlayerInfo playerInfo, Component fallback) {
		var nickname = minecraft.player.vl$sessionData().getClientSessionData(playerInfo.getProfile().getId()).dataMap.get(InternalPlayerData.NICKNAME, minecraft.getGameTime());
		return Empty.isEmpty(nickname) ? fallback : nickname;
	}

	public boolean isGlowing(Entity entity) {
		return false;
	}

	@Nullable
	public Color getTeamColor(Entity entity) {
		return null;
	}

	public Component getPlayerWorldName(Player player, Component fallback) {
		var nickname = player.get(InternalPlayerData.NICKNAME);
		return Empty.isEmpty(nickname) ? fallback : nickname;
	}

	@Nullable
	public Component getPlayerWorldNamePrefix(Player player) {
		return null;
	}

	@Nullable
	public Component getPlayerWorldNameSuffix(Player player) {
		return null;
	}

	public Component getFullPlayerWorldName(Player player, Component fallback) {
		return StringUtils.buildComponent(
			getPlayerWorldNamePrefix(player),
			getPlayerWorldName(player, fallback),
			getPlayerWorldNameSuffix(player)
		);
	}

	@Nullable
	public Component getScoreText(Player player) {
		return null;
	}

	public IconHolder getPlumbob(Player player) {
		return player.get(InternalPlayerData.PLUMBOB);
	}

	public Clothing getClothing(Player player) {
		return player.get(InternalPlayerData.CLOTHING);
	}

	public ResourceLocation getSkybox(Minecraft mc) {
		return mc.getSkybox();
	}

	public float getAmbientLight(float fallback) {
		return fallback;
	}

	@Nullable
	public FogParameters getFog() {
		return FogParameters.NO_FOG;
	}

	@Nullable
	public FogParameters getFluidFog() {
		return null;
	}

	public FogParameters getShaderFog(FogParameters shaderFog) {
		if (Minecraft.getInstance().gameRenderer.getMainCamera().getFluidInCamera() != FogType.NONE) {
			var fg = ClientGameEngine.INSTANCE.getFluidFog();
			return fg == null ? shaderFog : fg;
		}

		var fg = ClientGameEngine.INSTANCE.getFog();
		return fg != null ? fg : shaderFog;
	}

	public List<ChancedParticle> getEnvironmentEffects(Minecraft mc, BlockPos pos) {
		return List.of();
	}

	public boolean renderOnBossFramebuffer(LivingEntity entity) {
		return entity.level().getMainBoss() == entity;
	}

	public boolean hideRenderedName(LivingEntity entity, boolean bossFramebuffer) {
		return bossFramebuffer && !(entity instanceof Player);
	}
}
