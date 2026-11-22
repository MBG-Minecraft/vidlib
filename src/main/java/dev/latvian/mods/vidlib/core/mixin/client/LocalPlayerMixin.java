package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.vidlib.feature.item.ItemScreen;
import dev.latvian.mods.vidlib.feature.platform.CommonGameEngine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {
	@Shadow
	@Final
	public ClientPacketListener connection;

	@Shadow
	@Final
	protected Minecraft minecraft;

	@Inject(method = "openItemGui", at = @At("HEAD"), cancellable = true)
	private void vl$openItemGui(ItemStack stack, InteractionHand hand, CallbackInfo ci) {
		var s = ItemScreen.create((LocalPlayer) (Object) this, stack, hand);

		if (s != null) {
			minecraft.gui.setScreen(s);
			ci.cancel();
		}
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Abilities;getFlyingSpeed()F"))
	private float vl$getFlyingSpeed(float original) {
		return original * CommonGameEngine.INSTANCE.getFlightSpeedModifier((Player) (Object) this);
	}
}
