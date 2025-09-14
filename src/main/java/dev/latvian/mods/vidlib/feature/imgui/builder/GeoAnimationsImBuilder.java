package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GeoAnimationsImBuilder implements ImBuilder<ResourceLocation> {
	public static final ImBuilderType<ResourceLocation> TYPE = GeoAnimationsImBuilder::new;

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

			for (var id : graphics.mc.getResourceManager().listResources("geckolib/animations", id -> id.getPath().endsWith(".json")).keySet()) {
				if (id.getPath().endsWith(".animation.json")) {
					id = id.withPath(id.getPath().replace(".animation.json", ""));
				} else if (id.getPath().endsWith(".json")) {
					id = id.withPath(id.getPath().replace(".json", ""));
				}

				list.add(id.withPath(id.getPath().substring(20)));
			}

			list.sort(ResourceLocation::compareNamespaced);
			list.addFirst(Empty.ID);
			list = List.copyOf(list);
		}

		return graphics.combo("###animation", "", value, list);
	}

	@Override
	public ResourceLocation build() {
		return value[0];
	}
}
