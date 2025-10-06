package dev.latvian.mods.vidlib.feature.platform;

import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.feature.clock.Clock;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.util.FormattedCharSinkPartBuilder;
import dev.latvian.mods.vidlib.util.StringUtils;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
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
		var mc = Minecraft.getInstance();

		if (mc.gameRenderer.getMainCamera().getFluidInCamera() != FogType.NONE) {
			var fg = ClientGameEngine.INSTANCE.getFluidFog();
			return fg == null ? shaderFog : fg;
		}

		var fg = ClientGameEngine.INSTANCE.getFog();

		if (mc.player != null && (mc.player.hasEffect(MobEffects.DARKNESS) || mc.player.hasEffect(MobEffects.BLINDNESS))) {
			return shaderFog;
		}

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

	public boolean hasTopInfoBar(Minecraft mc) {
		return mc.player != null && mc.player.vl$sessionData().topInfoBarOverride != null;
	}

	public boolean hasBottomInfoBar(Minecraft mc) {
		for (var clock : Clock.REGISTRY) {
			if (clock.screen().isPresent()) {
				return true;
			}
		}

		return mc.player != null && mc.player.vl$sessionData().bottomInfoBarOverride != null;
	}

	public void topInfoBar(ImGraphics graphics, float x, float y, float w, float h) {
		if (graphics.mc.player != null) {
			var session = graphics.mc.player.vl$sessionData();

			if (session.topInfoBarOverride != null) {
				if (!Empty.isEmpty(session.topInfoBarOverride)) {
					var sink = new FormattedCharSinkPartBuilder();
					graphics.mc.font.split(session.topInfoBarOverride, Integer.MAX_VALUE).getFirst().accept(sink);
					graphics.text(sink.build());
				}
			}
		}
	}

	public void bottomInfoBar(ImGraphics graphics, float x, float y, float w, float h) {
		if (graphics.mc.player != null) {
			var session = graphics.mc.player.vl$sessionData();

			if (session.bottomInfoBarOverride != null) {
				if (!Empty.isEmpty(session.bottomInfoBarOverride)) {
					var sink = new FormattedCharSinkPartBuilder();
					graphics.mc.font.split(session.bottomInfoBarOverride, Integer.MAX_VALUE).getFirst().accept(sink);
					graphics.text(sink.build());
				}

				return;
			}

			for (var clock : Clock.REGISTRY) {
				if (clock.screen().isPresent()) {
					var screen = clock.screen().get();
					var value = session.clocks.get(clock.id());

					if (value != null && screen.visible().test(graphics.mc.player)) {
						var string = screen.format().formatted(value.second() / 60, value.second() % 60);
						var color = screen.color().lerp(switch (value.type()) {
							case FINISHED -> 1F;
							case FLASH -> 0.65F + Mth.cos((session.tick) * 0.85F) * 0.35F;
							default -> 0F;
						}, Clock.RED);

						graphics.pushStack();
						graphics.setStyleCol(ImGuiCol.Text, color);
						ImGui.text(string);
						graphics.popStack();
						return;
					}
				}
			}
		}

		ImGui.text("--:--");

		/*
		graphics.redTextIf(ImIcons.CIRCLE.toString(), true);
		ImGui.sameLine();
		ImGui.text("15:00");
		ImGui.sameLine();
		ImGui.text("|");
		ImGui.sameLine();
		ImGui.progressBar(0.67F, 200F, h - 4F);
		ImGui.sameLine();
		ImGui.text("|");
		ImGui.sameLine();
		ImGui.text("Some more info here");
		 */
	}
}
