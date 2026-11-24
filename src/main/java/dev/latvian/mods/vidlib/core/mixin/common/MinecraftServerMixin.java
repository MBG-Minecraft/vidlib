package dev.latvian.mods.vidlib.core.mixin.common;

import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.klib.platform.PlatformType;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.core.api.VLObject;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements VLObject {
	@Unique
	private Object vl$sessionData;

	@Override
	public Object vl$get(int id) {
		return id == 522 ? vl$sessionData : null;
	}

	@Override
	public void vl$set(int id, Object value) {
		if (id == 522) {
			vl$sessionData = value;
		}
	}

	@Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;initServer()Z", shift = At.Shift.AFTER))
	private void vl$initServer(CallbackInfo ci) {
		if (PlatformHelper.INSTANCE.getPlatform() != PlatformType.NEOFORGE) {
			VidLib.initServer((MinecraftServer) (Object) this);
		}
	}
}
