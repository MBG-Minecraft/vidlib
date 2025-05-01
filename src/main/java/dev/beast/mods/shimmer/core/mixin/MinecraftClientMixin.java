package dev.beast.mods.shimmer.core.mixin;

import com.mojang.authlib.GameProfile;
import dev.beast.mods.shimmer.core.ShimmerMinecraftClient;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin implements ShimmerMinecraftClient {
	@Unique
	private final Map<String, GameProfile> shimmer$profileByNameCache = new HashMap<>();

	@Unique
	private final Map<UUID, GameProfile> shimmer$profileByUUIDCache = new HashMap<>();

	@Inject(method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"))
	private void shimmer$reloadResourcePacks(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		shimmer$clearProfileCache();
		AutoInit.Type.ASSETS_RELOADED.invoke();
	}

	@Override
	public GameProfile retrieveGameProfile(UUID uuid) {
		return shimmer$profileByUUIDCache.computeIfAbsent(uuid, ShimmerMinecraftClient.super::retrieveGameProfile);
	}

	@Override
	public GameProfile retrieveGameProfile(String name) {
		return shimmer$profileByNameCache.computeIfAbsent(name, ShimmerMinecraftClient.super::retrieveGameProfile);
	}

	@Override
	public void shimmer$clearProfileCache() {
		shimmer$profileByUUIDCache.clear();
		shimmer$profileByNameCache.clear();
	}
}
