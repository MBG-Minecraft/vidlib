package dev.beast.mods.shimmer.core.mixin;

import com.mojang.blaze3d.platform.Window;
import dev.beast.mods.shimmer.core.ShimmerMinecraftClient;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.camerashake.CameraShake;
import dev.beast.mods.shimmer.feature.camerashake.CameraShakeInstance;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.cutscene.CutsceneScreen;
import dev.beast.mods.shimmer.feature.data.DataMap;
import dev.beast.mods.shimmer.feature.misc.InternalPlayerData;
import dev.beast.mods.shimmer.feature.misc.PauseType;
import dev.beast.mods.shimmer.feature.particle.physics.PhysicsParticleManager;
import dev.beast.mods.shimmer.feature.structure.ClientStructureStorage;
import dev.beast.mods.shimmer.feature.structure.StructureStorage;
import dev.beast.mods.shimmer.math.Vec2d;
import dev.beast.mods.shimmer.math.worldnumber.WorldNumberVariables;
import dev.beast.mods.shimmer.util.Empty;
import dev.beast.mods.shimmer.util.ScheduledTask;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin implements ShimmerMinecraftClient {
	@Shadow
	@Nullable
	public LocalPlayer player;

	@Shadow
	@Nullable
	public ClientLevel level;

	@Shadow
	@Nullable
	public Screen screen;

	@Shadow
	public abstract void setScreen(@Nullable Screen guiScreen);

	@Shadow
	@Final
	private Window window;

	@Unique
	private ScheduledTask.Handler shimmer$scheduledTaskHandler;

	@Unique
	private final List<CameraShakeInstance> shimmer$cameraShakeInstances = new ArrayList<>();

	@Unique
	private Vec2d shimmer$cameraShake = Vec2d.ZERO;

	@Override
	public ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		if (shimmer$scheduledTaskHandler == null) {
			shimmer$scheduledTaskHandler = new ScheduledTask.Handler(shimmer$self(), () -> shimmer$self().level);
		}

		return shimmer$scheduledTaskHandler;
	}

	@Override
	public DataMap getServerData() {
		return player.shimmer$sessionData().serverDataMap;
	}

	@Override
	public StructureStorage shimmer$structureStorage() {
		return ClientStructureStorage.CLIENT;
	}

	@Inject(method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
	private void shimmer$reloadResourcePacks(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		AutoInit.Type.ASSETS_RELOADED.invoke();
	}

	@Override
	public void shimmer$renderSetup(RenderLevelStageEvent event, float delta) {
		if (player == null) {
			return;
		}

		var session = player.shimmer$sessionData();

		double shakeX = 0D;
		double shakeY = 0D;

		if (!shimmer$cameraShakeInstances.isEmpty()) {
			for (var instance : shimmer$cameraShakeInstances) {
				float ticks = Mth.lerp(delta, instance.prevTicks, instance.ticks);
				float relTicks = ticks / (float) instance.shake.duration();
				var vec = instance.shake.type().get(instance.ticks * instance.shake.speed());
				var intensity = instance.shake.intensity();
				var intensityScale = instance.shake.start().easeMirrored(relTicks, instance.shake.end());
				shakeX += vec.x * intensity * intensityScale;
				shakeY += vec.y * intensity * intensityScale;
			}
		}

		shimmer$cameraShake = Math.abs(shakeX) <= 0.0001D && Math.abs(shakeY) <= 0.0001D ? Vec2d.ZERO : new Vec2d(shakeX, shakeY);

		var ray = shimmer$self().gameRenderer.getMainCamera().ray(512D);

		if (shimmer$self().options.getCameraType() == CameraType.FIRST_PERSON && player.get(InternalPlayerData.SHOW_ZONES)) {
			session.zoneClip = session.filteredZones.clip(ray);
		} else {
			session.zoneClip = null;
		}
	}

	@Override
	public Vec2d shimmer$getCameraShakeOffset() {
		return shimmer$cameraShake;
	}

	@Override
	public void shimmer$preTick(PauseType paused) {
		if (level == null || player == null) {
			return;
		}

		player.shimmer$sessionData().preTick(shimmer$self(), level, player, window, paused);
	}

	@Override
	public void shimmer$postTick(PauseType paused) {
		if (!paused.tick()) {
			return;
		}

		if (shimmer$scheduledTaskHandler != null) {
			shimmer$scheduledTaskHandler.tick();
		}

		var cc = ClientCutscene.instance;

		if (cc != null && cc.tick()) {
			stopCutscene();
		}

		if (!shimmer$cameraShakeInstances.isEmpty()) {
			var shakeIt = shimmer$cameraShakeInstances.iterator();

			while (shakeIt.hasNext()) {
				var instance = shakeIt.next();
				instance.prevTicks = instance.ticks;

				if (++instance.ticks >= instance.shake.duration()) {
					shakeIt.remove();
				}
			}

			if (shimmer$cameraShakeInstances.isEmpty()) {
				shimmer$self().gameRenderer.clearPostEffect();
			}
		}

		if (level != null) {
			PhysicsParticleManager.tickAll(level);
		}
	}

	@Override
	public void playCutscene(Cutscene cutscene, WorldNumberVariables variables) {
		if (!cutscene.steps.isEmpty() && player != null) {
			var inst = new ClientCutscene(shimmer$self(), cutscene, variables, player::getEyePosition);
			ClientCutscene.instance = inst;

			if (player.getClass() == LocalPlayer.class && !cutscene.allowMovement) {
				setScreen(new CutsceneScreen(inst, screen));
			}

			shimmer$self().options.hideGui = true;
		}
	}

	@Override
	public void stopCutscene() {
		ClientCutscene.instance = null;

		if (screen instanceof CutsceneScreen screen) {
			setScreen(screen.previousScreen);
		}

		shimmer$self().options.hideGui = false;
		shimmer$self().gameRenderer.clearPostEffect();
	}

	@Override
	public void shakeCamera(CameraShake shake) {
		shimmer$cameraShakeInstances.add(new CameraShakeInstance(shake));

		if (shake.motionBlur()) {
			shimmer$self().gameRenderer.setPostEffect(CameraShake.MOTION_BLUR_EFFECT);
		}
	}

	@Override
	public void stopCameraShaking() {
		shimmer$cameraShakeInstances.clear();
	}

	@Override
	public void setPostEffect(ResourceLocation id) {
		if (id.equals(Empty.ID)) {
			shimmer$self().gameRenderer.clearPostEffect();
		} else {
			shimmer$self().gameRenderer.setPostEffect(id);
		}
	}
}
