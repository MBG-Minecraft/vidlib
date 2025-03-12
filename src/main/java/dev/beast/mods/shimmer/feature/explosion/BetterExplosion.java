package dev.beast.mods.shimmer.feature.explosion;

import dev.beast.mods.shimmer.feature.block.filter.BlockFilter;
import dev.beast.mods.shimmer.feature.entity.filter.EntityFilter;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.List;

public class BetterExplosion implements Runnable {
	public final ServerLevel world;
	public final BlockPos at;
	public RandomSource random;
	public float hradius, vradiusd, vradiusu;
	public float destroy;
	public float fire;
	public float smolder;
	public double entityDamage;
	public double entityKnockback;
	public List<DestroyedBlock> blocks;
	public List<Entity> entities;
	public int floor;
	public BlockFilter blockFilter;
	public EntityFilter invincibleEntities;

	public BetterExplosion(ServerLevel world, BlockPos at) {
		this.world = world;
		this.at = at;
		this.random = world.random;
		this.hradius = 4F;
		this.vradiusd = 4F;
		this.vradiusu = 4F;
		this.destroy = 1F;
		this.fire = 0F;
		this.smolder = 0F;
		this.entityDamage = 4D;
		this.entityKnockback = 1D;
		this.blocks = List.of();
		this.entities = List.of();
		this.floor = -1000;
		this.blockFilter = BlockFilter.ANY.instance();
		this.invincibleEntities = EntityFilter.NONE.instance();
	}

	public float inside(float rx, float ry, float rz) {
		return KMath.sq(rx / hradius) + (Math.abs(ry) <= 0.001F ? 0F : ry > 0F ? KMath.sq(ry / vradiusu) : KMath.sq(ry / vradiusd)) + KMath.sq(rz / hradius);
	}

	public void collectBlocks() {
		blocks = new ArrayList<>();

		int atx = at.getX();
		int aty = at.getY();
		int atz = at.getZ();

		var pos = new BlockPos.MutableBlockPos();

		int ihradius = KMath.ceil(hradius);
		int ivradiusd = KMath.ceil(vradiusd);
		int ivradiusu = KMath.ceil(vradiusu);
		for (int y = -ivradiusd; y <= ivradiusu; y++) {
			if (aty + y <= floor) {
				continue;
			}

			for (int x = -ihradius; x <= ihradius; x++) {
				for (int z = -ihradius; z <= ihradius; z++) {
					// https://en.wikipedia.org/wiki/Ellipsoid

					var d = inside(x, y, z);

					if (d <= 1F) {
						pos.set(atx + x, aty + y, atz + z);
						var state = world.getBlockState(pos);

						if ((state.shimmer$getDensity() > 0F && state.getDestroySpeed(world, pos) >= 0F || state.getBlock() instanceof BaseFireBlock) && blockFilter.test(world, pos, state)) {
							blocks.add(new DestroyedBlock(world, pos.immutable(), state, x, y, z, d, new MutableBoolean(false)));
						}
					}
				}
			}
		}

		blocks.sort(null);
	}

	public void collectEntities() {
		entities = new ArrayList<>();

		double atx = at.getX() + 0.5D;
		double aty = at.getY() + 0.5D;
		double atz = at.getZ() + 0.5D;

		double exp = 0.5D;

		for (var entity : world.getEntities(null, new AABB(atx - hradius - exp, aty - vradiusd - exp, atz - hradius - exp, atx + hradius + exp, aty + vradiusu + exp, atz + hradius + exp))) {
			if (!entity.isAlive()) {
				continue;
			}

			double x = entity.getX() - atx;
			double y = entity.getY() - aty;
			double z = entity.getZ() - atz;
			var d = KMath.sq(x / hradius) + (y == 0 ? 0 : y > 0 ? KMath.sq(y / vradiusu) : KMath.sq(y / vradiusd)) + KMath.sq(z / hradius);

			if (d <= 1.05F) {
				entities.add(entity);
			}
		}
	}

	public void destroy() {
		if (destroy > 0F) {
			for (var block : blocks) {
				var d = Math.cbrt(block.d()) / destroy;
				block.destroyed().setTrue();

				if (d > 0.98F) {
					if (random.nextInt(3) == 0) {
						continue;
					}
				} else if (d > 0.96F) {
					if (random.nextInt(8) == 0) {
						continue;
					}
				} else if (d > 0.94F) {
					if (random.nextInt(32) == 0) {
						continue;
					}
				} else if (d > 0.92F) {
					if (random.nextInt(128) == 0) {
						continue;
					}
				}

				// world.setBlockState(block.pos(), colors[KMath.clamp((int) (d * (colors.length - 1)), 0, colors.length - 1)], Block.NOTIFY_LISTENERS, 0);
				block.destroy();
			}
		}
	}

