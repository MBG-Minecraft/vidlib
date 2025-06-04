package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import dev.latvian.mods.vidlib.feature.visual.PlayerSkinOverrides;
import net.minecraft.client.resources.SkinManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(SkinManager.class)
public class SkinManagerMixin {
	@ModifyExpressionValue(method = "registerTextures", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;skin()Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;"))
	private MinecraftProfileTexture vl$skin(@Nullable MinecraftProfileTexture original, @Local(argsOnly = true) UUID uuid) {
		var override = PlayerSkinOverrides.get(uuid, MinecraftProfileTexture.Type.SKIN);
		return override == null ? original : override;
	}

	@ModifyExpressionValue(method = "registerTextures", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;cape()Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;"))
	private MinecraftProfileTexture vl$cape(@Nullable MinecraftProfileTexture original, @Local(argsOnly = true) UUID uuid) {
		var override = PlayerSkinOverrides.get(uuid, MinecraftProfileTexture.Type.CAPE);
		return override == null ? original : override;
	}

	@ModifyExpressionValue(method = "registerTextures", at = @At(value = "INVOKE", target = "Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;elytra()Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;"))
	private MinecraftProfileTexture vl$elytra(@Nullable MinecraftProfileTexture original, @Local(argsOnly = true) UUID uuid) {
		var override = PlayerSkinOverrides.get(uuid, MinecraftProfileTexture.Type.ELYTRA);
		return override == null ? original : override;
	}
}
