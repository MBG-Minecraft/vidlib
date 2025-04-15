package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.misc.GlobalKeybinds;
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
	private void shimmer$createSoundSliderOptionInstance(String text, SoundSource source, CallbackInfoReturnable<OptionInstance<Double>> cir) {
		if (source == SoundSource.MUSIC) {
			cir.setReturnValue(new OptionInstance<>(text, OptionInstance.noTooltip(), (prefix, value) -> value == 0.0 ? genericValueLabel(prefix, CommonComponents.OPTION_OFF) : percentValueLabel(prefix, value), OptionInstance.UnitDouble.INSTANCE, 0.0, (value) -> minecraft.getSoundManager().updateSourceVolume(source, value.floatValue())));
		}
	}

	@Redirect(method = "processOptionsForge", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options$FieldAccess;process(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
	private String shimmer$processOptionsForge(Options.FieldAccess fieldAccess, String key, String fallback) {
		var override = key.startsWith("key_") ? GlobalKeybinds.get(key.substring(4)) : null;
		return override == null ? fallback : override;
	}

	@Inject(method = "save", at = @At("HEAD"))
	private void shimmer$save(CallbackInfo ci) {
		AutoInit.Type.CLIENT_OPTIONS_SAVED.invoke(this);
	}

	/*
	@ModifyReturnValue(method = "getEffectiveRenderDistance", at = @At("RETURN"))
	private int shimmer$getEffectiveRenderDistance(int original) {
		return MiscShimmerClientUtils.overrideRenderDistance(original);
	}

	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 32))
	private int shimmer$init(int original) {
		return ShimmerConfig.maxChunkDistance;
	}
	 */
}
