package dev.latvian.mods.vidlib.feature.client;

import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.util.MiscUtils;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.core.ClientAsset;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TextureSet {
	public static final TextureSet ALL = new TextureSet("textures");
	public static final TextureSet ENTITIES = new TextureSet("textures/entity");
	public static final TextureSet PROPS = new TextureSet("textures/entity");
	public static final TextureSet ENTITIES_AND_PROPS = new TextureSet("textures/", List.of("textures/entity", "textures/prop"));
	public static final TextureSet ATLAS = new TextureSet("textures/atlas");

	public final String prefix;
	public final List<String> paths;
	private List<ClientAsset> list;
	private int lastReload;

	public TextureSet(String prefix, List<String> paths) {
		this.prefix = prefix;
		this.paths = paths;
		this.list = null;
		this.lastReload = 0;
	}

	public TextureSet(String path) {
		this(path + "/", List.of(path));
	}

	public List<ClientAsset> get(Minecraft mc) {
		int reload = mc.vl$reloadCount();

		if (lastReload != reload) {
			lastReload = reload;
			list = null;
		}

		if (list == null) {
			list = new ArrayList<>();

			for (var path : paths) {
				for (var tex : mc.getResourceManager().listResources(path, id -> id.getPath().endsWith(".png")).keySet()) {
					list.add(MiscUtils.assetFromPNG(tex));
				}
			}

			list.sort((a, b) -> a.id().compareNamespaced(b.id()));
			list = List.copyOf(list);
		}

		return list;
	}

	private String format(ClientAsset asset) {
		if (!prefix.isEmpty() && asset.id().getPath().startsWith(prefix)) {
			return asset.id().getNamespace() + ":" + asset.id().getPath().substring(prefix.length());
		}

		return asset.id().toString();
	}

	public ImUpdate imgui(ImGraphics graphics, ClientAsset[] value, @Nullable ImString search) {
		return graphics.combo("###texture", value, "", get(graphics.mc), this::format, search);
	}

	public ImUpdate optionalImgui(ImGraphics graphics, ClientAsset[] value, @Nullable ImString search) {
		return graphics.combo("###texture", value, "None", get(graphics.mc), this::format, search);
	}
}
