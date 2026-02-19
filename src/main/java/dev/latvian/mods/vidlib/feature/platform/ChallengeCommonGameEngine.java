package dev.latvian.mods.vidlib.feature.platform;

import dev.latvian.mods.vidlib.feature.data.InternalServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ChallengeCommonGameEngine extends NeoForgeCommonGameEngine {
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
		return !level.get(InternalServerData.ICE_MELTS);
	}

	@Override
	public boolean disableBlockGravity(Level level, BlockPos pos, BlockState state) {
		return !level.get(InternalServerData.BLOCK_GRAVITY);
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
