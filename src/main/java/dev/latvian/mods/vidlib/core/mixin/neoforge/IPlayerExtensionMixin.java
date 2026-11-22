package dev.latvian.mods.vidlib.core.mixin.neoforge;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IPlayerExtension.class)
public interface IPlayerExtensionMixin {
	@ModifyReturnValue(method = "mayFly", at = @At("RETURN"))
	private boolean vl$mayFly(boolean original) {
		return original || VLBridge.allowFlight.test((Player) this);
	}
}
