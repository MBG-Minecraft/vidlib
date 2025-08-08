package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class DimensionImBuilder implements ImBuilder<ResourceKey<Level>> {
	public static final ImBuilderType<ResourceKey<Level>> TYPE = DimensionImBuilder::new;

	private static final ResourceKey<ResourceKey<Level>>[] EMPTY_DIMENSION_ARRAY = new ResourceKey[0];

	public final ResourceKey<Level>[] dimension = new ResourceKey[]{Level.OVERWORLD};

	@Override
	public void set(ResourceKey<Level> value) {
		dimension[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		ResourceKey<ResourceKey<Level>>[] dimensions = graphics.mc.player.connection.levels().toArray(EMPTY_DIMENSION_ARRAY);
		return graphics.combo("###dimension", "Select Dimension...", dimension, dimensions);
	}

	@Override
	public ResourceKey<Level> build() {
		return dimension[0];
	}
}
