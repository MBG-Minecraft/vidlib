package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.type.ImString;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class GeoModelImBuilder implements ImBuilder<Identifier> {
	public static final ImBuilderType<Identifier> TYPE = GeoModelImBuilder::new;

	public static final ImString SEARCH = ImGuiUtils.resizableString();

	public final Identifier[] value = {ID.vidlib("prop/skeleton")};
	private List<Identifier> list;

	@Override
	public void set(Identifier value) {
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

			list.sort(Identifier::compareNamespaced);
			list = List.copyOf(list);
		}

		return graphics.combo("###model", value, "", list, Identifier::toString, SEARCH);
	}

	@Override
	public Identifier build() {
		return value[0];
	}
}
