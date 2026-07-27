package dev.latvian.mods.vidlib.feature.imgui.builder.interpolation;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import net.minecraft.util.EasingType;
import org.jspecify.annotations.Nullable;

public class EasingTypeImBuilder implements ImBuilder<EasingType> {
	public static final ImBuilderType<EasingType> TYPE = EasingTypeImBuilder::new;

	private EasingType value = EasingType.LINEAR;

	@Override
	public void set(@Nullable EasingType value) {
		this.value = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return ImUpdate.NONE;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public EasingType build() {
		return value;
	}
}
