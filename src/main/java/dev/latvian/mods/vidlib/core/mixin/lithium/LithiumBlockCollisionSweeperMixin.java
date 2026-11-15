package dev.latvian.mods.vidlib.core.mixin.lithium;

import dev.latvian.mods.vidlib.feature.structure.GhostStructureCollision;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

@Mixin(targets = "net.caffeinemc.mods.lithium.common.entity.movement.ChunkAwareBlockCollisionSweeper")
public abstract class LithiumBlockCollisionSweeperMixin {
	@Shadow
	private AABB box;

	@Unique
	private Queue<VoxelShape> vl$ghostShapes;

	@Inject(method = "computeNext()Lnet/minecraft/world/phys/shapes/VoxelShape;", at = @At("HEAD"), cancellable = true)
	private void vl$addGhostStructureShapes(CallbackInfoReturnable<VoxelShape> cir) {
		var shapes = vl$ghostShapes;

		if (shapes == null) {
			var found = GhostStructureCollision.feed(box, List.of());
			shapes = found.isEmpty() ? new ArrayDeque<>(0) : new ArrayDeque<>(found);
			vl$ghostShapes = shapes;
		}

		if (!shapes.isEmpty()) {
			cir.setReturnValue(shapes.poll());
		}
	}
}