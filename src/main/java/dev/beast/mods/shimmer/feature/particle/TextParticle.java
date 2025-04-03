package dev.beast.mods.shimmer.feature.particle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.beast.mods.shimmer.math.KMath;
import dev.beast.mods.shimmer.math.Vec3f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;

public class TextParticle extends Particle {
	private int prevAge;
	private final TextParticleOptions options;
	private final Vec3f vector;

	protected TextParticle(TextParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.options = options;
		setLifetime(Math.abs(options.ttl()));
		vector = Vec3f.of(vx, vy, vz);
		setSize(1F, 1F);
	}

	@Override
	public void renderCustom(PoseStack ms, MultiBufferSource buffers, Camera camera, float delta) {
		float time = KMath.lerp(delta, prevAge, age);

		if (time >= lifetime - 1F) {
			return;
		}

		var color = options.ttl() < 0 ? options.color() : options.color().fadeOut(time, lifetime, 20F);

		if (color.alpha() == 0) {
			return;
		}

		var cameraPos = camera.getPosition();
		var rx = (float) (KMath.lerp(time, xo, x) - cameraPos.x);
		var ry = (float) (KMath.lerp(time, yo, y) - cameraPos.y);
		var rz = (float) (KMath.lerp(time, zo, z) - cameraPos.z);

		ms.pushPose();
		ms.translate(rx, ry, rz);
		float scale = 0.025F * options.scale();
		ms.scale(-scale, -scale, -scale);
		ms.mulPose(camera.rotation());
		ms.mulPose(Axis.YP.rotationDegrees(180F));

		var m = ms.last().pose();
		var font = Minecraft.getInstance().font;

		font.drawInBatch(
			options.text(),
			-font.width(options.text()) / 2F,
			-font.lineHeight / 2F,
			color.argb(),
			true,
			m,
			buffers,
			options.seeThrough() ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
			0, // Background color
			LightTexture.FULL_BRIGHT
		);

		ms.popPose();
	}

	@Override
	public void render(VertexConsumer buffer, Camera camera, float delta) {
	}

	@Override
	public void tick() {
		prevAge = age;
		super.tick();
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.CUSTOM;
	}
}
