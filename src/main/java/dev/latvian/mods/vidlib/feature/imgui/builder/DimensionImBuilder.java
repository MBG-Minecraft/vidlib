package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.type.ImString;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.List;

public class DimensionImBuilder implements ImBuilder<ResourceKey<Level>> {
	public static final ImBuilderType<ResourceKey<Level>> TYPE = DimensionImBuilder::new;

	public static final ImString SEARCH = ImGuiUtils.resizableString();

	public final ResourceKey<Level>[] dimension = new ResourceKey[]{Level.OVERWORLD};

	@Override
	public void set(ResourceKey<Level> value) {
		dimension[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		List<ResourceKey<Level>> dimensions = List.copyOf(graphics.mc.player.connection.levels());
		return graphics.combo("###dimension", dimension, dimensions, key -> key.location().toString(), SEARCH);
	}

	@Override
	public ResourceKey<Level> build() {
		return dimension[0];
	}
}
