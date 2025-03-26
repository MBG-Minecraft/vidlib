package dev.beast.mods.shimmer.feature.explosion;

import dev.beast.mods.shimmer.feature.bulk.BlockModificationConsumer;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.bulk.UndoableModification;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.util.DebugColorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ExplosionInstance {
	public record UndoableExplosion(List<PositionedBlock> blocks) implements UndoableModification {
		@Override
		public void undo(Level level, BlockModificationConsumer consumer) {
			for (var block : blocks) {
				consumer.add(block);
			}
		}
	}

	public final Level level;
	public final BlockPos at;
	public final ExplosionData data;
	public RandomSource random;
	public List<DestroyedBlock> blocks;
	public List<Entity> entities;
	public DebugColorBlocks debug;
	public boolean undoable;
	public boolean destroyBlocks;
	public boolean decayBlocks;
	public boolean igniteBlocks;
	public boolean damageEntities;
	public boolean knockBackEntities;
	public boolean igniteEntities;

	public ExplosionInstance(Level level, BlockPos at, ExplosionData data) {
		this.level = level;
		this.at = at;
		this.data = data;
		this.random = level.random;
		this.blocks = List.of();
		this.entities = List.of();
		this.debug = DebugColorBlocks.NONE;
		this.undoable = true;
		this.destroyBlocks = true;
		this.decayBlocks = true;
		this.igniteBlocks = true;
		this.damageEntities = true;
		this.knockBackEntities = true;
		this.igniteEntities = true;
	}

	public void debug() {
		debug = DebugColorBlocks.CONCRETE;
	}

	public void collectBlocks() {
		blocks = data.collectBlocks(level, at);
	}

	public void collectEntities() {
		entities = data.collectEntities(level, Vec3.atCenterOf(at));
	}

	public void destroy(BlockModificationConsumer modifications) {
		if (data.destroy <= 0F) {
			return;
		}

		for (var block : blocks) {
			var d = Math.cbrt(block.d()) / data.destroy;
			block.destroyed().setTrue();

			if (d > 0.98D) {
				if (random.nextInt(3) == 0) {
					continue;
				}
			} else if (d > 0.96D) {
				if (random.nextInt(8) == 0) {
					continue;
				}
			} else if (d > 0.94D) {
				if (random.nextInt(32) == 0) {
					continue;
				}
			} else if (d > 0.92D) {
				if (random.nextInt(128) == 0) {
					continue;
				}
			}

			if (debug != DebugColorBlocks.NONE) {
				modifications.set(block.pos(), debug.getState(d));
			} else {
				modifications.set(block.pos(), Blocks.AIR);
			}
		}
	}

	public void decay(BlockModificationConsumer modifications) {
		if (data.decay <= 1F) {
			return;
		}

		int lowestY = 1000;

		if (data.smolder) {
			for (var block : blocks) {
				if (block.dy() < lowestY) {
					lowestY = block.dy();
				}
			}
		}

		for (var block : blocks) {
			if (data.smolder ? (block.dy() <= lowestY + 1) : random.nextInt(3) == 0) {
				var pos = block.pos();

				if (data.smolder) {
					float r = random.nextFloat() * data.decay;

					double dist = Math.sqrt(block.dx() * block.dx() + block.dz() * block.dz()) / data.radius * KMath.lerp(r, 0.25D, 1D);

					if (dist < 0.15D) {
						modifications.set(pos, Blocks.MAGMA_BLOCK);
					} else if (dist < 0.35D) {
						modifications.set(pos, Blocks.COAL_BLOCK);
					} else if (dist < 0.6D) {
						modifications.set(pos, Blocks.BLACKSTONE);
					} else {
						modifications.set(pos, Blocks.BASALT);
					}
				} else {
					var state = level.getBlockState(pos);

					if (state.getBlock() == Blocks.STONE) {
						modifications.set(pos, Blocks.COBBLESTONE);
					} else if (state.getBlock() == Blocks.STONE_BRICKS) {
						modifications.set(pos, Blocks.CRACKED_STONE_BRICKS);
					}
				}
			}
		}
	}

	public void ignite(BlockModificationConsumer modifications) {
		if (data.fire <= 0F) {
			return;
		}

		for (var block : blocks) {
			if (random.nextFloat() < data.fire) {
				var pos = block.pos();

				if (Blocks.FIRE.defaultBlockState().canSurvive(level, pos)) {
					modifications.set(pos, random.nextFloat() < 0.05F ? Blocks.CAMPFIRE.defaultBlockState() : BaseFireBlock.getState(level, pos));
				}
			}
		}
	}

	public void create(BlockModificationConsumer modifications) {
		collectBlocks();
		collectEntities();

		if (destroyBlocks) {
			destroy(modifications);
		}

		if (decayBlocks) {
			decay(modifications);
		}

		if (igniteBlocks) {
			ignite(modifications);
		}

		if (level instanceof ServerLevel serverLevel) {
			var pos = Vec3.atCenterOf(at);

			if (damageEntities) {
				data.damageEntities(serverLevel, pos, entities);
			}

			if (knockBackEntities) {
				data.knockBackEntities(pos, entities);
			}

			if (igniteEntities) {
				data.igniteEntities(entities);
			}
		}
	}

	public int create() {
		var m = new OptimizedModificationBuilder();
		create(m);

		if (undoable) {
			var destroyedBlocks = getDestroyedBlocks();

			if (!destroyedBlocks.isEmpty()) {
				level.addUndoable(new UndoableExplosion(destroyedBlocks));
			}
		}

		return level.bulkModify(m.build());
	}

	public List<PositionedBlock> getDestroyedBlocks() {
		var list = new ArrayList<PositionedBlock>(blocks.size());

		for (var block : blocks) {
			if (block.destroyed().isTrue()) {
				list.add(block.toPositionedBlock());
			}
		}

		return list;
	}

	@Override
	public String toString() {
		return "ExplosionInstance[" +
			"at=" + at +
			", data=" + data +
			", undoable=" + undoable +
			", destroyBlocks=" + destroyBlocks +
			", decayBlocks=" + decayBlocks +
			", igniteBlocks=" + igniteBlocks +
			", damageEntities=" + damageEntities +
			", knockBackEntities=" + knockBackEntities +
			", igniteEntities=" + igniteEntities +
			']';
	}
}
