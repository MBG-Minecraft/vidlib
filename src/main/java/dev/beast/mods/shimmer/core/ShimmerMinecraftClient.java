package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.camerashake.CameraShake;
import dev.beast.mods.shimmer.feature.camerashake.CameraShakeInstance;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.cutscene.CutsceneScreen;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.data.DataMapValue;
import dev.beast.mods.shimmer.feature.data.DataType;
import dev.beast.mods.shimmer.feature.data.UpdatePlayerDataValuePayload;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleManager;
import dev.beast.mods.shimmer.feature.vote.VoteScreen;
import dev.beast.mods.shimmer.math.Vec2d;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberContext;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.util.Empty;
import dev.beast.mods.shimmer.util.PauseType;
import dev.beast.mods.shimmer.util.ScheduledTask;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector4f;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("resource")
public interface ShimmerMinecraftClient extends ShimmerMinecraftEnvironment, ShimmerClientEntityContainer {
	default Minecraft shimmer$self() {
		return (Minecraft) this;
	}

	@Override
	default ClientLevel shimmer$level() {
		return shimmer$self().level;
	}

	@Override
	default ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		return shimmer$self().player.shimmer$sessionData().getScheduledTaskHandler();
	}

	@Override
	default DataMap getServerData() {
		return shimmer$self().player.shimmer$sessionData().serverDataMap;
	}

	@Override
	default PauseType getPauseType() {
		var mc = shimmer$self();
		return mc.isPaused() ? PauseType.GAME : mc.level != null && mc.level.tickRateManager().isFrozen() ? PauseType.TICK : PauseType.NONE;
	}

	@ApiStatus.Internal
	default void shimmer$renderSetup(RenderLevelStageEvent event, float delta) {
		var player = shimmer$self().player;

		if (player == null) {
			return;
		}

		var session = player.shimmer$sessionData();

		var ray = shimmer$self().gameRenderer.getMainCamera().ray(512D);

		if (shimmer$self().options.getCameraType() == CameraType.FIRST_PERSON && player.getShowZones()) {
			session.zoneClip = session.filteredZones.clip(ray);
		} else {
			session.zoneClip = null;
		}
	}

	default Vec2d shimmer$getCameraShakeOffset(float delta) {
		var player = shimmer$self().player;

		if (player != null) {
			var s = player.shimmer$sessionData();
			return s.prevCameraShake.lerp(s.cameraShake, delta);
		}

		return Vec2d.ZERO;
	}

	@Override
	default List<? extends Player> shimmer$getPlayers() {
		var player = shimmer$self().player;
		return player == null ? List.of() : List.of(player);
	}

	default void shimmer$applyCameraShake(Camera camera, float delta) {
		var shake = shimmer$getCameraShakeOffset(delta);

		if (shake != Vec2d.ZERO) {
			var vec = new Vector4f((float) shake.x, (float) shake.y, 0F, 1F).rotate(camera.rotation());
			camera.shimmer$setPosition(camera.getPosition().add(vec.x(), vec.y(), vec.z()));
		}
	}

	default void updatePlayerData(List<DataMapValue> update) {
		c2s(new UpdatePlayerDataValuePayload(update));
	}

	default <T> void updatePlayerData(DataType<T> type, T value) {
		updatePlayerData(List.of(new DataMapValue(type, value)));
	}

	@Override
	default void shimmer$preTick(PauseType paused) {
		var level = shimmer$self().level;
		var player = shimmer$self().player;

		if (level == null || player == null) {
			return;
		}

		player.shimmer$sessionData().preTick(level, player, shimmer$self().getWindow(), paused);
	}

	@Override
	default void shimmer$postTick(PauseType paused) {
		var level = shimmer$self().level;
		var player = shimmer$self().player;

		if (player != null && level != null) {
			player.shimmer$sessionData().postTick(level, player, paused);
		}

		if (level != null && paused.tick()) {
			PhysicsParticleManager.tickAll(level, level.getGameTime());
		}
	}

	@Override
	default void playCutscene(Cutscene cutscene, WorldNumberVariables variables) {
		var level = shimmer$self().level;
		var player = shimmer$self().player;

		if (!cutscene.steps.isEmpty() && player != null) {
			var ctx = new WorldNumberContext(level, 0F, variables);

			for (var step : cutscene.steps) {
				step.resolvedStart = (int) step.start.get(ctx);
				step.resolvedLength = (int) step.length.get(ctx);
			}

			var overrideCamera = !player.isReplayCamera();
			var inst = new ClientCutscene(shimmer$self(), overrideCamera, cutscene, variables, player::getEyePosition);
			player.shimmer$sessionData().cutscene = inst;

			if (overrideCamera && !cutscene.allowMovement) {
				shimmer$self().setScreen(new CutsceneScreen(inst, shimmer$self().screen));
			}

			shimmer$self().options.hideGui = true;
		}
	}

	@Override
	default void stopCutscene() {
		shimmer$self().player.shimmer$sessionData().cutscene = null;

		if (shimmer$self().screen instanceof CutsceneScreen screen) {
			shimmer$self().setScreen(screen.previousScreen);
		}

		shimmer$self().options.hideGui = false;
		shimmer$self().gameRenderer.clearPostEffect();
	}

	@Override
	default void shakeCamera(CameraShake shake) {
		if (shake.skip()) {
			return;
		}

		shimmer$self().player.shimmer$sessionData().cameraShakeInstances.add(new CameraShakeInstance(shake));

		if (shake.motionBlur()) {
			shimmer$self().gameRenderer.setPostEffect(CameraShake.MOTION_BLUR_EFFECT);
		}
	}

	@Override
	default void shakeCamera(CameraShake shake, Vec3 source, double maxDistance) {
		shakeCamera(shake.atDistance(shimmer$self().gameRenderer.getMainCamera().getPosition(), source, maxDistance));
	}

	@Override
	default void stopCameraShaking() {
		shimmer$self().player.shimmer$sessionData().cameraShakeInstances.clear();
	}

	@Override
	default void setPostEffect(ResourceLocation id) {
		if (id.equals(Empty.ID)) {
			shimmer$self().gameRenderer.clearPostEffect();
		} else {
			shimmer$self().gameRenderer.setPostEffect(id);
		}
	}

	@Override
	default void shimmer$closeScreen() {
		shimmer$self().popGuiLayer();
	}

	@Override
	default void openVoteScreen(CompoundTag data, Component title, Component subtitle, Component yesLabel, Component noLabel) {
		shimmer$self().setScreen(new VoteScreen(data, title, subtitle, yesLabel, noLabel));
	}

	@Override
	default void endVote() {
		if (shimmer$self().screen instanceof VoteScreen) {
			shimmer$self().popGuiLayer();
		}
	}

	@Override
	default void removeZone(UUID uuid) {
		var session = shimmer$self().player.shimmer$sessionData();
		session.serverZones.remove(uuid);
		session.filteredZones.remove(uuid);
	}
}
