package dev.latvian.mods.vidlib.feature.particle.physics;

import dev.latvian.mods.klib.math.KMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class PhysicsParticle implements Comparable<PhysicsParticle> {
	public static final Comparator<PhysicsParticle> COMPARATOR = Comparable::compareTo;

	public PhysicsParticleManager manager;
	public RandomSource random;
	public PhysicsParticleShape shape;
	public long gameTimeSpawned;
	public double x, y, z;
	public double prevX, prevY, prevZ;
	public double velocityX, velocityY, velocityZ;
	public float rotationAngle, rotationSpeed, rotationRoll;
	public int age;
	public float tick;
	public float speed;
	public int ttl;
	public float scaleMul;
	public float scale;
	public float velocityMultiplier;
	public float gravityStrength;
	public float spin;
	public int tint;
	public float red, green, blue, alpha;

	public float flatColorMod;
	public float prevScale;
	public float prevSpin;
	public float bounce;
	public int prevBlockStateType = -1;
	public int blockStateType = -1;

	public boolean tick(Level level, long gameTime) {
		prevX = x;
		prevY = y;
		prevZ = z;
		prevSpin = spin;
		prevScale = scale;

		if (age >= ttl || gameTimeSpawned > gameTime || gameTimeSpawned + ttl < gameTime) {
			return true;
		}

		tick0(level);
		age++;
		return false;
	}

	public void tick0(Level level) {
		x += velocityX;
		y += velocityY;
		z += velocityZ;

		velocityX *= velocityMultiplier;
		velocityZ *= velocityMultiplier;

		if (velocityY <= 0D) {
			prevBlockStateType = blockStateType;

			if (blockStateType == -1 || KMath.floor(prevY - scale * 0.5F) != KMath.floor(y - scale * 0.5F) || KMath.floor(prevX) != KMath.floor(x) || KMath.floor(prevZ) != KMath.floor(z)) {
				var state = level.getBlockState(BlockPos.containing(x, y - scale * 0.5F, z));

				if (state.getBlock() == Blocks.WATER) {
					blockStateType = 2;
				} else if (state.vl$getDensity() > 0F) {
					blockStateType = 1;
				} else {
					blockStateType = 0;
				}

				if (prevBlockStateType == -1) {
					prevBlockStateType = blockStateType;
				}
			}

			if (blockStateType == 2) {
				if (random.nextFloat() <= scale * 0.3F) {
					level.addParticle(ParticleTypes.BUBBLE, x - scale + random.nextFloat() * scale * 2D, y - scale * 0.5D, z - scale + random.nextFloat() * scale * 2D, 0D, 0D, 0D);
				}

				if (prevBlockStateType == 0) {
					if (velocityY < -0.3D) {
						velocityY = 0.3D;
					}

					if (rotationSpeed > 0.3F) {
						rotationSpeed = 0.3F;
					}
				}

				velocityX *= 0.92D;
				velocityZ *= 0.92D;
				velocityY *= 0.4D;
				rotationSpeed *= 0.86F;
			} else if (prevBlockStateType == 1) {
				velocityX *= 0.85D;
				velocityZ *= 0.85D;
				rotationSpeed *= 0.45F;

				if (velocityY < -0.2D) {
					velocityY *= -0.2D * bounce;
				} else {
					velocityY = 0D;
				}

				if (velocityY == 0D && age < ttl - 8) {
					age = ttl - 8;
				}
			} else {
				rotationSpeed *= 0.97F;
			}
		}

		velocityY -= gravityStrength;

		// spin += (float) Math.abs(velocityY);
		spin += rotationSpeed;

		scale = 1F;
		if (age > ttl - 10) {
			scale = 1F - (age - (ttl - 10F)) / 10F;
		}
		scale *= scaleMul;
	}

	@Override
	public int compareTo(@NotNull PhysicsParticle o) {
		int i = Integer.compare(shape.hashCode(), o.shape.hashCode());
		if (i == 0) {
			i = Integer.compare(tint, o.tint);
		}
		return i;
	}
}
