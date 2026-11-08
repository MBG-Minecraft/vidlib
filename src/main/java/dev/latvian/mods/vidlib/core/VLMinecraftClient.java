package dev.latvian.mods.vidlib.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.knumber.KNumberVariables;
import dev.latvian.mods.klib.math.Rotation;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.texture.SpriteKey;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.replay.api.ReplayMarkerData;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.camera.DetachedCamera;
import dev.latvian.mods.vidlib.feature.camera.FreeCamera;
import dev.latvian.mods.vidlib.feature.camera.ScreenShake;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeInstance;
import dev.latvian.mods.vidlib.feature.cutscene.ClientCutscene;
import dev.latvian.mods.vidlib.feature.cutscene.Cutscene;
import dev.latvian.mods.vidlib.feature.cutscene.CutsceneScreen;
import dev.latvian.mods.vidlib.feature.data.DataKey;
import dev.latvian.mods.vidlib.feature.data.DataMapValue;
import dev.latvian.mods.vidlib.feature.data.UpdateGlobalDataValuePayload;
import dev.latvian.mods.vidlib.feature.data.UpdateLevelDataValuePayload;
import dev.latvian.mods.vidlib.feature.data.UpdatePlayerDataValuePayload;
import dev.latvian.mods.vidlib.feature.misc.MainMenuOpenedEvent;
import dev.latvian.mods.vidlib.feature.particle.ShapeParticleOptions;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticleManager;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticles;
import dev.latvian.mods.vidlib.feature.particle.physics.PhysicsParticlesDisplayData;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.Fade;
import dev.latvian.mods.vidlib.feature.screeneffect.fade.ScreenFadeInstance;
import dev.latvian.mods.vidlib.feature.session.ClientLevelSessionData;
import dev.latvian.mods.vidlib.feature.session.ClientSessionData;
import dev.latvian.mods.vidlib.feature.session.GlobalSessionData;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import dev.latvian.mods.vidlib.feature.sound.PositionedSoundData;
import dev.latvian.mods.vidlib.feature.sound.VidLibSoundInstance;
import dev.latvian.mods.vidlib.feature.vote.NumberVotingScreen;
import dev.latvian.mods.vidlib.feature.vote.YesNoVotingScreen;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("resource")
public interface VLMinecraftClient extends VLMinecraftEnvironment {
	Cache<String, SystemToast.SystemToastId> SYSTEM_TOAST_IDS = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();

	default Minecraft vl$self() {
		return (Minecraft) this;
	}

	@Override
	default boolean isClientEnvironment() {
		return true;
	}

	default ClientSessionData vl$getGlobalData() {
		throw new NoMixinException(this);
	}

	default void vl$setGlobalData(@Nullable ClientSessionData data) {
		throw new NoMixinException(this);
	}

	@Override
	default void c2s(@Nullable Packet<? super ServerGamePacketListener> packet) {
		if (packet != null) {
			vl$self().getConnection().send(packet);
		}
	}

	@Override
	default ClientLevel vl$level() {
		return vl$self().level;
	}

	@Override
	default void tell(Component message) {
		vl$self().player.sendSystemMessage(message);
	}

	@Override
	default void status(Component message) {
		vl$self().player.sendOverlayMessage(message);
	}

	@Override
	default void toast(String uniqueId, long displayTime, Component title, Component description) {
		SystemToast.SystemToastId id;

		if (uniqueId.isEmpty()) {
			id = new SystemToast.SystemToastId(displayTime);
		} else {
			try {
				id = SYSTEM_TOAST_IDS.get(uniqueId, () -> new SystemToast.SystemToastId(displayTime));
			} catch (Exception e) {
				id = new SystemToast.SystemToastId(displayTime);
			}
		}

		SystemToast.addOrUpdate(vl$self().gui.toastManager(), id, title, description);
	}

	@Override
	default void playCutscene(Ref<Cutscene> cutscene, KNumberVariables variables) {
		var mc = vl$self();
		var session = ClientSessionData.of(mc);
		var player = mc.player;
		var c = cutscene.value();

		if (!c.steps().isEmpty() && player != null) {
			var overrideCamera = !session.isReplay;
			var inst = new ClientCutscene(mc, overrideCamera, c, variables, mc.player::getEyePosition);
			session.currentCutscene = inst;
			session.cameraOverride = inst;

			if (overrideCamera && !c.allowMovement()) {
				mc.gui.setScreen(new CutsceneScreen(inst, mc.gui.screen()));
			}
		}
	}

