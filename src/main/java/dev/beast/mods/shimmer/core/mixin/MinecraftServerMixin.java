package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerMinecraftServer;
import dev.beast.mods.shimmer.feature.zone.UpdateZoneContainerPayload;
import dev.beast.mods.shimmer.feature.zone.ZoneContainer;
import dev.beast.mods.shimmer.feature.zone.ZoneEvent;
import dev.beast.mods.shimmer.util.ScheduledTask;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements ShimmerMinecraftServer {
	@Unique
	private ZoneContainer shimmer$zoneContainer = ZoneContainer.EMPTY;

	@Unique
	private final ScheduledTask.Handler shimmer$scheduledTaskHandler = new ScheduledTask.Handler((MinecraftServer) (Object) this, () -> ((MinecraftServer) (Object) this).overworld());

	@Override
	public ScheduledTask.Handler shimmer$getScheduledTaskHandler() {
		return shimmer$scheduledTaskHandler;
	}

	@Override
	public ZoneContainer shimmer$getZoneContainer() {
		return shimmer$zoneContainer;
	}

	@Override
	public void refreshZones() {
		shimmer$zoneContainer = ZoneContainer.EMPTY;
		NeoForge.EVENT_BUS.post(new ZoneEvent.Refresh((MinecraftServer) (Object) this, zones -> shimmer$zoneContainer = zones));
		send(new UpdateZoneContainerPayload(shimmer$zoneContainer));
	}

	@Override
	public void shimmer$playerJoined(ServerPlayer player) {
		player.send(new UpdateZoneContainerPayload(shimmer$zoneContainer));
	}
}
