package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TextureImBuilder implements ImBuilder<ResourceLocation> {
	public static ImBuilderType<ResourceLocation> of(List<String> paths, ResourceLocation defaultTexture) {
		return () -> new TextureImBuilder(paths, defaultTexture);
	}

	public static final ImBuilderType<ResourceLocation> ALL = of(List.of("textures"), null);
	public static final ImBuilderType<ResourceLocation> GEO = of(List.of("textures/entity", "textures/prop"), ID.mc("textures/entity/skeleton/skeleton.png"));
	public static final ImBuilderType<ResourceLocation> SKIN = of(List.of("textures/entity"), SkinTexture.STEVE);

	public final ImString SEARCH = ImGuiUtils.resizableString();

	public final List<String> paths;
	public final ResourceLocation[] value;
	private List<ResourceLocation> list;

	public TextureImBuilder(List<String> paths, @Nullable ResourceLocation defaultTexture) {
		this.paths = paths;
		this.value = new ResourceLocation[]{defaultTexture};
		this.list = null;
	}

	@Override
	public void set(ResourceLocation value) {
		this.value[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		if (list == null) {
			list = new ArrayList<>();

			for (var path : paths) {
				list.addAll(graphics.mc.getResourceManager().listResources(path, id -> id.getPath().endsWith(".png")).keySet());
			}

			list.sort(ResourceLocation::compareNamespaced);
			list = List.copyOf(list);
		}

		return graphics.combo("###texture", value, list, id -> id.getNamespace() + ":" + id.getPath().substring(9, id.getPath().length() - 4), SEARCH);
	}

	@Override
	public ResourceLocation build() {
		return value[0];
	}
}
