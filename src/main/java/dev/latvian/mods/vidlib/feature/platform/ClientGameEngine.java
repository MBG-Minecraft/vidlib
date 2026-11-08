package dev.latvian.mods.vidlib.feature.platform;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.entity.EntityUtils;
import dev.latvian.mods.klib.icon.Icon;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.klib.util.FormattedCharSinkPartBuilder;
import dev.latvian.mods.klib.util.SkinTexture;
import dev.latvian.mods.klib.util.StringUtils;
import dev.latvian.mods.replay.api.ReplayAPI;
import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.atmosphere.Atmosphere;
import dev.latvian.mods.vidlib.feature.camera.ControlledCameraOverride;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.client.VidLibKeys;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.clothing.ClothedPlayerSkinTexture;
import dev.latvian.mods.vidlib.feature.clothing.PlayerClothing;
import dev.latvian.mods.vidlib.feature.feature.Feature;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.item.VidLibTool;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.PacketDebuggerPanel;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import dev.latvian.mods.vidlib.feature.skin.PlayerSkinOverrides;
import dev.latvian.mods.vidlib.feature.visual.Visuals;
import dev.latvian.mods.vidlib.feature.waypoint.Waypoint;
import dev.mrbeastgaming.mods.hub.api.HubClientSession;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import dev.mrbeastgaming.mods.hub.api.gateway.HubClientGateway;
import imgui.ImGui;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.item.properties.numeric.CompassAngleState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.client.renderer.state.level.WorldBorderRenderState;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class ClientGameEngine {
	public static ClientGameEngine INSTANCE = new ClientGameEngine();

	public static final boolean DISABLE_IMGUI = "true".equals(System.getenv("DISABLE_IMGUI"));

	public void collectClientFeatures(Reference2IntMap<Feature> map) {
		for (var mod : ModList.get().getMods()) {
			map.put(Feature.create(Identifier.fromNamespaceAndPath("mod", mod.getModId())), 1);
		}

		map.put(Feature.INFINITE_CHUNK_RENDERING, 1);
		map.put(Feature.SERVER_DATA, 1);
		map.put(Feature.PLAYER_DATA, 1);
	}

	public List<PlayerInfo> getPlayerInfos(Minecraft mc) {
		if (mc.player == null) {
			return List.of();
		}

		var stream = mc.player.connection.getListedOnlinePlayers().stream();

		if (!canSeeAllPlayersInList(mc.player)) {
			stream = stream.filter(info -> canSeePlayerInList(mc, mc.player, info));
		}

		return stream.sorted(PlayerTabOverlay.PLAYER_COMPARATOR).limit(80L).toList();
	}

	public boolean canSeeAllPlayersInList(LocalPlayer self) {
		return PlayerSessionData.of(self).isStaff();
	}

	public boolean canSeePlayerInList(Minecraft mc, LocalPlayer self, PlayerInfo playerInfo) {
		if (CommonGameEngine.INSTANCE.privacyMode()) {
			return playerInfo.getProfile().id().equals(self.getGameProfile().id());
		}

		return !ClientSessionData.of(mc).getPlayer(playerInfo.getProfile().id()).isStaff();
	}

	public Component getPlayerListName(Minecraft mc, PlayerInfo playerInfo, Component fallback) {
		var nickname = ClientSessionData.of(mc).getPlayer(playerInfo.getProfile().id()).getNickname();
		return Empty.isEmpty(nickname) ? fallback : nickname;
	}

	@Nullable
	public Color getTeamColor(Minecraft mc, Entity entity) {
		var override = ClientSessionData.of(mc).glowColors.get(entity.getUUID());
		return override != null ? override : entity instanceof Player player ? PlayerSessionData.of(player).getGlowColor() : null;
	}

	public boolean isGlowing(Minecraft mc, Entity entity) {
		return entity instanceof Player && VidLibKeys.playerGlowKeyMapping.isDown() && PlayerSessionData.of(mc.player).isStaffOrTalent();
	}

	public Component getPlayerWorldName(Player player, Component fallback) {
		var nickname = PlayerSessionData.of(player).getNickname();
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

	public Ref<Icon> getPlumbob(Player player) {
		return PlayerSessionData.of(player).getPlumbob();
	}

	public PlayerClothing getClothing(AbstractClientPlayer player) {
		return PlayerSessionData.of(player).getClothing();
	}

	@Nullable
	public SkinTexture overrideSkin(AbstractClientPlayer player) {
		return PlayerSessionData.of(player).getSkinOverride();
	}

	@Nullable
	public ClientAsset overrideCape(Player player) {
		return PlayerSessionData.of(player).getCapeOverride();
	}

	public boolean disableCape(Player player) {
		return false;
	}

	@Nullable
	public ClientAsset overrideElytra(Player player) {
		return PlayerSessionData.of(player).getElytraOverride();
	}

	public PlayerSkin overridePlayerSkin(AbstractClientPlayer player, PlayerSkin original) {
		Identifier skinTexture = original.body().id();
		var skinModel = original.model();
		Identifier capeTexture = original.cape() == null ? null : original.cape().id();
		Identifier elytraTexture = original.elytra() == null ? null : original.elytra().id();
		var override = false;

		var skinOverride = overrideSkin(player);

		if (skinOverride != null && (!skinOverride.asset().id().equals(skinTexture) || skinOverride.slim() != (original.model() == PlayerModelType.SLIM))) {
			skinTexture = skinOverride.asset().id();
			skinModel = skinOverride.slim() ? PlayerModelType.SLIM : PlayerModelType.WIDE;
			override = true;
		}

		if (disableCape(player)) {
			if (capeTexture != null) {
				capeTexture = null;
				override = true;
			}
		} else {
			var capeOverride = overrideCape(player);

			if (capeOverride != null && !capeOverride.id().equals(capeTexture)) {
				capeTexture = capeOverride.equals(VidLibTextures.MISSING) ? null : capeOverride.id();
				override = true;
			}
		}

		var elytraOverride = overrideElytra(player);

		if (elytraOverride != null && !elytraOverride.id().equals(elytraTexture)) {
			elytraTexture = elytraOverride.id();
			override = true;
		}

		var replacement = ClothedPlayerSkinTexture.replace(Minecraft.getInstance(), skinTexture, skinModel, getClothing(player));

		if (replacement != null) {
			skinTexture = replacement;
			override = true;
		}

		if (override) {
			return new PlayerSkin(
				new ClientAsset.ResourceTexture(skinTexture),
				capeTexture == null ? null : new ClientAsset.ResourceTexture(capeTexture),
				elytraTexture == null ? null : new ClientAsset.ResourceTexture(elytraTexture),
				skinModel,
				true
			);
		} else {
			return original;
		}
	}

	@Nullable
	public Ref<Atmosphere> getAtmosphere(Minecraft mc) {
		return LevelSessionData.of(mc.level).getAtmosphere();
	}

	public boolean disableFog() {
		return false;
	}

	/**
	 * 26.3 replaced {@code net.minecraft.client.renderer.fog.FogData} with
	 * {@link FogData} (see {@code net.minecraft.client.renderer.fog.FogRenderer}), which is
	 * uploaded into the fog UBO instead of being read by the render system directly.
	 * <p>
	 * A {@code null} return value still means "no override — use vanilla fog".
	 */
	@Nullable
	public FogData getFog() {
		return disableFog() ? disabledFog() : null;
	}

	@Nullable
	public FogData getFluidFog() {
		return null;
	}

	public FogData getShaderFog(FogData original) {
		var mc = Minecraft.getInstance();

		if (mc.gameRenderer.mainCamera().getFluidInCamera() != FogType.NONE) {
			var fg = getFluidFog();
			return fg == null ? original : fg;
		}

		var fg = getFog();

		if (mc.player != null && (mc.player.hasEffect(MobEffects.DARKNESS) || mc.player.hasEffect(MobEffects.BLINDNESS))) {
			return original;
		}

		return fg != null ? fg : original;
	}

	/**
	 * The 26.3 equivalent of the old {@code FogParameters.NO_FOG}: every fog distance is pushed
	 * out to {@code Float.MAX_VALUE} so that no fog is applied at all.
	 */
	public static FogData disabledFog() {
		var fog = new FogData();
		fog.environmentalStart = Float.MAX_VALUE;
		fog.environmentalEnd = Float.MAX_VALUE;
		fog.renderDistanceStart = Float.MAX_VALUE;
		fog.renderDistanceEnd = Float.MAX_VALUE;
		fog.skyEnd = Float.MAX_VALUE;
		fog.cloudEnd = Float.MAX_VALUE;
		return fog;
	}

	/**
	 * Applies a (possibly overridden) {@link FogData} and re-packs the terrain fog UBO slice.
	 * <p>
	 * 26.3 uploads fog data to the GPU through {@code FogRenderer.updateBuffer(FogData)} before
	 * {@code LevelRenderer.render(...)} is entered, so an override has to be written both back into
	 * the render state (which drives the sky pass and the main-target clear colour) and into the
	 * already-uploaded {@link GpuBufferSlice}.
	 */
	public static void applyFog(FogData fog, FogData renderStateFog, @Nullable GpuBufferSlice fogBuffer) {
		renderStateFog.environmentalStart = fog.environmentalStart;
		renderStateFog.environmentalEnd = fog.environmentalEnd;
		renderStateFog.renderDistanceStart = fog.renderDistanceStart;
		renderStateFog.renderDistanceEnd = fog.renderDistanceEnd;
		renderStateFog.skyEnd = fog.skyEnd;
		renderStateFog.cloudEnd = fog.cloudEnd;
		renderStateFog.color.set(fog.color);

		if (fogBuffer == null) {
			return;
		}

		try (GpuBufferSlice.MappedView view = fogBuffer.map(false, true)) {
			ByteBuffer buffer = view.data();
			buffer.position(0);
			Std140Builder.intoBuffer(buffer)
				.putVec4(fog.color)
				.putFloat(fog.environmentalStart)
				.putFloat(fog.environmentalEnd)
				.putFloat(fog.renderDistanceStart)
				.putFloat(fog.renderDistanceEnd)
				.putFloat(fog.skyEnd)
				.putFloat(fog.cloudEnd);
		}
	}

	public List<ChancedParticle> getEnvironmentEffects(Minecraft mc, ClientLevel level, BlockPos pos) {
		return List.of();
	}

	public void handleEnvironmentalEffects(Minecraft mc, ClientLevel level, BlockPos pos) {
		var effects = getEnvironmentEffects(mc, level, pos);
		var ctx = ClientLevelSessionData.of(level).createGlobalContext();

		if (!effects.isEmpty()) {
			var random = level.getRandom();

			for (var effect : effects) {
				var chance = effect.chance().value().getOr(ctx, 0D);

				if (random.nextFloat() < chance) {
					level.addParticle(
						effect.particle(),
						pos.getX() + random.nextFloat(),
						pos.getY() + random.nextFloat(),
						pos.getZ() + random.nextFloat(),
						0.0, 0.0, 0.0
					);
				}
			}
		}
	}

	public boolean hideRenderedName(LivingEntity entity) {
		return false;
	}

	public boolean hasTopInfoBar(Minecraft mc) {
		return true;
	}

	public boolean hasBottomInfoBar(Minecraft mc) {
		return true;
	}

	public void topInfoBarPre(ImGraphics graphics, float h) {
		var session = HubClientSession.CURRENT;
		var hubUser = session.user;
		var hubTooltip = new ArrayList<String>();

		if (hubUser == null) {
			graphics.pushStack();
			graphics.setErrorText();
			ImGui.text(ImIcons.WARNING.toString());

			if (ImGui.isItemHovered()) {
				hubTooltip.add("MrBeast Gaming Hub profile not linked!");
				hubTooltip.add("");
				hubTooltip.add("Click Hub -> Link Profile!");
			}

			graphics.popStack();
		} else {
			var mcProfile = session.minecraftLink;
			var hubProject = session.project;

			graphics.pushStack();
			graphics.setText(mcProfile == null || hubProject == null ? ImColorVariant.YELLOW : ImColorVariant.GREEN);
			ImGui.text(ImIcons.CHECK.toString());
			graphics.popStack();

			if (ImGui.isItemHovered()) {
				hubTooltip.add("MrBeast Gaming Hub: Linked " + ImIcons.CHECK);
				hubTooltip.add("");
				hubTooltip.add("User: " + hubUser.toString());

				hubTooltip.add("- Roles: " + String.join(", ", hubUser.flags().getRoles()));

				hubTooltip.add("");
				hubTooltip.add("Project: " + (hubProject == null ? "Not Configured" : hubProject.toString()));

				var gateway = HubClientGateway.get();

				hubTooltip.add("");
				hubTooltip.add("Gateway: " + (gateway != null ? gateway.status : "Inactive"));

				if (gateway != null) {
					var now = Instant.now();
					hubTooltip.add("- Connected for: " + StringUtils.clock(Duration.between(gateway.connected, now).getSeconds()));
					hubTooltip.add("- Last Pinged: " + StringUtils.clock(Duration.between(gateway.lastPing.time(), now).getSeconds()) + " (" + gateway.lastPing.type() + ")");
					hubTooltip.add("- Last Ponged: " + StringUtils.clock(Duration.between(gateway.lastPong.time(), now).getSeconds()) + " (" + gateway.lastPong.type() + ")");
				}
			}
		}

		if (!hubTooltip.isEmpty()) {
			graphics.tooltip(String.join("\n", hubTooltip));
		}

		ImGui.separator();

		if (graphics.player != null) {
			ImGui.text(ClientGameEngine.INSTANCE.getPlayerWorldName(graphics.player, graphics.player.getName()).getString());
		} else {
			ImGui.text(graphics.mc.getUser().getName());
		}

		ImGui.separator();
	}

	public void topInfoBar(ImGraphics graphics, float h) {
		if (graphics.player != null) {
			if (graphics.session.topInfoBarOverride != null) {
				if (!Empty.isEmpty(graphics.session.topInfoBarOverride)) {
					var sink = new FormattedCharSinkPartBuilder();
					graphics.mc.font.split(graphics.session.topInfoBarOverride, Integer.MAX_VALUE).getFirst().accept(sink);
					graphics.text(sink.build());
				}
			}
		}
	}

	public void bottomInfoBar(ImGraphics graphics, float h) {
		Instant now = null;

		if (graphics.isReplay) {
			var current = ReplayAPI.getActive().getOpenSession();

			if (current != null) {
				var utc = current.getCurrentTime();

				if (utc != null) {
					now = utc;
				}
			}
		} else {
			now = Instant.now();
		}

		graphics.pushStack();
		ImGui.beginGroup();
		graphics.setText(ImColorVariant.BLUE);
		ImGui.text(ImIcons.WORLD.toString());
		graphics.popStack();
		ImGui.text(now == null ? "--:--:--" : StringUtils.SHORT_EST_TIMESTAMP_FORMAT.format(now));
		ImGui.endGroup();

		if (now != null && ImGui.isItemHovered()) {
			graphics.tooltip("EST: " + StringUtils.LONG_EST_TIMESTAMP_FORMAT.format(now) + "\nLOC: " + StringUtils.LONG_LOCAL_TIMESTAMP_FORMAT.format(now));
		}

		ImGui.separator();
		ImGui.text(graphics.mc.getFps() + " FPS");
		ImGui.separator();

		if (graphics.player != null) {
			if (graphics.session.bottomInfoBarOverride != null) {
				if (!Empty.isEmpty(graphics.session.bottomInfoBarOverride)) {
					var sink = new FormattedCharSinkPartBuilder();
					graphics.mc.font.split(graphics.session.bottomInfoBarOverride, Integer.MAX_VALUE).getFirst().accept(sink);
					graphics.text(sink.build());
					ImGui.separator();
				}

				return;
			}

			// Clocks
		}
	}

	@Nullable
	public WorldBorderRenderState overrideRenderedWorldBorder(Minecraft mc) {
		/*
		var session = mc.player == null ? null : mc.player.vl$sessionData();

		if (session != null && session.worldBorderOverrideEnd != null) {
			if (session.worldBorderOverride == null) {
				session.worldBorderOverride = new WorldBorder();
			}

			var b = session.worldBorderOverrideEnd;
			double now = mc.level.getGameTime() - 1D + mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
			boolean moving = false;

			if (session.worldBorderOverrideStart != null) {
				b = session.worldBorderOverrideStart.lerp(now, b);
				moving = b != session.worldBorderOverrideStart && b != session.worldBorderOverrideEnd;
			}

			if (b != null) {
				session.worldBorderOverride.setCenter(b.pos().x, b.pos().z);
				session.worldBorderOverride.setSize(b.size());

				if (moving) {
					session.worldBorderOverride.lerpSizeBetween(b.size(), b.size() + Math.signum(session.worldBorderOverrideEnd.size() - session.worldBorderOverrideStart.size()), 1L, level.getGameTime());
				} else {
					session.worldBorderOverride.setSize(b.size());
				}

				return session.worldBorderOverride;
			}
		}

		return mc.level.getWorldBorder();
		 */

		return null;
	}

	public boolean cancelBlockHighlight(LevelRenderState levelState) {
		var state = levelState.blockOutlineRenderState;

		if (state == null) {
			return false;
		}

		var mc = Minecraft.getInstance();

		if (mc.player == null || mc.level == null) {
			return false;
		}

		var blockPos = state.pos();

		if (!EntityUtils.isSpectatorOrCreative(mc.player) && mc.level.getBlockState(blockPos).is(Blocks.BARRIER)) {
			return true;
		}

		var tool = VidLibTool.of(mc.player);

		if (tool != null) {
			var tempVisuals = new Visuals();
			tool.getSecond().visuals(mc.player, tool.getFirst(), tempVisuals, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true));

			if (tempVisuals.contains(blockPos)) {
				return true;
			}
		}

		return false;
	}

	public boolean shouldShowName(Entity entity, boolean original) {
		return original;
	}

	public float getFarDepth(float renderDistance) {
		return 8192F;
	}

	public int calculateScale(int w, int h) {
		if (VidLibClientOptions.LOCK_GUI_SCALE.get()) {
			int i = 1;

			while (i < w && i < h && w / (i + 1) >= 320 && h / (i + 1) >= 240) {
				i++;
			}

			return Math.max(2, i - 1);
		}

		return -1;
	}

	public boolean primitiveF3(Minecraft mc) {
		return false;
	}

	public boolean allowCoordinateDisplay(Minecraft mc) {
		return mc.isLocalServer() || mc.player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);
	}

	public boolean hideGui(Minecraft mc) {
		if (mc.gui.hud.isHidden()) {
			return true;
		} else if (mc.player == null) {
			return false;
		}

		var session = ClientSessionData.of(mc);
		return session.cameraOverride != null && session.cameraOverride.hideGui();
	}

	public boolean renderHardcoreHearts(boolean original) {
		if (original) {
			return true;
		}

		var session = ClientSessionData.of(Minecraft.getInstance());
		return session.localPlayer.getHardcoreHearts();
	}

	public boolean hideCrosshair(Minecraft mc) {
		var vehicle = mc.player == null ? null : mc.player.getVehicle();

		if (vehicle != null && vehicle.hideCrosshair(mc.player)) {
			return true;
		}

		return hideGui(mc);
	}

	public boolean renderSpectatedUI(Minecraft mc) {
		return MiscClientUtils.SPECTATE_UI.get();
	}

	public boolean overrideWaterParticles(Level level, BlockPos pos, FluidState state, RandomSource random) {
		return true;
	}

	public void renderOverlays(Minecraft mc, Gui gui, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		if (getRenderSuspendedOverlay() && CommonGameEngine.INSTANCE.isSuspended(mc.player)) {
			renderSuspendedOverlay(mc, graphics);
		}
	}

	/**
	 * The former {@code Gui.POWDER_SNOW_OUTLINE_LOCATION} — 26.3 removed both the constant and
	 * {@code Gui.renderTextureOverlay(...)}, so the full-screen tint is drawn directly with
	 * {@link GuiGraphicsExtractor#blit}.
	 */
	private static final Identifier SUSPENDED_OVERLAY_LOCATION = Identifier.withDefaultNamespace("textures/misc/powder_snow_outline.png");

	private static void renderSuspendedOverlay(Minecraft mc, GuiGraphicsExtractor graphics) {
		var texture = mc.getTextureManager().getTexture(SUSPENDED_OVERLAY_LOCATION);
		graphics.blit(
			texture.getTextureView(),
			texture.getSampler(),
			0,
			0,
			graphics.guiWidth(),
			graphics.guiHeight(),
			0F,
			1F,
			0F,
			1F
		);
	}

	public boolean getRenderSuspendedOverlay() {
		return false;
	}

	public boolean getRecordVoiceChat() {
		return false;
	}

	public boolean handleClientPacket(Context ctx) {
		if (isPacketLoggingEnabled() && ctx.payload().allowDebugLogging()) {
			VidLib.LOGGER.info("S2C Packet '%s' @ %s".formatted(ctx.payload().type().id(), ctx.payload()));
		}

		if (PacketDebuggerPanel.INSTANCE.isOpen()) {
			var timestamp = Instant.now();
			var gameTime = ctx.level().getGameTime();
			Minecraft.getInstance().execute(() -> PacketDebuggerPanel.INSTANCE.debugPackets.add(new PacketDebuggerPanel.LoggedPacket(timestamp, gameTime, ctx.payload())));
		}

		return true;
	}

	public boolean isPacketLoggingEnabled() {
		return false;
	}

	public boolean disableLightningSounds(LightningBolt entity) {
		return false;
	}

	public OptionInstance<?>[] insertControlsOptions() {
		return VidLibClientOptions.CONTROLS_OPTIONS;
	}

	public OptionInstance<?>[] insertAccessibilityOptions() {
		return VidLibClientOptions.ACCESSIBILITY_OPTIONS;
	}

	public boolean overrideBiomeMoodSounds(BiomeAmbientSoundsHandler handler) {
		return true;
	}

	@Nullable
	public List<String> collectGameInformationText(Minecraft mc, DebugScreenOverlay overlay) {
		if (!mc.showOnlyReducedInfo()) {
			return null;
		}

		var list = new ArrayList<String>();
		list.add("Minecraft " + SharedConstants.getCurrentVersion().name() + " (" + mc.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")");
		list.add(mc.getFps() + " fps");

		var blockpos = mc.getCameraEntity().blockPosition();
		var entity = mc.getCameraEntity();
		var direction = entity.getDirection();
		var chunkpos = new ChunkPos(blockpos.getX(), blockpos.getZ());

		if (!Objects.equals(overlay.lastPos, chunkpos)) {
			overlay.lastPos = chunkpos;
			overlay.clearChunkCache();
		}

		String s;
		switch (direction) {
			case NORTH -> s = "Towards negative Z";
			case SOUTH -> s = "Towards positive Z";
			case WEST -> s = "Towards negative X";
			case EAST -> s = "Towards positive X";
			default -> s = "Invalid";
		}

		list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, s, Mth.wrapDegrees(entity.getYRot()), Mth.wrapDegrees(entity.getXRot())));
		list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", mc.getCameraEntity().getX(), mc.getCameraEntity().getY(), mc.getCameraEntity().getZ()));
		list.add(String.format(Locale.ROOT, "Chunk: %d %d %d [%d %d in r.%d.%d.mca]", chunkpos.x(), SectionPos.blockToSectionCoord(blockpos.getY()), chunkpos.z(), chunkpos.getRegionLocalX(), chunkpos.getRegionLocalZ(), chunkpos.getRegionX(), chunkpos.getRegionZ()));
		list.add(String.format(Locale.ROOT, "Block: %d %d %d [%d %d %d]", blockpos.getX(), blockpos.getY(), blockpos.getZ(), blockpos.getX() & 15, blockpos.getY() & 15, blockpos.getZ() & 15));

		var levelchunk = overlay.getClientChunk();

		if (levelchunk == null || levelchunk.isEmpty()) {
			list.add("Waiting for chunk...");
		} else {
			int rawBrightness = mc.level.getChunkSource().getLightEngine().getRawBrightness(blockpos, 0);
			int skyLight = mc.level.getBrightness(LightLayer.SKY, blockpos);
			int blockLight = mc.level.getBrightness(LightLayer.BLOCK, blockpos);
			list.add("Client Light: " + rawBrightness + " (" + skyLight + " sky, " + blockLight + " block)");

			if (mc.level.isInsideBuildHeight(blockpos.getY())) {
				var biome = mc.level.getBiome(blockpos);
				list.add("Biome: " + biome.unwrap().map(key -> key.identifier().toString(), l -> "[unregistered " + l + "]"));
			}
		}

		var entityStatistics = mc.levelExtractor.entityStatistics();

		if (entityStatistics != null) {
			list.add(entityStatistics.replace("E:", "Entities:"));
		}

		return list;
	}

	@Nullable
	public SoundEvent overrideFireworkSound(FireworkParticles.Starter firework, SoundEvent original) {
		return null;
	}

	@Nullable
	public Float overrideNightVisionScale(LivingEntity entity, float delta) {
		// Removes the night vision fade in/out effect nearing the end of the effect
		var effect = entity.getEffect(MobEffects.NIGHT_VISION);

		if (effect == null) {
			return null;
		}

		int duration = effect.getDuration();

		if (effect.isInfiniteDuration()) {
			return null;
		}

		return duration > 20F ? 1F : duration / 20F;
	}

	public boolean handleDebugKeys(Minecraft mc, int key) {
		return VidLibKeys.handleDebugKeys(mc, key);
	}

	public void tickPlayerInput(Minecraft mc, LocalPlayer player, KeyboardInput in) {
		if (ClientSessionData.of(mc).cameraOverride instanceof ControlledCameraOverride c && c.move(in)) {
			in.keyPresses = Input.EMPTY;
			in.moveVector = Vec2.ZERO;
		} else if (CommonGameEngine.INSTANCE.isSuspended(player)) {
			in.keyPresses = in.keyPresses.shift() ? new Input(
				false,
				false,
				false,
				false,
				false,
				true,
				false
			) : Input.EMPTY;

			in.moveVector = Vec2.ZERO;
		}
	}

	public boolean overrideLevelEvent(Level level, int eventId, BlockPos pos, int data) {
		// Cancel sound that plays when you switch dimensions
		if (eventId == 1032) {
			return true;
		}

		return false;
	}

	public boolean overrideServerPingPlayers(ServerData serverData, ServerStatus.Players players) {
		if (hideServerPingPlayers()) {
			serverData.status = CommonComponents.EMPTY;
			serverData.players = null;
			serverData.playerList = List.of();
			return true;
		}

		return false;
	}

	public boolean hideServerPingPlayers() {
		return CommonGameEngine.INSTANCE.privacyMode();
	}

	@Nullable
	public MinecraftProfileTexture overridePlayerTexture(UUID uuid, MinecraftProfileTexture.Type type) {
		return PlayerSkinOverrides.get(uuid, type);
	}

	@Nullable
	public Biome.Precipitation overrideGlobalVisualPrecipitation(ClientLevel level, float partialTick, Vec3 cameraPosition) {
		var cam = overrideCamera(Minecraft.getInstance());
		return cam == null ? null : cam.getWeatherOverride();
	}

	public boolean drawItemStackSize(GuiGraphicsExtractor graphics, ItemStack stack, Font font, @Nullable String text, int x, int y) {
		if (text != null && text.isEmpty()) {
			return true;
		}

		if (stack.getCount() != 1 || text != null) {
			var s = text != null ? text : StringUtils.formatPower(stack.getCount());
			MiscClientUtils.drawStackSize(graphics, font, s, x, y, 0xFFFFFFFF, true);
		}

		return true;
	}

	public List<Component> getInformationHUD(Minecraft mc, LocalPlayer player, DeltaTracker deltaTracker) {
		return List.of();
	}

	@Nullable
	public CameraOverride overrideCamera(Minecraft mc) {
		if (mc.gui.screen() != null && mc.gui.screen().overrideCamera()) {
			return mc.gui.screen();
		} else if (mc.player != null) {
			var session = ClientSessionData.of(mc);

			if (session.cameraOverride != null) {
				return session.cameraOverride;
			}
		}

		return null;
	}

	@Nullable
	public ReplayMarkerData handleRuntimeMarker(ReplayMarkerData data) {
		return data;
	}

	@Nullable
	public ReplayMarkerData handleReplayMarker(ReplayMarkerData data) {
		return data;
	}

	public List<Waypoint> getWaypoints(Minecraft mc) {
		return LevelSessionData.of(mc.level).getWaypoints();
	}

	public boolean hideDebugCharts() {
		return Minecraft.getInstance().showOnlyReducedInfo();
	}

	public double overrideCompassAngle(CompassAngleState.CompassTarget type, Entity entity, BlockPos target) {
		return Double.NaN;
	}

	@Nullable
	public List<String> overrideCommandOnlinePlayerNames() {
		if (!CommonGameEngine.INSTANCE.privacyMode()) {
			return null;
		}

		var mc = Minecraft.getInstance();
		var connection = mc.getConnection();

		if (connection == null) {
			return List.of();
		} else if (mc.player != null && canViewOnlinePlayerNames(mc.player)) {
			return connection.getOnlinePlayers().stream().map(p -> p.getProfile().name()).toList();
		} else {
			return List.of(mc.getUser().getName());
		}
	}

	public boolean canViewOnlinePlayerNames(LocalPlayer player) {
		return PlayerSessionData.of(player).isStaffOrTalent();
	}

	public boolean enableSinglePlayerMainMenuButton() {
		return HubUserCapabilities.get().singleplayer();
	}

	public boolean enableMultiPlayerMainMenuButton() {
		return HubUserCapabilities.get().multiplayer();
	}

	public boolean enableReplayMainMenuButton() {
		return HubUserCapabilities.get().viewLocalReplays();
	}

	public boolean isVoiceChatPTTDown() {
		return false;
	}

	public boolean shouldRenderHand(Minecraft mc) {
		if (overrideCamera(mc) != null) {
			return false;
		}

		return mc.player == null || mc.player.getVehicle() == null || mc.player.getVehicle().shouldRenderPassengerHand(mc.player);
	}

	public boolean hideVoiceChatPlayerList() {
		return CommonGameEngine.INSTANCE.privacyMode();
	}

	public boolean removeChatFromSleepScreen() {
		return CommonGameEngine.INSTANCE.privacyMode();
	}

	public boolean disableToast(Toast toast) {
		return toast instanceof TutorialToast || toast instanceof AdvancementToast || toast instanceof RecipeToast;
	}

	@Nullable
	public Component blockedScreenText(LocalPlayer player) {
		return null;
	}

	public boolean allowAdminPanel(@Nullable LocalPlayer player) {
		return HubUserCapabilities.get().adminPanel();
	}

	public boolean shouldRender2DPlayerName(Minecraft mc, LocalPlayer self, Player player) {
		if (self == player && mc.options.getCameraType().isFirstPerson()) {
			return false;
		}

		if (player.isSpectator()) {
			return false;
		}

		return !player.isInvisibleTo(self);
	}

	public boolean imGuiOpenMenu(ImGraphics graphics) {
		return graphics.adminPanel;
	}

	public boolean imGuiConfigMenu(ImGraphics graphics) {
		return graphics.adminPanel;
	}

	public boolean imGuiDebugMenu(ImGraphics graphics) {
		return graphics.adminPanel;
	}

	public boolean imGuiShowMenu(ImGraphics graphics) {
		return graphics.adminPanel;
	}

	public boolean imGuiWarpMenu(ImGraphics graphics) {
		return graphics.adminPanel;
	}

	public boolean hideActionBarText(Minecraft mc, Component component) {
		return false;
	}

	public boolean defaultDrawName(Player player) {
		return true;
	}

	public boolean defaultDrawHealthBar(Player player) {
		return EntityUtils.isSurvivalLike(player);
	}

	public String getClientGatewayName(Minecraft mc) {
		return mc.getUser().getName();
	}

	public String getClientGatewayStatus(Minecraft mc) {
		var server = mc.getCurrentServer();

		if (server != null) {
			return "Server - " + server.name;
		} else if (mc.level != null && ClientSessionData.of(mc).isReplay) {
			return "Replay Editor";
		} else if (mc.level != null) {
			return "Singleplayer";
		} else {
			return "Main Menu";
		}
	}

	public boolean shouldRenderDarkDisc(float partialTick, ClientLevel level, boolean original) {
		return false;
	}

	public void handleIncreaseKey(Minecraft mc) {
		mc.options.renderDistance().values().validateValue(mc.options.renderDistance().get() + 1).ifPresent(v -> {
			mc.options.renderDistance().set(v);
			mc.status(String.valueOf(v));
		});
	}

	public void handleDecreaseKey(Minecraft mc) {
		mc.options.renderDistance().values().validateValue(mc.options.renderDistance().get() - 1).ifPresent(v -> {
			mc.options.renderDistance().set(v);
			mc.status(String.valueOf(v));
		});
	}

	public float getAmbientLight(float fallback) {
		var player = Minecraft.getInstance().player;

		if (player != null) {
			float value = PlayerSessionData.of(player).getBrightnessOverride();

			if (value > 0F) {
				return value;
			}
		}

		return fallback;
	}
}
