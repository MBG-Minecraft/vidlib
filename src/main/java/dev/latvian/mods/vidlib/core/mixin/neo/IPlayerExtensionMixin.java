package dev.latvian.mods.vidlib.core.mixin.neo;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.latvian.mods.vidlib.core.VLEntity;
import dev.latvian.mods.vidlib.feature.data.InternalPlayerData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.IPlayerExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(IPlayerExtension.class)
public interface IPlayerExtensionMixin extends VLEntity {
	@ModifyReturnValue(method = "mayFly", at = @At("RETURN"))
	private boolean vl$mayFly(boolean original) {
		return original || ((Player) this).get(InternalPlayerData.CAN_FLY);
	}
}
