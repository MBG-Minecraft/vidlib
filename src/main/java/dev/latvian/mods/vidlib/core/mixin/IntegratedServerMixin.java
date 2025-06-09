package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.VidLibConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(IntegratedServer.class)
public class IntegratedServerMixin {
	@Redirect(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 0))
	private <T> T vl$redirectGetRenderDistance(OptionInstance<T> instance) {
		return VidLibConfig.robert ? Cast.to(VidLibConfig.clientRenderDistance) : instance.get();
	}
}