	@Override
	default void stopCutscene() {
		var mc = vl$self();
		var session = ClientSessionData.of(mc);

		if (session.currentCutscene != null) {
			session.currentCutscene.stopped();
		}

		session.cameraOverride = null;
		session.currentCutscene = null;

		if (mc.gui.screen() instanceof CutsceneScreen screen) {
			mc.gui.setScreen(screen.previousScreen);
		}
	}

	@Override
	default void screenShake(ScreenShake shake) {
		var mc = vl$self();
		var session = ClientSessionData.of(mc);

		if (shake.skip() || session.isReplay) {
			return;
		}

		session.screenShakeInstances.add(new ScreenShakeInstance(shake));

		if (shake.motionBlur()) {
			vl$self().player.setActivePostEffects(List.of(ScreenShake.MOTION_BLUR_EFFECT));
		}
	}

	@Override
	default void screenShake(ScreenShake shake, Vec3 source, double maxDistance) {
		screenShake(shake.atDistance(vl$self().gameRenderer.mainCamera().position(), source, maxDistance));
	}

	@Override
	default void stopScreenShake() {
		var mc = vl$self();
		var session = ClientSessionData.of(mc);
		session.screenShakeInstances.clear();
	}

	@Override
	default void setCameraMode(int mode) {
		var mc = vl$self();
		var session = ClientSessionData.of(mc);

		if (session.currentCutscene != null) {
			stopCutscene();
		}

		if (mode == 3) {
			int currentMode = 0;

			if (session.cameraOverride instanceof DetachedCamera) {
				currentMode = 1;
			} else if (session.cameraOverride instanceof FreeCamera) {
				currentMode = 2;
			}

			if (currentMode == 1) {
				mode = 2;
			} else if (currentMode == 2) {
				mode = 1;
			} else {
				mode = 2;
			}
		}

		switch (mode) {
			case 1 -> {
				if (session.cameraOverride instanceof FreeCamera c) {
					session.cameraOverride = new DetachedCamera(c.position, c.rotation);
				} else {
					session.cameraOverride = new DetachedCamera(mc.player.getEyePosition(), Rotation.of(mc.player, 1F));
				}
			}
			case 2 -> {
				if (session.cameraOverride instanceof DetachedCamera(Vec3 position, Rotation rotation)) {
					session.cameraOverride = new FreeCamera(position, rotation);
				} else {
					session.cameraOverride = new FreeCamera(mc.player.getEyePosition(), Rotation.of(mc.player, 1F));
				}
			}
			default -> stopCutscene();
		}
	}

	@Override
	default void setPostEffect(Identifier id) {
		if (id.equals(Identifier.withDefaultNamespace("empty"))) {
			vl$self().player.setActivePostEffects(List.of());
		} else {
			vl$self().player.setActivePostEffects(List.of(id));
		}
	}

	@Override
	default void vl$closeScreen() {
		vl$self().gui.setScreen(null);
	}

	@Override
	default void openYesNoVotingScreen(CompoundTag extraData, Component title, Component subtitle, Component yesLabel, Component noLabel, boolean closeOnVote) {
		vl$self().gui.setScreen(new YesNoVotingScreen(extraData, title, subtitle, yesLabel, noLabel, closeOnVote));
	}

	@Override
	default void openNumberVotingScreen(CompoundTag extraData, Component title, Component subtitle, int max, IntList unavailable, boolean closeOnVote) {
		vl$self().gui.setScreen(new NumberVotingScreen(extraData, title, subtitle, max, unavailable, closeOnVote));
	}

	@Override
	default void removeAllParticles() {
		vl$self().particleEngine.setLevel(vl$self().level);
		PhysicsParticleManager.clearAllParticles();
	}

	@Override
	default void redrawSections(LongList sections, boolean mainThread) {
		var levelExtractor = vl$self().levelExtractor;

		for (long section : sections) {
			levelExtractor.setSectionDirty(SectionPos.x(section), SectionPos.y(section), SectionPos.z(section));
		}
	}

	@Override
	default void playGlobalSound(PositionedSoundData data, KNumberVariables variables) {
		vl$self().getSoundManager().play(createGlobalSound(data, variables));
	}

	default SoundInstance createGlobalSound(PositionedSoundData data, KNumberContext ctx) {
		var mc = vl$self();

		if (mc.level != null && data.position().isPresent()) {
			return new VidLibSoundInstance(mc.level, data, ctx);
		} else {
			return SimpleSoundInstance.forUI(data.data().sound().value(), data.data().pitch(), data.data().volume());
		}
	}

