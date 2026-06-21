package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(SkinManager.class)
public class SkinManagerMixin {
	@Inject(method = "registerTextures", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;allOf([Ljava/util/concurrent/CompletableFuture;)Ljava/util/concurrent/CompletableFuture;"))
	private void vl$registerTextures(UUID uuid, MinecraftProfileTextures textures, CallbackInfoReturnable<CompletableFuture<PlayerSkin>> cir, @Local PlayerModelType model) {
		MiscClientUtils.setModel(uuid, textures, model);
	}

	@ModifyExpressionValue(method = "registerTextures", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;skin()Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;"))
	private MinecraftProfileTexture vl$skin(@Nullable MinecraftProfileTexture original, @Local(argsOnly = true) UUID uuid) {
		var override = ClientGameEngine.INSTANCE.overridePlayerTexture(uuid, MinecraftProfileTexture.Type.SKIN);
		return override == null ? original : override;
	}

	@ModifyExpressionValue(method = "registerTextures", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;cape()Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;"))
	private MinecraftProfileTexture vl$cape(@Nullable MinecraftProfileTexture original, @Local(argsOnly = true) UUID uuid) {
		var override = ClientGameEngine.INSTANCE.overridePlayerTexture(uuid, MinecraftProfileTexture.Type.CAPE);
		return override == null ? original : override;
	}

	@ModifyExpressionValue(method = "registerTextures", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;elytra()Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;"))
	private MinecraftProfileTexture vl$elytra(@Nullable MinecraftProfileTexture original, @Local(argsOnly = true) UUID uuid) {
		var override = ClientGameEngine.INSTANCE.overridePlayerTexture(uuid, MinecraftProfileTexture.Type.ELYTRA);
		return override == null ? original : override;
	}
}
