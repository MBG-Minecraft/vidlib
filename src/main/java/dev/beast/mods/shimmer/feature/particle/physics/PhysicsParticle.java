package dev.beast.mods.shimmer.feature.particle.physics;

import com.mojang.blaze3d.vertex.VertexBuffer;
import dev.beast.mods.shimmer.util.FrameInfo;
import dev.latvian.mods.kmath.KMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.joml.FrustumIntersection;
import org.joml.Matrix4fStack;

import java.util.Comparator;

public class PhysicsParticle {
	public static final double SQRT_2 = Math.sqrt(2);
	public static final Comparator<PhysicsParticle> COMPARATOR = Comparator.comparingInt(PhysicsParticle::order);

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
	public float red, green, blue, alpha;

	public float flatColorMod;
	public float prevScale;
	public float prevSpin;
	public float bounce;
	public int prevBlockStateType = -1;
	public int blockStateType = -1;

	public void render(Matrix4fStack matrix, FrameInfo frame, VertexBuffer[] buffer) {
		float delta = frame.worldDelta();
		float dScale = KMath.lerp(delta, prevScale, scale);

		if (dScale < 0.001F) {
			return;
		}

		var camera = frame.camera();
		var frustum = frame.frustum();

		double rx = KMath.lerp(delta, prevX, x);
		double ry = KMath.lerp(delta, prevY, y);
		double rz = KMath.lerp(delta, prevZ, z);
		var ro = dScale * SQRT_2;
		int cubeInFrustum = frustum.cubeInFrustum(rx - ro, ry - ro, rz - ro, rx + ro, ry + ro, rz + ro);

		if (cubeInFrustum != FrustumIntersection.INSIDE && cubeInFrustum != FrustumIntersection.INTERSECT) {
			return;
		}

		var particleBuffer = shape.getBuffer();

		float ox = (float) (rx - camera.getPosition().x);
		float oy = (float) (ry - camera.getPosition().y);
		float oz = (float) (rz - camera.getPosition().z);

		matrix.pushMatrix();
		matrix.translate(ox, oy, oz);

		if (rotationAngle != 0F) {
			matrix.rotateY(rotationAngle);
		}

		float dSpin = KMath.lerp(delta, prevSpin, spin);

		if (dSpin != 0F) {
			matrix.rotateX(dSpin);
		}

		if (rotationRoll != 0F) {
			matrix.rotateZ(rotationRoll);
		}

		if (dScale != 1F) {
			matrix.scale(dScale);
		}

		manager.setModelMatrix(matrix);
		matrix.popMatrix();
		manager.setTint(red, green, blue, alpha);

		if (buffer[0] != particleBuffer) {
			if (buffer[0] != null) {
				VertexBuffer.unbind();
			}

			buffer[0] = particleBuffer;
			particleBuffer.bind();
			manager.buffersSwitched++;
		}

		particleBuffer.draw();
		manager.rendered++;
	}

	public boolean tick(Level level, long gameTime) {
		prevX = x;
		prevY = y;
		prevZ = z;
		prevSpin = spin;
		prevScale = scale;

		if (age >= ttl || gameTimeSpawned > gameTime || gameTimeSpawned + ttl < gameTime) {
			return true;
		}

		/*
		tick += speed;

		while (tick >= 1F) {
			tick0(level);
			tick -= 1F;
		}
		 */

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
				} else if (state.shimmer$getDensity() > 0F) {
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

	public int order() {
		return shape.hashCode();
	}
}
