package dev.latvian.mods.vidlib.feature.particle;

import com.mojang.math.Axis;
import dev.latvian.mods.klib.math.KMath;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.FormattedCharSequence;

public class TextParticle extends CustomParticle {
	private final TextParticleOptions options;

	protected TextParticle(TextParticleOptions options, ClientLevel level, double x, double y, double z, double vx, double vy, double vz) {
		super(level, x, y, z);
		this.options = options;
		setLifetime(Math.abs(options.lifespan()));
	}

	@Override
	public void submit(SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
		float time = KMath.lerp(delta, prevAge, age);

		if (time >= lifetime - 1F) {
			return;
		}

		var color = options.lifespan() < 0 ? options.color() : options.color().fadeOut(time, lifetime, 20F);

		if (color.alpha() == 0) {
			return;
		}

		var cameraPos = camera.position();
		var rx = (float) (KMath.lerp(time, xo, x) - cameraPos.x);
		var ry = (float) (KMath.lerp(time, yo, y) - cameraPos.y);
		var rz = (float) (KMath.lerp(time, zo, z) - cameraPos.z);

		var font = Minecraft.getInstance().font;
		FormattedCharSequence text = options.text().getVisualOrderText();
		float scale = 0.025F * options.scale();

		ms.pushPose();
		ms.translate(rx, ry, rz);
		ms.scale(-scale, -scale, -scale);
		ms.rotate(camera.rotation());
		ms.rotate(Axis.YP.rotationDegrees(180F));

		buffers.collector().submitText(
			ms,
			-font.width(text) / 2F,
			-font.lineHeight / 2F,
			text,
			false,
			options.seeThrough() ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.POLYGON_OFFSET,
			15728880,
			color.argb(),
			0,
			0
		);

		ms.popPose();
	}
}
