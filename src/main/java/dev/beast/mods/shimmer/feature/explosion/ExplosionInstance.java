package dev.beast.mods.shimmer.feature.explosion;

import dev.beast.mods.shimmer.feature.bulk.BlockModificationConsumer;
import dev.beast.mods.shimmer.feature.bulk.OptimizedModificationBuilder;
import dev.beast.mods.shimmer.feature.bulk.PositionedBlock;
import dev.beast.mods.shimmer.feature.particle.CubeParticleOptions;
import dev.beast.mods.shimmer.feature.particle.TextParticleOptions;
import dev.beast.mods.shimmer.math.Color;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.util.DebugColorBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExplosionInstance {
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
			var d = Math.cbrt(block.inside()) / data.destroy;
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
		if (data.decay <= 0F) {
			return;
		}

		int lowestY = Integer.MAX_VALUE - 1;

		if (data.smolder) {
			for (var block : blocks) {
				if (block.dy() < lowestY) {
					lowestY = block.dy();
				}
			}
		}

		for (var block : blocks) {
			if (data.smolder ? (block.dy() <= lowestY + 1) : block.inside() >= 0.95F) {
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
				} else if (random.nextFloat() < data.decay) {
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
		return level.bulkModify(undoable, m.build());
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

	public void displayEntityDamage(int duration) {
		var blocks = new ArrayList<List<BlockPos>>();
		var instantDeathBlocks = new ArrayList<BlockPos>();
		var maxBlocks = Mth.ceil(data.radius + 1F);
		var damageText = new HashMap<String, List<Vec3>>();

		for (int i = 0; i < maxBlocks; i++) {
			blocks.add(new ArrayList<>());
		}

		for (var bpos : BlockPos.betweenClosed(data.getBounds(Vec3.atCenterOf(at)).inflate(data.entityRangeInflation()))) {
			int bx = bpos.getX() - at.getX();
			int by = bpos.getY() - at.getY();
			int bz = bpos.getZ() - at.getZ();

			var inside = data.inside(bx + 0.5F, by + 0.5F, bz + 0.5F);

			if (inside >= 0D && inside <= data.entity.radiusMod && level.getBlockState(bpos).isAir() && !level.getBlockState(bpos.below()).isAir()) {
				var damage = data.entity.damage(inside / data.entity.radiusMod);
				var relDamage = 1F - (damage - data.entity.minDamage) / (data.entity.maxDamage - data.entity.minDamage);

				if (damage >= 20F) {
					instantDeathBlocks.add(bpos.immutable());
				} else {
					blocks.get(Math.clamp((int) (relDamage * (maxBlocks - 1D)), 0, maxBlocks - 1)).add(bpos.immutable());
				}

				damageText.computeIfAbsent(KMath.veryShortFormat(damage), k -> new ArrayList<>()).add(Vec3.atCenterOf(bpos));
			}
		}

		var map = new HashMap<CubeParticleOptions, List<BlockPos>>();

		for (int i = 0; i < maxBlocks; i++) {
			var list = blocks.get(i);

			if (!list.isEmpty()) {
				map.put(new CubeParticleOptions(Color.hsb(KMath.lerp(i / (float) maxBlocks, 0F, 0.3F), 1F, 1F, 255), Color.TRANSPARENT, -duration), list);
			}
		}

		if (!instantDeathBlocks.isEmpty()) {
			map.put(new CubeParticleOptions(Color.MAGENTA, Color.TRANSPARENT, -duration), instantDeathBlocks);
		}

		for (var entry : damageText.entrySet()) {
			level.textParticles(new TextParticleOptions(Component.literal(entry.getKey()), duration), entry.getValue());
		}

		level.cubeParticles(map);
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
