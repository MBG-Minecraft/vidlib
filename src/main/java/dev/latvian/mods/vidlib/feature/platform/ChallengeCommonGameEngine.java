package dev.latvian.mods.vidlib.feature.platform;

import dev.latvian.mods.klib.core.api.common.KLibFoodData;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;

import java.util.Collection;
import java.util.List;

public class ChallengeCommonGameEngine extends NeoForgeCommonGameEngine {
	@Override
	public void setupGameRules(GameRules rules, MinecraftServer server) {
		super.setupGameRules(rules, server);
		rules.set(GameRules.KEEP_INVENTORY, true, server);
		rules.set(GameRules.SPAWN_MOBS, false, server);
		rules.set(GameRules.NATURAL_HEALTH_REGENERATION, false, server);
		rules.set(GameRules.ADVANCE_TIME, false, server);
		rules.set(GameRules.RANDOM_TICK_SPEED, 0, server);
		rules.set(GameRules.ADVANCE_WEATHER, false, server);
		rules.set(GameRules.FALL_DAMAGE, false, server);
	}

	@Override
	public boolean disablePOI() {
		return true;
	}

	@Override
	public Collection<AdvancementHolder> overrideAdvancements(Collection<AdvancementHolder> original) {
		return List.of();
	}

	@Override
	public boolean replaceFoodTick(ServerPlayer player, FoodData foodData) {
		foodData.setFoodLevel(20);
		foodData.setSaturation(20F);

		if (foodData instanceof KLibFoodData klib) {
			klib.klib$setExhaustionLevel(0F);
			klib.klib$setTickTimer(0);
		}

		return true;
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
	public boolean tickCoralBlocks(ScheduledTickAccess instance, BlockPos pos, Block block) {
		return false;
	}

	@Override
	public boolean disableIceMelting(Level level, BlockPos pos, BlockState state) {
		return !LevelSessionData.of(level).getIceMelts();
	}

	@Override
	public boolean disableBlockGravity(Level level, BlockPos pos, BlockState state) {
		return !LevelSessionData.of(level).getBlockGravity();
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

	@Override
	public boolean getScaleDamageWithDifficulty(ServerPlayer player) {
		return false;
	}

	@Override
	public void modifyDroppedItem(LivingEntity entity, ItemEntity item) {
		if (entity instanceof Player) {
			item.setUnlimitedLifetime();
		}
	}

	@Override
	public boolean disableSleepStatusAnnouncement(ServerLevel level) {
		return true;
	}
}
