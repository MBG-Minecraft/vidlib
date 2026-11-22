package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightningBolt.class)
public class LightningBoltMixin {
	@WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;playLocalSound(DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FFZ)V"))
	private void vl$cancelLightningSounds(Level instance, double x, double y, double z, SoundEvent sound, SoundSource category, float volume, float pitch, boolean distanceDelay, Operation<Void> original) {
		if (!ClientGameEngine.INSTANCE.disableLightningSounds(((LightningBolt) (Object) this))) {
			instance.playLocalSound(x, y, z, sound, category, volume, pitch, distanceDelay);
		}
	}
}
