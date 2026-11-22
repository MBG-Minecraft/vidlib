package dev.latvian.mods.vidlib.core.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.packs.repository.PackDetector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PackDetector.class)
public class PackDetectorMixin {
	@WrapOperation(method = "detectPackResources", at = @At(value = "INVOKE", target = "Ljava/lang/String;endsWith(Ljava/lang/String;)Z"))
	private boolean vl$endsWith(String string, String suffix, Operation<Boolean> original) {
		return original.call(string, suffix) || string.endsWith(".jar");
	}
}
