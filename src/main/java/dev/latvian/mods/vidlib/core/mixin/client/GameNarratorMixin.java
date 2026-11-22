package dev.latvian.mods.vidlib.core.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.text2speech.Narrator;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.NarratorStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameNarrator.class)
public class GameNarratorMixin {
	@WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/text2speech/Narrator;getNarrator()Lcom/mojang/text2speech/Narrator;"))
	private Narrator vl$getNarrator(Operation<Narrator> original) {
		return Narrator.EMPTY;
	}

	/**
	 * @author Lat
	 * @reason Yeet
	 */
	@Overwrite
	private NarratorStatus getStatus() {
		return NarratorStatus.OFF;
	}
}
