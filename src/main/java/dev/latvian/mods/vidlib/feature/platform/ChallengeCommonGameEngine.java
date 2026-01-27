package dev.latvian.mods.vidlib.feature.platform;

import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import dev.latvian.mods.vidlib.feature.entity.EntityOverride;
import dev.latvian.mods.vidlib.feature.feature.Feature;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class ChallengeCommonGameEngine extends CommonGameEngine {
	@Override
	public void setupServer(MinecraftServer server) {
		server.overworld().setDayTime(6000L);
		server.overworld().setWeatherParameters(20000000, 20000000, false, false);
		server.setFlightAllowed(true);

		if (server.isSingleplayer()) {
			server.getPlayerList().setAllowCommandsForAllPlayers(true);
		}

		setupGameRules(server.getGameRules(), server);
	}

	@Override
	public void setupGameRules(GameRules rules, MinecraftServer server) {
		super.setupGameRules(rules, server);
		rules.getRule(GameRules.RULE_KEEPINVENTORY).set(true, server);
		rules.getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
		rules.getRule(GameRules.RULE_NATURAL_REGENERATION).set(false, server);
		rules.getRule(GameRules.RULE_DAYLIGHT).set(false, server);
		rules.getRule(GameRules.RULE_RANDOMTICKING).set(0, server);
		rules.getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
		rules.getRule(GameRules.RULE_FALL_DAMAGE).set(false, server);
	}

	@Override
	public boolean tickCoralBlocks(ScheduledTickAccess instance, BlockPos pos, Block block) {
		return false;
	}

	@Override
	public boolean getInfiniteArrows() {
		return true;
	}

	@Override
	public int getArrowDespawnTime(AbstractArrow arrow) {
		return 20;
	}

	@Override
	@Nullable
	public VoxelShape overrideBarrierShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext ctx && ctx.getEntity() != null && blockGetter instanceof Level level && level.getServerFeatures().has(Feature.SOFT_BARRIERS)) {
			if (ctx.getEntity() instanceof AbstractArrow) {
				return Shapes.empty();
			}

			var v = EntityOverride.PASS_THROUGH_BARRIERS.get(ctx.getEntity());

			if (v == null ? ctx.getEntity().vl$isCreative() : v) {
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
	public boolean disableIceMelting(Level level, BlockPos pos, BlockState state) {
		return !level.get(InternalServerData.ICE_MELTS);
	}

	@Override
	public boolean disableBlockGravity(Level level, BlockPos pos, BlockState state) {
		return !level.get(InternalServerData.BLOCK_GRAVITY);
	}

	@Override
	public boolean replaceFoodTick(ServerPlayer player, FoodData foodData) {
		foodData.setFoodLevel(20);
		foodData.setSaturation(20F);
		foodData.vl$setExhaustionLevel(0F);
		foodData.vl$setTickTimer(0);
		return true;
	}

	@Override
	public boolean disableGolems(Level level, BlockPos pos) {
		return true;
	}

	@Override
	public boolean disableXP(Level level) {
		return true;
	}

	@Override
	public boolean disableLeafDecay(BlockState state) {
		return true;
	}

	@Override
	public boolean disablePricklyBerryBushes(Level level, BlockPos pos, BlockState state, Entity entity) {
		return true;
	}
}
