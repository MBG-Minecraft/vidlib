package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Display.class)
public class DisplayEntityMixin {
	@Shadow
	private AABB cullingBoundingBox;

	@Inject(method = "updateCulling", at = @At("RETURN"))
	private void vl$updateCulling(CallbackInfo ci) {
		((Display) (Object) this).setBoundingBox(cullingBoundingBox.inflate(0.1D));
	}
}
