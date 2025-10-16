package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import net.minecraft.util.Mth;

public class KNumberFloatInstance {
	public KNumber value;
	public float floatValue;
	public float prevFloatValue;
	private ImBuilder<KNumber> builder;

	public KNumberFloatInstance(KNumber value) {
		this.value = value;
		this.floatValue = 0F;
		this.prevFloatValue = 0F;
	}

	public void snap() {
		prevFloatValue = floatValue;
	}

	public void update(KNumberContext ctx) {
		var v = value.get(ctx);

		if (v != null) {
			floatValue = v.floatValue();
		}
	}

	public float get(float delta) {
		return Mth.lerp(delta, prevFloatValue, floatValue);
	}

	public float getDegrees(float delta) {
		return Mth.rotLerp(delta, prevFloatValue, floatValue);
	}

	public float getRadians(float delta) {
		return (float) Math.toRadians(getDegrees(delta));
	}

	public ImUpdate imgui(ImGraphics graphics, String id, String name) {
		if (builder == null) {
			builder = KNumberImBuilder.create(0D);
		}

		builder.set(value);
		var update = builder.imguiKey(graphics, id, name);

		if (update.isAny() && builder.isValid()) {
			value = builder.build();
		}

		return update;
	}
}
