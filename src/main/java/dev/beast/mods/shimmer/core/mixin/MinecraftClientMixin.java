package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerMinecraftClient;
import dev.beast.mods.shimmer.feature.zone.ActiveZones;
import dev.beast.mods.shimmer.util.ScheduledTask;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin implements ShimmerMinecraftClient {
	@Unique
	private ScheduledTask.Handler shimmer$scheduledTaskHandler;

	@Override
	public ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		if (shimmer$scheduledTaskHandler == null) {
			shimmer$scheduledTaskHandler = new ScheduledTask.Handler(shimmer$self(), () -> shimmer$self().level);
		}

		return shimmer$scheduledTaskHandler;
	}

	@Override
	public void shimmer$postTick() {
		if (shimmer$scheduledTaskHandler != null) {
			shimmer$scheduledTaskHandler.tick();
		}
	}

	@Override
	public ActiveZones shimmer$getActiveZones() {
		return ActiveZones.CLIENT;
	}
}
