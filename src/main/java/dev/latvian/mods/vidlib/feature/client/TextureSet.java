package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TextureSet {
	public static final TextureSet ALL = new TextureSet(List.of("textures"));
	public static final TextureSet ENTITIES = new TextureSet(List.of("textures/entity"));
	public static final TextureSet PROPS = new TextureSet(List.of("textures/entity"));
	public static final TextureSet ENTITIES_AND_PROPS = new TextureSet(List.of("textures/entity", "textures/prop"));
	public static final TextureSet ATLAS = new TextureSet(List.of("textures/atlas"));

	public final List<String> paths;
	private List<ResourceLocation> list;
	private int lastReload;

	public TextureSet(List<String> paths) {
		this.paths = paths;
		this.list = null;
		this.lastReload = 0;
	}

	public List<ResourceLocation> get(Minecraft mc) {
		int reload = mc.vl$reloadCount();

		if (lastReload != reload) {
			lastReload = reload;
			list = null;
		}

		if (list == null) {
			list = new ArrayList<>();

			for (var path : paths) {
				list.addAll(mc.getResourceManager().listResources(path, id -> id.getPath().endsWith(".png")).keySet());
			}

			list.sort(ResourceLocation::compareNamespaced);
			list = List.copyOf(list);
		}

		return list;
	}

	public ImUpdate imgui(ImGraphics graphics, ResourceLocation[] value, @Nullable ImString search) {
		return graphics.combo("###texture", value, "", get(graphics.mc), id -> id.getNamespace() + ":" + id.getPath().substring(9, id.getPath().length() - 4), search);
	}

	public ImUpdate optionalImgui(ImGraphics graphics, ResourceLocation[] value, @Nullable ImString search) {
		return graphics.combo("###texture", value, "None", get(graphics.mc), id -> id.getNamespace() + ":" + id.getPath().substring(9, id.getPath().length() - 4), search);
	}
}
