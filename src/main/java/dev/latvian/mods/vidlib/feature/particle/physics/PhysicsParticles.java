package dev.latvian.mods.vidlib.feature.particle.physics;

import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.klib.math.Split;
import dev.latvian.mods.klib.vertex.function.AdditiveRandomVertexFunction;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.block.VidLibBlockStateClientProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public class PhysicsParticles {
	public static final Split[] SPLIT = {new Split(0), new Split(1), new Split(2)};

	public static Split getSplit(int split) {
		return switch (split) {
			case 0 -> SPLIT[0];
			case 1 -> SPLIT[1];
			default -> SPLIT[2];
		};
	}

	@AutoInit(AutoInit.Type.CLIENT_LOADED)
	public static void reshape() {
		var func = new AdditiveRandomVertexFunction(0L, 0L, 0.4F);

		for (var split : SPLIT) {
			split.reshape(func);
			VidLib.LOGGER.info("Reloaded Particle Splitter 1/" + split.split);
		}
	}

	public final PhysicsParticleData data;
	public final BlockAndTintGetter level;
	public final long gameTime;
	public final RandomSource random;
	private final BlockColors blockColors;
	public BlockPos at;
	public BlockState state;

	public PhysicsParticles(PhysicsParticleData data, @Nullable BlockAndTintGetter level, long gameTime, long seed) {
		this.data = data;
		this.level = level;
		this.gameTime = gameTime;
		this.random = new XoroshiroRandomSource(seed);
		this.blockColors = Minecraft.getInstance().getBlockColors();
		this.at = BlockPos.ZERO;
		this.state = Blocks.AIR.defaultBlockState();
	}

	public void spawn() {
		var clientProperties = VidLibBlockStateClientProperties.of(state);
		var manager = clientProperties.getManager();
		float density1 = data.ignoreBlockDensity ? data.density : (data.density * state.vl$getDensity());
		int count = (int) density1;

		if (density1 - count > 0) {
			if (random.nextFloat() < density1 - count) {
				count++;
			}
		}

		if (count > 0) {
			var tint = blockColors.getColor(state, level, at, 0);
			var identity = new Matrix4f();
			identity.rotateY((float) Math.toRadians(data.direction));
			identity.rotateX((float) Math.toRadians(-data.tilt));
			var matrix = new Matrix4f();

			for (int i = 0; i < count; i++) {
				spawnOne(identity, matrix, clientProperties, manager, getSplit(1 + random.nextInt(2)), tint);
			}
		}
	}

	public void spawnOne(Matrix4f identity, Matrix4f matrix, VidLibBlockStateClientProperties clientProperties, PhysicsParticleManager manager, Split split, int tint) {
		var p = new PhysicsParticle();
		p.manager = manager;
		p.random = new XoroshiroRandomSource(random.nextLong());
		p.shape = clientProperties.getPhysicsBlockParticleShape(split.boxes[random.nextInt(split.count)]);
		p.gameTimeSpawned = gameTime;
		p.speed = data.speed.sample(random);
		p.prevX = p.x = at.getX() + random.nextFloat();
		p.prevY = p.y = at.getY() + random.nextFloat();
		p.prevZ = p.z = at.getZ() + random.nextFloat();

		var power3 = random.nextFloat();
		var power = data.power.get(power3 * power3 * power3 * random.nextFloat());
		var spreadInput = random.nextFloat();
		var spread = data.spread.get(spreadInput * spreadInput);

		var angle = data.section.sample(random);

		matrix.set(identity);
		matrix.rotateY((float) Math.toRadians(angle));
		// matrix.rotateX((float) (spread * Math.PI / 2D));

		var velocityVector = new Vector4f(0F, power, -spread, 1F).mul(matrix);
		p.velocityX = velocityVector.x;
		p.velocityY = velocityVector.y;
		p.velocityZ = velocityVector.z;
		p.rotationRoll = KMath.lerp(random.nextFloat(), -1.3F, 1.3F);
		p.rotationAngle = -(float) Math.toRadians(angle) + p.rotationRoll;
		p.ttl = (int) data.lifespan.sample(random);
		p.scaleMul = split.scale * data.scale.sample(random);
		p.rotationSpeed = (KMath.lerp(random.nextFloat(), 0.25F, 0.4F)) / p.scaleMul * (float) Math.atan2(spread, power);
		p.tint = tint;
		p.red = ARGB.redFloat(tint);
		p.green = ARGB.greenFloat(tint);
		p.blue = ARGB.blueFloat(tint);
		p.alpha = ARGB.alphaFloat(tint);

		if (p.alpha < 0.1F) {
			p.alpha = 1F;
		}

		p.velocityMultiplier = data.inertia;
		p.gravityStrength = data.gravity;
		p.spin = p.prevSpin = random.nextFloat() * (float) (Math.PI * 2D);

		p.scale = p.prevScale = p.scaleMul;
		p.bounce = random.nextFloat();

		if (random.nextInt(10) == 0) {
			p.bounce *= 2F;
		}

		p.flatColorMod = (int) (random.nextFloat() * 8F) / 8F;
		p.manager.queue.add(p);

		// Minecraft.getInstance().level.addParticle(new LineParticleOptions(Color.WHITE, Color.CYAN, p.ttl), true, true, p.x, p.y, p.z, p.velocityX, p.velocityY, p.velocityZ);
	}
}
