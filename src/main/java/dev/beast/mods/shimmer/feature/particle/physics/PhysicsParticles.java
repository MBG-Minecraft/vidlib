package dev.beast.mods.shimmer.feature.particle.physics;

import dev.beast.mods.shimmer.Shimmer;
import dev.beast.mods.shimmer.feature.auto.AutoInit;
import dev.beast.mods.shimmer.feature.block.ShimmerBlockStateClientProperties;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.math.Range;
import dev.beast.mods.shimmer.math.Split;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

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
	public static void calculateUV() {
		for (var split : SPLIT) {
			split.calculateUV(split.id, 0.4F);
			Shimmer.LOGGER.info("Reloaded Particle Splitter 1/" + split.split);
		}
	}

	public RandomSource random;
	public BlockPos at = BlockPos.ZERO;
	public BlockState state = Blocks.AIR.defaultBlockState();
	public float density = 4F;
	public float tintRed = 1F;
	public float tintGreen = 1F;
	public float tintBlue = 1F;
	public float tintAlpha = 1F;

	public Range angle = Range.of(0F, 360F);
	public Range ttl = Range.of(80F);
	public Range scale = Range.ONE;
	public float largeChance = 1F;
	public float speed = 0.5F;
	public Range hvel = Range.of(0F, 0.2F);
	public Range vvel = Range.of(0.3F, 1F);
	public float velocityMultiplier = 0.96F;
	public float gravityStrength = 0.036F;

	public PhysicsParticles(RandomSource random) {
		this.random = random;
	}

	public void tint(int col) {
		tintRed = (col >> 16 & 0xFF) / 255F;
		tintGreen = (col >> 8 & 0xFF) / 255F;
		tintBlue = (col & 0xFF) / 255F;
		tintAlpha = (col >> 24 & 0xFF) / 255F;
	}

	public void spawn() {
		var clientProperties = ShimmerBlockStateClientProperties.of(state);
		var manager = clientProperties.getManager();
		float density1 = density * state.shimmer$getDensity();
		int count = (int) density1;

		if (density1 - count > 0) {
			if (random.nextFloat() < density1 - count) {
				count++;
			}
		}

		for (int i = 0; i < count; i++) {
			spawnOne(clientProperties, manager, getSplit(1 + random.nextInt(2)));
		}
	}

	public void spawnOne(ShimmerBlockStateClientProperties clientProperties, PhysicsParticleManager manager, Split split) {
		var angle0 = angle.get(random);
		var angle1 = Math.toRadians(angle0 + 90D);
		var dist = random.nextFloat();

		var pspeed = KMath.clamp(speed, 0.01F, 100F);

		var p = new PhysicsParticle();
		p.manager = manager;
		p.random = new XoroshiroRandomSource(random.nextLong());
		p.shape = clientProperties.getPhysicsBlockParticleShape(split.boxes[p.random.nextInt(split.count)]);
		p.prevX = p.x = at.getX() + random.nextFloat();
		p.prevY = p.y = at.getY() + random.nextFloat();
		p.prevZ = p.z = at.getZ() + random.nextFloat();

		var hvelrv = p.random.nextFloat();
		var hvelr = hvel.get(hvelrv * hvelrv);
		var vvelrv = p.random.nextFloat();
		var vvelr = vvel.get(vvelrv * vvelrv * vvelrv * KMath.lerp(dist, 0.4F, 1F));

		p.velocityX = Math.cos(angle1) * hvelr;
		p.velocityY = vvelr;
		p.velocityZ = Math.sin(angle1) * hvelr;
		p.rotationRoll = KMath.lerp(p.random.nextFloat(), -1.3F, 1.3F);
		p.rotationAngle = -(float) Math.toRadians(angle0) + p.rotationRoll;
		p.ttl = (int) ttl.get(p.random);
		p.scaleMul = split.scale * scale.get(p.random);
		p.rotationSpeed = (KMath.lerp(p.random.nextFloat(), 0.25F, 0.4F)) / p.scaleMul * (float) Math.atan2(hvelr, vvelr);
		p.red = tintRed;
		p.green = tintGreen;
		p.blue = tintBlue;
		p.alpha = tintAlpha;

		if (p.alpha < 0.1F) {
			p.alpha = 1F;
		}

		p.velocityMultiplier = velocityMultiplier;
		p.gravityStrength = gravityStrength;
		p.spin = p.prevSpin = p.random.nextFloat() * (float) (Math.PI * 2D);

		if (largeChance >= 1F || largeChance > 0F && p.random.nextFloat() < largeChance) {
			p.scaleMul *= 2F;
		}

		p.scale = p.prevScale = p.scaleMul;
		p.bounce = p.random.nextFloat();

		if (p.random.nextInt(10) == 0) {
			p.bounce *= 2F;
		}

		p.flatColorMod = (int) (p.random.nextFloat() * 8F) / 8F;
		p.manager.queue.add(p);
	}
}
