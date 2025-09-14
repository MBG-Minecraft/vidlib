package dev.latvian.mods.vidlib.feature.canvas.dof;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.EnumImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.FloatImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorImBuilder;

public class DepthOfFieldDataImBuilder implements ImBuilder<DepthOfFieldData> {
	public final ImBuilder<KVector> focus = KVectorImBuilder.create();
	public final FloatImBuilder focusRange = new FloatImBuilder(0F, 30F);
	public final FloatImBuilder blurRange = new FloatImBuilder(0F, 30F);
	public final FloatImBuilder strength = new FloatImBuilder(0F, 30F);
	public final ImBuilder<DepthOfFieldShape> shape = new EnumImBuilder<>(DepthOfFieldShape.VALUES);

	@Override
	public void set(DepthOfFieldData value) {
		if (value != null) {
			focus.set(value.focus());
			focusRange.set(value.focusRange());
			blurRange.set(value.blurRange());
			strength.set(value.strength());
			shape.set(value.shape());
		} else {
			focus.set(KVector.ZERO);
			focusRange.set(1.5F);
			blurRange.set(8F);
			strength.set(5F);
			shape.set(DepthOfFieldShape.SPHERE);
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		update = update.or(focus.imguiKey(graphics, "Focus", "focus"));
		update = update.or(focusRange.imguiKey(graphics, "Focus Range", "focus_range"));
		update = update.or(blurRange.imguiKey(graphics, "Blur Range", "blur_range"));
		update = update.or(strength.imguiKey(graphics, "Strength", "strength"));
		update = update.or(shape.imguiKey(graphics, "Shape", "shape"));
		return update;
	}

	@Override
	public boolean isValid() {
		return focus.isValid() && focusRange.isValid() && blurRange.isValid() && strength.isValid() && shape.isValid();
	}

	@Override
	public DepthOfFieldData build() {
		return new DepthOfFieldData(focus.build(), focusRange.build(), blurRange.build(), strength.build(), shape.build());
	}
}
