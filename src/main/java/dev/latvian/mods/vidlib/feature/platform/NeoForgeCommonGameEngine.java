package dev.latvian.mods.vidlib.feature.platform;

import dev.latvian.mods.vidlib.feature.feature.Feature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class NeoForgeCommonGameEngine extends CommonGameEngine {
	@Override
	@Nullable
	public VoxelShape overrideBarrierShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext ctx && ctx.getEntity() != null && blockGetter instanceof Level level && level.getServerFeatures().has(Feature.SOFT_BARRIERS)) {
			if (ctx.getEntity() instanceof AbstractArrow) {
				return Shapes.empty();
			}

			if (ctx.getEntity().vl$isCreative()) {
				return Shapes.empty();
			}
		}

		return null;
	}

	@Override
	public boolean disablePOI() {
		return true;
	}

	@Override
	public boolean disableAdvancements() {
		return true;
	}

	@Override
	public boolean replaceFoodTick(ServerPlayer player, FoodData foodData) {
		foodData.setFoodLevel(20);
		foodData.setSaturation(20F);
		foodData.vl$setExhaustionLevel(0F);
		foodData.vl$setTickTimer(0);
		return true;
	}
}
