package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerMinecraftClient;
import dev.beast.mods.shimmer.util.ScheduledTask;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin implements ShimmerMinecraftClient {
	@Unique
	private final ScheduledTask.Handler shimmer$scheduledTaskHandler = new ScheduledTask.Handler((Minecraft) (Object) this, () -> ((Minecraft) (Object) this).level);

	@Override
	public ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		return shimmer$scheduledTaskHandler;
	}
}
