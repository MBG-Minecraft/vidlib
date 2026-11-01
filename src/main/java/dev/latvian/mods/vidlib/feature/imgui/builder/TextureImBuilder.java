package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.TextureSet;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.type.ImString;
import net.minecraft.core.ClientAsset;
import org.jetbrains.annotations.Nullable;

public class TextureImBuilder implements ImBuilder<ClientAsset.Texture> {
	public static ImBuilderType<ClientAsset.Texture> of(TextureSet textureSet, ClientAsset.Texture defaultTexture) {
		return () -> new TextureImBuilder(textureSet, defaultTexture);
	}

	public static final ImBuilderType<ClientAsset.Texture> GEO = of(TextureSet.ENTITIES_AND_PROPS, new ClientAsset.ResourceTexture(ID.mc("entity/skeleton/skeleton")));
	public static final ImBuilderType<ClientAsset.Texture> SKIN = of(TextureSet.ENTITIES, new ClientAsset.ResourceTexture(ID.mc("entity/player/wide/steve")));
	public static final ImBuilderType<ClientAsset.Texture> ALL = of(TextureSet.ALL, null);

	public final ImString SEARCH = ImGuiUtils.resizableString();
	public final ClientAsset.Texture[] value;
	public final TextureSet textureSet;

	public TextureImBuilder(TextureSet textureSet, @Nullable ClientAsset.Texture defaultTexture) {
		this.textureSet = textureSet;
		this.value = new ClientAsset.Texture[]{defaultTexture};
	}

	@Override
	public void set(ClientAsset.Texture value) {
		this.value[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return textureSet.imgui(graphics, value, SEARCH);
	}

	@Override
	public ClientAsset.Texture build() {
		return value[0];
	}
}
