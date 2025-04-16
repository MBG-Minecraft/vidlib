package dev.latvian.mods.vidlib.core.mixin;

import dev.latvian.mods.vidlib.core.VLMinecraftClient;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin implements VLMinecraftClient {
	@Inject(method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
	private void vl$reloadResourcePacks(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		AutoInit.Type.ASSETS_RELOADED.invoke();
	}
}
