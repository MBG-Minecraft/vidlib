package dev.latvian.mods.vidlib.core.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.latvian.mods.vidlib.VidLibConfig;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.misc.GlobalKeybinds;
import dev.latvian.mods.vidlib.util.Cast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public abstract class OptionsMixin {
	@Shadow
	private static Component percentValueLabel(Component text, double value) {
		return null;
	}

	@Shadow
	public static Component genericValueLabel(Component text, Component value) {
		return null;
	}

	@Shadow
	protected Minecraft minecraft;

	@Inject(method = "createSoundSliderOptionInstance", at = @At("HEAD"), cancellable = true)
	private void vl$createSoundSliderOptionInstance(String text, SoundSource source, CallbackInfoReturnable<OptionInstance<Double>> cir) {
		if (source == SoundSource.MUSIC) {
			cir.setReturnValue(new OptionInstance<>(text, OptionInstance.noTooltip(), (prefix, value) -> value == 0.0 ? genericValueLabel(prefix, CommonComponents.OPTION_OFF) : percentValueLabel(prefix, value), OptionInstance.UnitDouble.INSTANCE, 0.0, (value) -> minecraft.getSoundManager().updateSourceVolume(source, value.floatValue())));
		}
	}

	@Redirect(method = "processOptions", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options$FieldAccess;process(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
	private String vl$processOptionsForge(Options.FieldAccess fieldAccess, String key, String fallback) {
		var override = key.startsWith("key_") ? GlobalKeybinds.get(key.substring(4)) : null;
		return override == null ? fallback : override;
	}

	@Inject(method = "save", at = @At("HEAD"))
	private void vl$save(CallbackInfo ci) {
		AutoInit.Type.CLIENT_OPTIONS_SAVED.invoke(this);
	}

	@ModifyExpressionValue(method = "getEffectiveRenderDistance", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;"))
	private <T> T vl$getEffectiveRenderDistance(T original) {
		return VidLibConfig.robert ? Cast.to(VidLibConfig.clientRenderDistance) : original;
	}

	/*
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 32))
	private int vl$init(int original) {
		return VidLibConfig.maxChunkDistance;
	}
	 */
}
