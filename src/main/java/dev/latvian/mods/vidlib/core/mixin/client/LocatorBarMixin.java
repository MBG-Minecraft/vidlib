package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.gui.contextualbar.LocatorBar;
import net.minecraft.client.waypoints.ClientWaypointManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(LocatorBar.class)
public abstract class LocatorBarMixin {
	@WrapOperation(
		method = "extractRenderState",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/waypoints/ClientWaypointManager;forEachWaypoint(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Consumer;)V"
		)
	)
	private void vl$forEachWaypoint(
		ClientWaypointManager manager,
		Entity fromEntity,
		Consumer<TrackedWaypoint> consumer,
		Operation<Void> original
	) {
		original.call(manager, fromEntity, (Consumer<TrackedWaypoint>) waypoint -> {
			if (ClientGameEngine.INSTANCE.isLocatorBarWaypointVisible(waypoint)) {
				consumer.accept(waypoint);
			}
		});
	}
}