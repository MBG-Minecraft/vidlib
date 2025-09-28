package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GeoModelImBuilder implements ImBuilder<ResourceLocation> {
	public static final ImBuilderType<ResourceLocation> TYPE = GeoModelImBuilder::new;

	public static final ImString SEARCH = ImGuiUtils.resizableString();

	public final ResourceLocation[] value = {VidLib.id("prop/skeleton")};
	private List<ResourceLocation> list;

	@Override
	public void set(ResourceLocation value) {
		this.value[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		if (list == null) {
			list = new ArrayList<>();

			for (var id : graphics.mc.getResourceManager().listResources("geckolib/models", id -> id.getPath().endsWith(".json")).keySet()) {
				if (id.getPath().endsWith(".geo.json")) {
					id = id.withPath(id.getPath().replace(".geo.json", ""));
				} else if (id.getPath().endsWith(".json")) {
					id = id.withPath(id.getPath().replace(".json", ""));
				}

				list.add(id.withPath(id.getPath().substring(16)));
			}

			list.sort(ResourceLocation::compareNamespaced);
			list = List.copyOf(list);
		}

		return graphics.combo("###model", value, list, ResourceLocation::toString, SEARCH);
	}

	@Override
	public ResourceLocation build() {
		return value[0];
	}
}
