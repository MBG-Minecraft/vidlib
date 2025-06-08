package dev.latvian.mods.vidlib.core.mixin;

import net.minecraft.server.packs.repository.PackDetector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PackDetector.class)
public class PackDetectorMixin {
	@Redirect(method = "detectPackResources", at = @At(value = "INVOKE", target = "Ljava/lang/String;endsWith(Ljava/lang/String;)Z"))
	private boolean vl$endsWith(String string, String suffix) {
		return string.endsWith(suffix) || string.endsWith(".jar");
	}
}
