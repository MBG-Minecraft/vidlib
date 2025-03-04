package dev.beast.mods.shimmer.core.mixin;

import com.mojang.text2speech.Narrator;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.NarratorStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameNarrator.class)
public class GameNarratorMixin {
	@Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/mojang/text2speech/Narrator;getNarrator()Lcom/mojang/text2speech/Narrator;"))
	private Narrator shimmer$getNarrator() {
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
