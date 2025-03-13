package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShaderManager.class)
public class ShaderManagerMixin {
	@Inject(method = "apply(Lnet/minecraft/client/renderer/ShaderManager$Configs;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V", at = @At("RETURN"))
	private void shimmer$apply(ShaderManager.Configs configs, ResourceManager manager, ProfilerFiller profiler, CallbackInfo ci) {
		AutoInit.Type.SHADERS_RELOADED.invoke();
	}
}
