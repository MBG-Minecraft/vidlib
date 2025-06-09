package dev.latvian.mods.vidlib.feature.particle;

import dev.latvian.mods.klib.math.KMath;
import dev.latvian.mods.vidlib.feature.gradient.ClientGradients;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import org.joml.SimplexNoise;

public class BurnSmokeParticle extends TextureSheetParticle {
	public static ParticleProvider<SimpleParticleType> create(SpriteSet spriteSet) {
		return (type, level, x, y, z, xd, yd, zd) -> new BurnSmokeParticle(level, x, y, z, xd, yd, zd, spriteSet);
	}

	private final float random1;
	private final float random2;
	private final float random3;
	private float oQuadSize;

	public BurnSmokeParticle(ClientLevel level, double x, double y, double z, double velocityX, double velocityY, double velocityZ, SpriteSet spriteSet) {
		super(level, x, y, z);

		this.pickSprite(spriteSet);
		// this.sprite = MinecraftClient.getInstance().getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE).apply(TEXTURE);
		this.friction = 1F;
		this.random1 = random.nextFloat();
		this.random2 = random.nextFloat();
		this.random3 = random.nextFloat();

		var angle = ((velocityZ + random1) * 0.1D + velocityX) * 2D * Math.PI;
		this.xd = Math.cos(angle) * velocityY;
		this.yd = 0.3D;
		this.zd = Math.sin(angle) * velocityY;

		this.lifetime = 200;
		this.gravity = 0;
		this.hasPhysics = false;
		this.oQuadSize = this.quadSize = 0F;
		this.oRoll = this.roll = KMath.lerp(random1, -1F, 1F);

		this.rCol = this.gCol = this.bCol = KMath.lerp(random2, 0.13F, 0.23F);

		if (random.nextFloat() < 0.04F) {
			var col = ClientGradients.FIRE_1.sample(random);
			setColor(col.redf(), col.greenf(), col.bluef());
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void tick() {
		oQuadSize = quadSize;
		oRoll = roll;
		super.tick();

		double vm = KMath.lerp(random3, 0.97D, 0.99D);
		xd *= vm;
		zd *= vm;

		float f0 = age / (float) lifetime;
		float f = f0 < 0.5F ? 0F : ((f0 - 0.5F) / 0.5F);
		quadSize = ((f0 < 0.2F ? (f0 / 0.2F) : 1F - f) * 3F) * KMath.lerp(SimplexNoise.noise(random1, random2, random2, f0 * 2F), 0.5F, 1F);
		roll = KMath.lerp(random1, -1F, 1F) * (1F - f);

		rCol = Math.min(rCol * 1.004F, 1F);
		gCol = Math.min(gCol * 1.004F, 1F);
		bCol = Math.min(bCol * 1.004F, 1F);
	}

	@Override
	public float getQuadSize(float delta) {
		return Mth.lerp(delta, oQuadSize, quadSize);
	}

	@Override
	protected int getLightColor(float tint) {
		return 15728880;
	}
}
