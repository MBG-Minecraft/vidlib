package dev.beast.mods.shimmer.core.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FoodData.class)
public class FoodDataMixin {
	@Shadow
	private int foodLevel;

	@Shadow
	private float saturationLevel;

	@Shadow
	private float exhaustionLevel;

	@Shadow
	private int tickTimer;

	/**
	 * @author Lat
	 * @reason Shimmer
	 */
	@Overwrite
	public void tick(ServerPlayer player) {
		foodLevel = 20;
		saturationLevel = 20F;
		exhaustionLevel = 0F;
		tickTimer = 0;
	}
}