	default SoundInstance createGlobalSound(PositionedSoundData data, KNumberVariables variables) {
		var mc = vl$self();

		if (mc.level != null && data.position().isPresent()) {
			var ctx = ClientLevelSessionData.of(mc.level).createGlobalContext().fork(variables);
			return new VidLibSoundInstance(mc.level, data, ctx);
		} else {
			return SimpleSoundInstance.forUI(data.data().sound().value(), data.data().pitch(), data.data().volume());
		}
	}

	@Override
	default void physicsParticles(PhysicsParticlesDisplayData displayData, long spawnTime) {
		var level = vl$level();
		var data = displayData.data().value();
		var realTime = level.getGameTime();

		if (spawnTime < realTime - 60L || spawnTime > realTime + 60L + (long) data.lifespan().max()) {
			VidLib.LOGGER.info("Discarded physics particles packet @ " + realTime + " from " + spawnTime);
			return;
		}

		var particles = new PhysicsParticles(data, level, spawnTime, displayData.seed() == 0L ? level.getRandom().nextLong() : displayData.seed());

		displayData.blocks().value().forEach((pos, state) -> {
			particles.at = pos.immutable();
			particles.state = state;
			particles.spawn();
		}, level.getRandom());
	}

	@Override
	default void cubeParticles(Map<ShapeParticleOptions, List<BlockPos>> map) {
		var particles = vl$self().particleEngine;

		for (var entry : map.entrySet()) {
			for (var pos : entry.getValue()) {
				particles.createParticle(entry.getKey(), pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 0D, 0D, 0D);
			}
		}
	}

	@Override
	default void setScreenFade(Fade fade) {
		var mc = vl$self();
		var session = ClientSessionData.of(mc);

		if (!session.isReplay) {
			session.screenFade = new ScreenFadeInstance(fade);
		}
	}

	@Override
	default void marker(ReplayMarkerData data) {
		data = ClientGameEngine.INSTANCE.handleRuntimeMarker(data);

		if (data != null) {
			var mc = vl$self();
			var session = ClientSessionData.of(mc);
			session.markers.add(data);
		}
	}

	@Override
	default void setInfoBarText(int bar, Component text) {
		var mc = vl$self();
		var session = ClientSessionData.of(mc);

		switch (bar) {
			case 0 -> session.topInfoBarOverride = text;
			case 1 -> session.bottomInfoBarOverride = text;
		}
	}

	default float linearizeDepth(float depth) {
		float near = 0.05F;
		float far = vl$self().gameRenderer.gameRenderState().levelRenderState.cameraRenderState.depthFar;
		return near * far / (far + depth * (near - far));
	}

	default TextureAtlasSprite getSprite(SpriteKey sprite) {
		return vl$self().getAtlasManager().get(new SpriteId(sprite.atlas(), sprite.sprite()));
	}

	default UV getSpriteUV(SpriteKey sprite) {
		return sprite.isSpecial() ? UV.FULL : getSprite(sprite).uv();
	}

	default <T> void updateServerDataValue(DataKey<GlobalSessionData, T> key, T value) {
		c2s(new UpdateGlobalDataValuePayload(List.of(new DataMapValue<>(key, value))));
	}

	default <T> void updateLevelDataValue(ResourceKey<Level> dimension, DataKey<LevelSessionData<?>, T> key, T value) {
		c2s(new UpdateLevelDataValuePayload(dimension, List.of(new DataMapValue<>(key, value))));
	}

	default <T> void updatePlayerDataValue(UUID player, DataKey<PlayerSessionData, T> key, T value) {
		c2s(new UpdatePlayerDataValuePayload(player, List.of(new DataMapValue<>(key, value))));
	}

	default <T> void updatePlayerDataValue(DataKey<PlayerSessionData, T> type, T value) {
		updatePlayerDataValue(vl$self().player.getUUID(), type, value);
	}

	default void runClientCommand(String command) {
		vl$self().player.connection.sendCommand(command.startsWith("/") ? command.substring(1) : command);
	}

	@Override
	@Nullable
	default String getServerBrand() {
		var p = vl$self().player;
		return p == null ? null : p.connection.serverBrand();
	}

	default float getEffectScale() {
		return vl$self().getWindow().getGuiScaledHeight() / 1080F;
	}

	default int vl$reloadCount() {
		return 0;
	}

	default void vl$exitToTitle() {
		var mc = vl$self();

		if (mc.level != null) {
			mc.level.disconnect(Component.translatable("menu.savingLevel"));

			if (mc.isLocalServer()) {
				mc.disconnect(new GenericMessageScreen(Component.translatable("menu.savingLevel")), true);
			} else {
				mc.disconnect(new TitleScreen(), false);
			}
		}

		mc.gui.setScreen(new TitleScreen());
		NeoForge.EVENT_BUS.post(new MainMenuOpenedEvent(mc, false));
	}
}
