package dev.latvian.mods.vidlib.feature.platform;

import com.google.common.collect.ImmutableMap;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.camera.ControlledCameraOverride;
import dev.latvian.mods.vidlib.feature.canvas.BossRendering;
import dev.latvian.mods.vidlib.feature.canvas.Canvas;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.client.VidLibKeys;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.clock.Clock;
import dev.latvian.mods.vidlib.feature.clothing.Clothing;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import dev.latvian.mods.vidlib.feature.decal.Decal;
import dev.latvian.mods.vidlib.feature.feature.Feature;
import dev.latvian.mods.vidlib.feature.icon.IconHolder;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.misc.CameraOverride;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.net.Context;
import dev.latvian.mods.vidlib.feature.net.PacketDebuggerPanel;
import dev.latvian.mods.vidlib.feature.net.VidLibPacketPayloadContainer;
import dev.latvian.mods.vidlib.feature.particle.ChancedParticle;
import dev.latvian.mods.vidlib.feature.skin.PlayerSkinOverrides;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import dev.latvian.mods.vidlib.feature.waypoint.Waypoint;
import dev.latvian.mods.vidlib.util.FormattedCharSinkPartBuilder;
import dev.latvian.mods.vidlib.util.StringUtils;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.particle.FireworkParticles;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.FogParameters;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.sounds.BiomeAmbientSoundsHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class ClientGameEngine {
	public static ClientGameEngine INSTANCE = new ClientGameEngine();

	public void collectClientFeatures(Reference2IntMap<Feature> map) {
		for (var mod : ModList.get().getMods()) {
			map.put(Feature.create(ResourceLocation.fromNamespaceAndPath("mod", mod.getModId())), 1);
		}

		map.put(Feature.INFINITE_CHUNK_RENDERING, 1);
		map.put(Feature.SMALL_GRASS_HITBOX, 1);
		map.put(Feature.SOFT_BARRIERS, 1);
		map.put(Feature.SERVER_DATA, 1);
		map.put(Feature.PLAYER_DATA, 1);
		map.put(Feature.SKYBOX, 1);
	}

	public boolean isClientStaff(Collection<String> tags, GameType gameMode) {
		return CommonGameEngine.INSTANCE.isPlayerStaff(tags, gameMode);
	}

	public boolean canSeeAllPlayersInList(LocalPlayer self) {
		return isClientStaff(self.getTags(), self.getGameMode());
	}

	public boolean canSeePlayerInList(LocalPlayer self, PlayerInfo playerInfo) {
		var sessionData = self.vl$sessionData().getClientSessionData(playerInfo.getProfile().getId());
		return !isClientStaff(sessionData.getTags(), playerInfo.getGameMode());
	}

	public Component getPlayerListName(Minecraft mc, PlayerInfo playerInfo, Component fallback) {
		var nickname = mc.player.vl$sessionData().getClientSessionData(playerInfo.getProfile().getId()).dataMap.get(InternalPlayerData.NICKNAME);
		return Empty.isEmpty(nickname) ? fallback : nickname;
	}

	public boolean isGlowing(Minecraft mc, Entity entity) {
		if (entity instanceof Player player) {
			return player.getOptional(InternalPlayerData.GLOW_COLOR) != null;
		}

		return mc.player != null && mc.player.vl$sessionData().glowColors.get(entity.getUUID()) != null;
	}

	@Nullable
	public Color getTeamColor(Minecraft mc, Entity entity) {
		return mc.player != null ? mc.player.vl$sessionData().glowColors.get(entity.getUUID()) : null;
	}

	public Component getPlayerWorldName(Player player, Component fallback) {
		var nickname = player.getOptional(InternalPlayerData.NICKNAME);
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

	@Nullable
	public SkinTexture overrideSkin(AbstractClientPlayer player) {
		return player.getOptional(InternalPlayerData.SKIN_OVERRIDE);
	}

	@Nullable
	public ResourceLocation overrideCape(Player player) {
		return player.getOptional(InternalPlayerData.CAPE_OVERRIDE);
	}

	public boolean disableCape(Player player) {
		return false;
	}

	@Nullable
	public ResourceLocation overrideElytra(Player player) {
		return player.getOptional(InternalPlayerData.ELYTRA_OVERRIDE);
	}

	public PlayerSkin overridePlayerSkin(AbstractClientPlayer player, PlayerSkin original) {
		var skinOverride = overrideSkin(player);
		var capeOverride = overrideCape(player);
		var elytraOverride = overrideElytra(player);
		boolean disableCape = disableCape(player);

		if (skinOverride == null && capeOverride == null && elytraOverride == null) {
			if (!disableCape || original.capeTexture() == null) {
				return original;
			}
		}

		return new PlayerSkin(
			skinOverride == null ? original.texture() : skinOverride.texture(),
			null,
			disableCape ? null : capeOverride == null ? original.capeTexture() : capeOverride,
			elytraOverride == null ? original.elytraTexture() : elytraOverride,
			skinOverride == null ? original.model() : skinOverride.slim() ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE,
			true
		);
	}

	public ResourceLocation getSkybox(Minecraft mc) {
		return mc.getSkybox();
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
			var fg = getFluidFog();
			return fg == null ? shaderFog : fg;
		}

		var fg = getFog();

		if (mc.player != null && (mc.player.hasEffect(MobEffects.DARKNESS) || mc.player.hasEffect(MobEffects.BLINDNESS))) {
			return shaderFog;
		}

		return fg != null ? fg : shaderFog;
	}

	public List<ChancedParticle> getEnvironmentEffects(Minecraft mc, ClientLevel level, BlockPos pos) {
		return List.of();
	}

	public void handleEnvironmentalEffects(Minecraft mc, ClientLevel level, BlockPos pos) {
		var effects = getEnvironmentEffects(mc, level, pos);
		var ctx = level.getGlobalContext();

		if (!effects.isEmpty()) {
			for (var effect : effects) {
				var chance = effect.chance().getOr(ctx, 0D);

				if (level.random.roll((float) chance)) {
					level.addParticle(
						effect.particle(),
						pos.getX() + level.random.nextFloat(),
						pos.getY() + level.random.nextFloat(),
						pos.getZ() + level.random.nextFloat(),
						0.0, 0.0, 0.0
					);
				}
			}
		}
	}

	public boolean renderOnBossFramebuffer(LivingEntity entity) {
		return entity.level().getMainBoss() == entity;
	}

	public boolean hideRenderedName(LivingEntity entity, boolean bossFramebuffer) {
		return bossFramebuffer && !(entity instanceof Player);
	}

	public boolean hasTopInfoBar(Minecraft mc) {
		return true;
	}

	public boolean hasBottomInfoBar(Minecraft mc) {
		return true;
	}

	public void topInfoBarPre(ImGraphics graphics, float h) {
		if (graphics.mc.player != null) {
			ImGui.text(ClientGameEngine.INSTANCE.getPlayerWorldName(graphics.mc.player, graphics.mc.player.getName()).getString());
		} else {
			ImGui.text(graphics.mc.getUser().getName());
		}

		ImGui.separator();
	}

	public void topInfoBar(ImGraphics graphics, float h) {
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

	public void bottomInfoBar(ImGraphics graphics, float h) {
		var now = new Date();

		graphics.pushStack();
		graphics.setText(ImColorVariant.BLUE);
		ImGui.text(ImIcons.WORLD.toString());
		graphics.popStack();
		ImGui.text(StringUtils.TIMESTAMP_FORMAT.format(now));
		ImGui.separator();
		ImGui.text(graphics.mc.fpsString.split(" ", 2)[0] + " FPS");
		ImGui.separator();

		if (graphics.mc.player != null) {
			var session = graphics.mc.player.vl$sessionData();

			if (session.bottomInfoBarOverride != null) {
				if (!Empty.isEmpty(session.bottomInfoBarOverride)) {
					var sink = new FormattedCharSinkPartBuilder();
					graphics.mc.font.split(session.bottomInfoBarOverride, Integer.MAX_VALUE).getFirst().accept(sink);
					graphics.text(sink.build());
					ImGui.separator();
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
						ImGui.separator();
						return;
					}
				}
			}
		}
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

	public boolean shouldShowName(Entity entity, boolean original) {
		return BossRendering.active <= 0 && original;
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

	public ImmutableMap<ModelLayerLocation, LayerDefinition> customLayerDefinitions(ImmutableMap<ModelLayerLocation, LayerDefinition> original) {
		var map = new HashMap<>(original);
		// map.put(ModelLayers.PLAYER, LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64).apply(HumanoidModel.BABY_TRANSFORMER));
		return ImmutableMap.<ModelLayerLocation, LayerDefinition>builder().putAll(map).build();
	}

	public boolean primitiveF3(Minecraft mc) {
		return false;
	}

	public boolean allowCoordinateDisplay(Minecraft mc) {
		return mc.isLocalServer() || mc.player.hasPermissions(2);
	}

	public boolean hideGui(Minecraft mc) {
		if (mc.options.hideGui) {
			return true;
		} else if (mc.screen != null && mc.screen.hideGui()) {
			return true;
		} else if (mc.player == null) {
			return false;
		}

		var session = mc.player.vl$sessionData();
		return session.cameraOverride != null && session.cameraOverride.hideGui();
	}

	public boolean overrideWaterParticles(Level level, BlockPos pos, FluidState state, RandomSource random) {
		return true;
	}

	public void renderOverlays(Minecraft mc, Gui gui, GuiGraphics graphics, DeltaTracker deltaTracker) {
		if (getRenderSuspendedOverlay() && mc.player.vl$sessionData().suspended) {
			gui.renderTextureOverlay(graphics, Gui.POWDER_SNOW_OUTLINE_LOCATION, 1F);
		}
	}

	public boolean getRenderSuspendedOverlay() {
		return false;
	}

	public boolean getRecordVoiceChat() {
		return false;
	}

	public void copyMainDepth(Minecraft mc) {
	}

	public void copyOutlineDepth(Minecraft mc) {
		if (getEntityOutlineDepth() && mc.levelRenderer.shouldShowEntityOutlines()) {
			if (getEndBatchesBeforeOutline()) {
				var buffers = mc.renderBuffers().bufferSource();
				buffers.endLastBatch();
				endBatchesBeforeOutline(buffers);
			}

			Canvas.WEAK_OUTLINE.copyDepthFrom(mc.getMainRenderTarget());
		}
	}

	public boolean getEndBatchesBeforeOutline() {
		return false;
	}

	public void endBatchesBeforeOutline(MultiBufferSource.BufferSource buffers) {
		buffers.endBatch(RenderType.solid());
		buffers.endBatch(RenderType.endPortal());
		buffers.endBatch(RenderType.endGateway());
		buffers.endBatch(Sheets.solidBlockSheet());
		buffers.endBatch(Sheets.cutoutBlockSheet());
		buffers.endBatch(Sheets.bedSheet());
		buffers.endBatch(Sheets.shulkerBoxSheet());
		buffers.endBatch(Sheets.signSheet());
		buffers.endBatch(Sheets.hangingSignSheet());
		buffers.endBatch(Sheets.chestSheet());
	}

	public boolean getEntityOutlineDepth() {
		return true;
	}

	@Nullable
	public RenderType overrideRenderType(RenderType renderType, boolean isPlayer) {
		if (isPlayer || getStrongEntityOutline()) {
			var tex = renderType.vl$getTexture();

			if (tex != null) {
				Canvas.STRONG_OUTLINE.markActive();
				return VidLibRenderTypes.STRONG_OUTLINE_NO_CULL.apply(tex);
			}
		}

		return null;
	}

	public boolean getStrongEntityOutline() {
		return false;
	}

	public boolean handleClientPacket(Context ctx) {
		if (ctx.payload() instanceof VidLibPacketPayloadContainer container) {
			if (isPacketLoggingEnabled() && container.wrapped().allowDebugLogging()) {
				VidLib.LOGGER.info("S2C Packet '%s' #%,d @ %,d: %s".formatted(ctx.payload().type().id(), ctx.uid(), ctx.remoteGameTime(), ctx.payload()));
			}

			if (PacketDebuggerPanel.INSTANCE.isOpen()) {
				Minecraft.getInstance().execute(() -> PacketDebuggerPanel.INSTANCE.debugPackets.add(new PacketDebuggerPanel.LoggedPacket(ctx.uid(), ctx.remoteGameTime(), container.wrapped())));
			}
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
		list.add("Minecraft " + SharedConstants.getCurrentVersion().getName() + " (" + mc.getLaunchedVersion() + "/" + ClientBrandRetriever.getClientModName() + ")");
		list.add(mc.fpsString);

		var blockpos = mc.getCameraEntity().blockPosition();
		var entity = mc.getCameraEntity();
		var direction = entity.getDirection();
		var chunkpos = new ChunkPos(blockpos);

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
		list.add(String.format(Locale.ROOT, "Chunk: %d %d %d [%d %d in r.%d.%d.mca]", chunkpos.x, SectionPos.blockToSectionCoord(blockpos.getY()), chunkpos.z, chunkpos.getRegionLocalX(), chunkpos.getRegionLocalZ(), chunkpos.getRegionX(), chunkpos.getRegionZ()));
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
				list.add("Biome: " + overlay.printBiome(biome));
			}
		}

		list.add(mc.levelRenderer.getEntityStatistics().replace("E:", "Entities:"));
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

	public void tickPlayerInput(LocalPlayer player, KeyboardInput in) {
		if (player != null && player.vl$sessionData().cameraOverride instanceof ControlledCameraOverride c && c.move(in)) {
			in.keyPresses = Input.EMPTY;
			in.moveVector = Vec2.ZERO;
		} else if (player != null && player.vl$sessionData().suspended) {
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
		return false;
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

	public boolean drawItemStackSize(GuiGraphics graphics, ItemStack stack, Font font, @Nullable String text, int x, int y) {
		if (text != null && text.isEmpty()) {
			return true;
		}

		if (stack.getCount() != 1 || text != null) {
			var s = text != null ? text : MiscClientUtils.formatNumber(stack.getCount());
			MiscClientUtils.drawStackSize(graphics, font, s, x, y, 0xFFFFFFFF, true);
		}

		return true;
	}

	public List<Component> getInformationHUD(Minecraft mc, LocalPlayer player, DeltaTracker deltaTracker) {
		return List.of();
	}

	@Nullable
	public CameraOverride overrideCamera(Minecraft mc) {
		if (mc.screen != null && mc.screen.overrideCamera()) {
			return mc.screen;
		} else if (mc.player != null && mc.player.vl$sessionData().cameraOverride != null) {
			return mc.player.vl$sessionData().cameraOverride;
		} else {
			return null;
		}
	}

	public void handleMarker(String event, @Nullable Tag tag) {
		// VidLib.LOGGER.info("Marker " + event + "/" + tag);
	}

	public List<Waypoint> getWaypoints(Minecraft mc) {
		return mc.getWaypoints();
	}

	public boolean hideDebugCharts() {
		return Minecraft.getInstance().showOnlyReducedInfo();
	}
}
