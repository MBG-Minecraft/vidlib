package dev.latvian.mods.vidlib.math.knumber;

import dev.latvian.mods.klib.knumber.KNumber;
import dev.latvian.mods.klib.knumber.KNumberContext;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import net.minecraft.util.Mth;

public class KNumberFloatInstance {
	public final Ref<KNumber> number;
	public float value;
	public float prevValue;

	public KNumberFloatInstance(Ref<KNumber> number) {
		this.number = number;
	}

	public void snap() {
		prevValue = value;
	}

	public void update(KNumberContext ctx) {
		var v = number.value().get(ctx);

		if (v != null) {
			value = v.floatValue();
		}
	}

	public float get(float delta) {
		return Mth.lerp(delta, prevValue, value);
	}

	public float getDegrees(float delta) {
		return Mth.rotLerp(delta, prevValue, value);
	}

	public float getRadians(float delta) {
		return Mth.rotLerpRad(delta, prevValue, value);
	}

	public void imgui(ImGraphics graphics, String id, String name) {
	}
}
