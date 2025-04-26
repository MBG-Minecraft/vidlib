package dev.beast.mods.shimmer.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import dev.beast.mods.shimmer.feature.client.PlayerSkinOverrides;
import net.minecraft.client.resources.SkinManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(SkinManager.class)
public class SkinManagerMixin {
	@ModifyExpressionValue(method = "registerTextures", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;skin()Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;"))
	private MinecraftProfileTexture shimmer$skin(@Nullable MinecraftProfileTexture original, @Local(argsOnly = true) UUID uuid) {
		var override = PlayerSkinOverrides.get(uuid, MinecraftProfileTexture.Type.SKIN);
		return override == null ? original : override;
	}

	@ModifyExpressionValue(method = "registerTextures", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;cape()Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;"))
	private MinecraftProfileTexture shimmer$cape(@Nullable MinecraftProfileTexture original, @Local(argsOnly = true) UUID uuid) {
		var override = PlayerSkinOverrides.get(uuid, MinecraftProfileTexture.Type.CAPE);
		return override == null ? original : override;
	}

	@ModifyExpressionValue(method = "registerTextures", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;elytra()Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;"))
	private MinecraftProfileTexture shimmer$elytra(@Nullable MinecraftProfileTexture original, @Local(argsOnly = true) UUID uuid) {
		var override = PlayerSkinOverrides.get(uuid, MinecraftProfileTexture.Type.ELYTRA);
		return override == null ? original : override;
	}
}
