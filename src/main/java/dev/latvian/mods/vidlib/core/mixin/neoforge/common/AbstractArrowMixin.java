package dev.latvian.mods.vidlib.core.mixin.neoforge.common;

import dev.latvian.mods.vidlib.core.api.VLBridge;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractArrow.class)
public class AbstractArrowMixin {
	@ModifyConstant(method = "tickDespawn", constant = @Constant(intValue = 1200))
	private int vl$despawnLife(int o) {
		return VLBridge.getArrowDespawnTime.applyAsInt((AbstractArrow) (Object) this);
	}
}
