package dev.latvian.mods.vidlib.feature.zone;

import dev.latvian.mods.klib.math.AAIBB;
import dev.latvian.mods.vidlib.feature.imgui.BlockPosImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.DimensionImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;

public class AreaImBuilder implements ImBuilder<Area> {
	private final DimensionImBuilder dimension = new DimensionImBuilder();
	private final BlockPosImBuilder corner1 = new BlockPosImBuilder();
	private final BlockPosImBuilder corner2 = new BlockPosImBuilder();

	@Override
	public void set(Area value) {
		dimension.set(value.dimension());
		corner1.set(value.shape().min());
		corner2.set(value.shape().max());
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		update = update.or(dimension.imguiKey(graphics, "Dimension", "###dimension"));
		update = update.or(corner1.imguiKey(graphics, "Corner 1", "###corner-1"));
		update = update.or(corner2.imguiKey(graphics, "Corner 2", "###corner-2"));
		return update;
	}

	@Override
	public boolean isValid() {
		return dimension.isValid() && corner1.isValid() && corner2.isValid();
	}

	@Override
	public Area build() {
		return new Area(dimension.build(), new AAIBB(corner1.build(), corner2.build()));
	}
}
