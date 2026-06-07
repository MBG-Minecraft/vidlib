package dev.latvian.mods.vidlib.feature.clothing;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ListImBuilder;

import java.util.List;

public class ClothingSetImBuilder implements ImBuilder<ClothingSet> {
	public static final ImBuilderType<ClothingSet> TYPE = ClothingSetImBuilder::new;

	public final ListImBuilder<ClothingPart> parts = new ListImBuilder<>(ClothingPartImBuilder.TYPE);

	@Override
	public void set(ClothingSet value) {
		parts.set(value == null ? List.of() : value.parts);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return parts.imgui(graphics);
	}

	@Override
	public ClothingSet build() {
		return ClothingSet.of(parts.build());
	}

	@Override
	public boolean isValid() {
		return parts.isValid();
	}
}
