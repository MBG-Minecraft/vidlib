package dev.latvian.mods.vidlib.feature.platform;

import com.google.common.collect.ImmutableMap;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.feature.clock.Clock;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.util.FormattedCharSinkPartBuilder;
import dev.latvian.mods.vidlib.util.StringUtils;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
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
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.FogType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
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

	public Component getPlayerListName(Minecraft mc, PlayerInfo playerInfo, Component fallback) {
		var nickname = mc.player.vl$sessionData().getClientSessionData(playerInfo.getProfile().getId()).dataMap.get(InternalPlayerData.NICKNAME, mc.getGameTime());
		return Empty.isEmpty(nickname) ? fallback : nickname;
	}

	public boolean isGlowing(Minecraft mc, Entity entity) {
		if (entity instanceof Player player) {
			return player.get(InternalPlayerData.GLOW_COLOR) != null;
		}

		return mc.player != null && mc.player.vl$sessionData().glowColors.get(entity.getUUID()) != null;
	}

	@Nullable
	public Color getTeamColor(Minecraft mc, Entity entity) {
		return mc.player != null ? mc.player.vl$sessionData().glowColors.get(entity.getUUID()) : null;
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

	public void addDecals(List<Decal> list) {
	}

	public WorldBorder getRenderedWorldBorder(Minecraft mc, ClientLevel level) {
		var session = mc.player == null ? null : mc.player.vl$sessionData();

		if (session != null && session.worldBorderOverrideEnd != null) {
			if (session.worldBorderOverride == null) {
				session.worldBorderOverride = new WorldBorder();
			}

			var b = session.worldBorderOverrideEnd;
			double now = level.getGameTime() - 1D + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			boolean moving = false;

			if (session.worldBorderOverrideStart != null) {
				b = session.worldBorderOverrideStart.lerp(now, b);
				moving = b != session.worldBorderOverrideStart && b != session.worldBorderOverrideEnd;
			}

			if (b != null) {
				session.worldBorderOverride.setCenter(b.pos().x, b.pos().z);
				session.worldBorderOverride.setSize(b.size());

				if (moving) {
					session.worldBorderOverride.lerpSizeBetween(b.size(), b.size() + Math.signum(session.worldBorderOverrideEnd.size() - session.worldBorderOverrideStart.size()), 1L);
				} else {
					session.worldBorderOverride.setSize(b.size());
				}

				return session.worldBorderOverride;
			}
		}

		return level.getWorldBorder();
	}

	public boolean shouldShowName(Entity entity) {
		// var mc = Minecraft.getInstance();
		// return entity instanceof LocalPlayer && mc.isLocalServer() && !mc.options.getCameraType().isFirstPerson() || entity.hasCustomName();
		return !entity.isInvisible() && (entity instanceof LocalPlayer || entity.hasCustomName());
	}

	public float depthFar(float renderDistance) {
		return 8192F;
	}

	public int calculateScale(int w, int h) {
		if (VidLibConfig.forceHalfAuto) {
			int i = 1;

			while (i < w && i < h && w / (i + 1) >= 320 && h / (i + 1) >= 240) {
				i++;
			}

			if (i <= 3) {
				return 2;
			} else {
				return (i + 1) / 2;
			}
		}

		return -1;
	}

	public ImmutableMap<ModelLayerLocation, LayerDefinition> customLayerDefinitions(ImmutableMap<ModelLayerLocation, LayerDefinition> original) {
		var map = new HashMap<>(original);
		// map.put(ModelLayers.PLAYER, LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64).apply(HumanoidModel.BABY_TRANSFORMER));
		return ImmutableMap.<ModelLayerLocation, LayerDefinition>builder().putAll(map).build();
	}

	public float getClothingScale(Clothing clothing) {
		return 0.95F;
	}
}