	public void decay() {
		int lowestY = 1000;

		if (smolder > 0F) {
			for (var block : blocks) {
				if (block.dy() < lowestY) {
					lowestY = block.dy();
				}
			}
		}

		for (var block : blocks) {
			if (smolder > 0F ? (block.dy() <= lowestY + 1) : random.nextInt(3) == 0) {
				var pos = block.pos();

				if (smolder > 0F) {
					float r = random.nextFloat() * smolder;

					double dist = Math.sqrt(block.dx() * block.dx() + block.dz() * block.dz()) / hradius * KMath.lerp(r, 0.25D, 1D);

					if (dist < 0.15D) {
						world.setBlockFast(pos, Blocks.MAGMA_BLOCK);
					} else if (dist < 0.35D) {
						world.setBlockFast(pos, Blocks.COAL_BLOCK);
					} else if (dist < 0.6D) {
						world.setBlockFast(pos, Blocks.BLACKSTONE);
					} else {
						world.setBlockFast(pos, Blocks.BASALT);
					}
				} else {
					var state = world.getBlockState(pos);

					if (state.getBlock() == Blocks.STONE) {
						world.setBlockFast(pos, Blocks.COBBLESTONE);
					} else if (state.getBlock() == Blocks.STONE_BRICKS) {
						world.setBlockFast(pos, Blocks.CRACKED_STONE_BRICKS);
					}
				}
			}
		}
	}

	public void ignite() {
		if (fire > 0F) {
			for (var block : blocks) {
				if (random.nextFloat() < fire) {
					var pos = block.pos();

					if (Blocks.FIRE.defaultBlockState().canSurvive(world, pos)) {
						world.setBlockFast(pos, random.nextFloat() < 0.05F ? Blocks.CAMPFIRE.defaultBlockState() : BaseFireBlock.getState(world, pos));
					}
				}
			}
		}
	}

	public void damageEntities() {
		if (entities.isEmpty()) {
			return;
		}

		var source = world.damageSources().explosion(null, null);
		double atx = at.getX() + 0.5D;
		double aty = at.getY() + 0.5D;
		double atz = at.getZ() + 0.5D;
		var fakeExplosion = new Explosion(world, null, atx, aty, atz, 0F, false, Explosion.BlockInteraction.DESTROY);

		for (var e : entities) {
			if (entityDamage > 0F && !e.ignoreExplosion(fakeExplosion)) {
				double x = e.getX() - atx;
				double y = (e instanceof PrimedTnt ? e.getY() : e.getEyeY()) - aty;
				double z = e.getZ() - atz;

				double q = Math.min(inside((float) x, (float) y, (float) z), 1D);
				double aa = Math.sqrt(x * x + y * y + z * z);

				if (aa > 0D) {
					x /= aa;
					y /= aa;
					z /= aa;
					double ac = 1D - q;

					var ed = KMath.lerp(q, entityDamage, entityDamage / 3D);

					if (e instanceof LivingEntity l && invincibleEntities.test(l)) {
						var h = l.getHealth();
						var hp = Math.max(1D, h - ed);
						float f = (float) (h - hp);
						e.hurt(source, f <= 0F ? 0.001F : f);
					} else {
						e.hurt(source, (float) ed);
					}

					double ad;

					if (e instanceof LivingEntity livingEntity) {
						ad = ac * (1D - livingEntity.getAttributeValue(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE));
					} else {
						ad = ac;
					}

					x *= ad * entityKnockback;
					y *= ad * entityKnockback;
					z *= ad * entityKnockback;
					y += 0.1D * entityDamage;

					e.setDeltaMovement(e.getDeltaMovement().add(new Vec3(x, y, z)));
				}
			}

			if (fire > 0F && !e.fireImmune()) {
				e.igniteForTicks((int) (fire * 10));
			}
		}
	}

	@Override
	public void run() {
		collectBlocks();
		collectEntities();
		destroy();
		decay();
		ignite();
		damageEntities();
	}

	public void restore() {
		for (var block : blocks) {
			if (block.destroyed().isTrue()) {
				block.restore();
			}
		}
	}
}
