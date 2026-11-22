package dev.latvian.mods.vidlib.core.mixin.common;

import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Level.class)
public abstract class LevelMixin {
	@Inject(method = "createFireworks", at = @At("RETURN"))
	private void vl$createFireworks(double x, double y, double z, double xd, double yd, double zd, List<FireworkExplosion> explosions, boolean playSound, CallbackInfo ci) {
		var level = (Level) (Object) this;

		if (level instanceof ServerLevel serverLevel) {
			VLBridge.createFireworks.apply(serverLevel, new Vec3(x, y, z), new Vec3(xd, yd, zd), explosions);
		}
	}
}
