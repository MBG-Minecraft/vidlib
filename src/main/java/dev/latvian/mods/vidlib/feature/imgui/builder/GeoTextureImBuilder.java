package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.flag.ImGuiComboFlags;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class GeoTextureImBuilder implements ImBuilder<ResourceLocation> {
	public static final ImBuilderType<ResourceLocation> TYPE = GeoTextureImBuilder::new;

	public final ResourceLocation[] value = {ID.mc("textures/entity/skeleton/skeleton.png")};
	private List<ResourceLocation> list;

	@Override
	public void set(ResourceLocation value) {
		this.value[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		if (list == null) {
			list = new ArrayList<>();
			list.addAll(graphics.mc.getResourceManager().listResources("textures/entity", id -> id.getPath().endsWith(".png")).keySet());
			list.addAll(graphics.mc.getResourceManager().listResources("textures/prop", id -> id.getPath().endsWith(".png")).keySet());
		}

		return graphics.combo("###texture", "", value, list, id -> id.getNamespace() + ":" + id.getPath().substring(9, id.getPath().length() - 4), ImGuiComboFlags.None);
	}

	@Override
	public ResourceLocation build() {
		return value[0];
	}
}
