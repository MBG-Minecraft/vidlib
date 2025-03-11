package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.core.ShimmerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin implements ShimmerPlayer {
	@Shadow
	private Component displayname;

	/**
	 * @author Lat
	 * @reason Optimization
	 */
	@Overwrite
	public void refreshDisplayName() {
		displayname = null;
	}

	@Inject(method = "canHarmPlayer", at = @At("RETURN"), cancellable = true)
	public void shimmer$canHarmPlayer(Player other, CallbackInfoReturnable<Boolean> cir) {
		if (cir.getReturnValueZ() && !shimmer$pvp(other)) {
			cir.setReturnValue(false);
		}
	}
}
