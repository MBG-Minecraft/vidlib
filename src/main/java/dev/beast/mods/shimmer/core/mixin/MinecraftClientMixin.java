package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerMinecraftClient;
import dev.beast.mods.shimmer.feature.camerashake.CameraShake;
import dev.beast.mods.shimmer.feature.camerashake.CameraShakeInstance;
import dev.beast.mods.shimmer.feature.cutscene.ClientCutscene;
import dev.beast.mods.shimmer.feature.cutscene.Cutscene;
import dev.beast.mods.shimmer.feature.cutscene.CutsceneScreen;
import dev.beast.mods.shimmer.feature.cutscene.CutsceneStep;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.math.Vec2d;
import dev.beast.mods.shimmer.util.ScheduledTask;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin implements ShimmerMinecraftClient {
	@Shadow
	@Nullable
	public LocalPlayer player;

	@Shadow
	@Nullable
	public Screen screen;

	@Shadow
	public abstract void setScreen(@Nullable Screen guiScreen);

	@Unique
	private ScheduledTask.Handler shimmer$scheduledTaskHandler;

	@Unique
	private final List<CameraShakeInstance> shimmer$cameraShakeInstances = new ArrayList<>();

	@Unique
	private Vec2d shimmer$prevCameraShake = Vec2d.ZERO;

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
	public Vec2d shimmer$getCameraShakeOffset(float delta) {
		return shimmer$prevCameraShake.lerp(shimmer$cameraShake, delta);
	}

	@Override
	public void shimmer$postTick() {
		if (shimmer$scheduledTaskHandler != null) {
			shimmer$scheduledTaskHandler.tick();
		}

		var cc = ClientCutscene.instance;

		if (cc != null && cc.tick()) {
			ClientCutscene.instance = null;

			if (screen instanceof CutsceneScreen s) {
				setScreen(s.previousScreen);
			}
		}

		shimmer$prevCameraShake = shimmer$cameraShake;
		double shakeX = 0D;
		double shakeY = 0D;

		if (!shimmer$cameraShakeInstances.isEmpty()) {
			var shakeIt = shimmer$cameraShakeInstances.iterator();

			while (shakeIt.hasNext()) {
				var instance = shakeIt.next();
				float relTicks = (float) instance.ticks / (float) instance.shake.duration();
				var vec = instance.shake.type().get(instance.ticks * instance.shake.speed());
				var intensity = instance.shake.intensity();
				var intensityScale = instance.shake.start().easeMirrored(relTicks, instance.shake.end());
				shakeX += vec.x * intensity * intensityScale;
				shakeY += vec.y * intensity * intensityScale;

				if (++instance.ticks >= instance.shake.duration()) {
					shakeIt.remove();
				}
			}
		}

		shimmer$cameraShake = Math.abs(shakeX) <= 0.0001D && Math.abs(shakeY) <= 0.0001D ? Vec2d.ZERO : new Vec2d(shakeX, shakeY);
	}

	@Override
	public ActiveZones shimmer$getActiveZones() {
		return ActiveZones.CLIENT;
	}

	@Override
	public void playCutscene(Cutscene cutscene) {
		if (!cutscene.steps.isEmpty() && player != null) {
			var inst = new ClientCutscene(shimmer$self(), cutscene);
			ClientCutscene.instance = inst;

			if (player.getClass() == LocalPlayer.class && (cutscene.steps.getFirst().flags & CutsceneStep.NO_SCREEN) == 0) {
				setScreen(new CutsceneScreen(inst, screen));
			}
		}
	}

	@Override
	public void stopCutscene() {
		ClientCutscene.instance = null;

		if (screen instanceof CutsceneScreen screen) {
			setScreen(screen.previousScreen);
		}
	}

	@Override
	public void shakeCamera(CameraShake shake) {
		shimmer$cameraShakeInstances.add(new CameraShakeInstance(shake));
	}

	@Override
	public void stopCameraShaking() {
		shimmer$cameraShakeInstances.clear();
	}
}
