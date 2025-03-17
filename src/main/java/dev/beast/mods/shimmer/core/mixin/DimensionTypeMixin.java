package dev.beast.mods.shimmer.core.mixin;

import dev.beast.mods.shimmer.GameEventHandler;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {
	@Shadow
	@Final
	private float ambientLight;

	/**
	 * @author Lat
	 * @reason Global Ambient Light
	 */
	@Overwrite
	public float ambientLight() {
		return GameEventHandler.ambientLight.clamp(ambientLight);
	}
}
