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
	private List<ResourceLocation> listWithNull;
	private int lastReload;

	public TextureSet(List<String> paths) {
		this.paths = paths;
		this.list = null;
		this.listWithNull = null;
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

	public List<ResourceLocation> getWithNull(Minecraft mc) {
		int reload = mc.vl$reloadCount();

		if (lastReload != reload) {
			lastReload = reload;
			listWithNull = null;
		}

		if (listWithNull == null) {
			var list = get(mc);
			listWithNull = new ArrayList<>(list.size() + 1);
			listWithNull.add(null);
			listWithNull.addAll(list);
		}

		return listWithNull;
	}

	public ImUpdate imgui(ImGraphics graphics, ResourceLocation[] value, @Nullable ImString search) {
		var list = getWithNull(graphics.mc);
		return graphics.combo("###texture", value, list, id -> id == null ? "None" : (id.getNamespace() + ":" + id.getPath().substring(9, id.getPath().length() - 4)), search);
	}
}
