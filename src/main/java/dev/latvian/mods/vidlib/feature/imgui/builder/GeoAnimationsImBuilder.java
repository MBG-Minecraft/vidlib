package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.util.Empty;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.type.ImString;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class GeoAnimationsImBuilder implements ImBuilder<Identifier> {
	public static final ImBuilderType<Identifier> TYPE = GeoAnimationsImBuilder::new;

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

			for (var id : graphics.mc.getResourceManager().listResources("geckolib/animations", id -> id.getPath().endsWith(".json")).keySet()) {
				if (id.getPath().endsWith(".animation.json")) {
					id = id.withPath(id.getPath().replace(".animation.json", ""));
				} else if (id.getPath().endsWith(".json")) {
					id = id.withPath(id.getPath().replace(".json", ""));
				}

				list.add(id.withPath(id.getPath().substring(20)));
			}

			list.sort(Identifier::compareNamespaced);
			list.addFirst(Empty.ID);
			list = List.copyOf(list);
		}

		return graphics.combo("###animation", value, "", list, Identifier::toString, SEARCH);
	}

	@Override
	public Identifier build() {
		return value[0];
	}
}
