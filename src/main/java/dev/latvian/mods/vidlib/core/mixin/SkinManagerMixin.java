package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import dev.latvian.mods.vidlib.feature.misc.MiscClientUtils;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.world.entity.player.PlayerModelType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.UUID;

@Mixin(SkinManager.class)
public class SkinManagerMixin {
	/**
	 * 1.21.5 hooked the {@code Optionull.map(...)} site to record the resolved model. 26.3
	 * resolves it directly through {@code PlayerModelType.byLegacyServicesName(String)}, so the
	 * hook is now a {@code @ModifyExpressionValue} over that exact expression - the resolved
	 * value and the surrounding locals are unchanged.
	 */
	@ModifyExpressionValue(method = "registerTextures", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/PlayerModelType;byLegacyServicesName(Ljava/lang/String;)Lnet/minecraft/world/entity/player/PlayerModelType;"))
	private PlayerModelType vl$registerTextures(PlayerModelType original, @Local(argsOnly = true) UUID uuid, @Local(argsOnly = true) MinecraftProfileTextures textures) {
		MiscClientUtils.setModel(uuid, textures, original);
		return original;
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
