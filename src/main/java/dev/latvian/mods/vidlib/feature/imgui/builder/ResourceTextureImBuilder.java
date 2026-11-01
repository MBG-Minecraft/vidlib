package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.TextureSet;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import imgui.type.ImString;
import net.minecraft.core.ClientAsset;
import org.jetbrains.annotations.Nullable;

public class ResourceTextureImBuilder implements ImBuilder<ClientAsset.ResourceTexture> {
	public static ImBuilderType<ClientAsset.ResourceTexture> of(TextureSet textureSet, ClientAsset.ResourceTexture defaultTexture) {
		return () -> new ResourceTextureImBuilder(textureSet, defaultTexture);
	}

	public static final ImBuilderType<ClientAsset.ResourceTexture> GEO = of(TextureSet.ENTITIES_AND_PROPS, new ClientAsset.ResourceTexture(ID.mc("entity/skeleton/skeleton")));
	public static final ImBuilderType<ClientAsset.ResourceTexture> SKIN = of(TextureSet.ENTITIES, new ClientAsset.ResourceTexture(ID.mc("entity/player/wide/steve")));
	public static final ImBuilderType<ClientAsset.ResourceTexture> ALL = of(TextureSet.ALL, null);

	public final ImString SEARCH = ImGuiUtils.resizableString();
	public final ClientAsset.ResourceTexture[] value;
	public final TextureSet textureSet;

	public ResourceTextureImBuilder(TextureSet textureSet, @Nullable ClientAsset.ResourceTexture defaultTexture) {
		this.textureSet = textureSet;
		this.value = new ClientAsset.ResourceTexture[]{defaultTexture};
	}

	@Override
	public void set(ClientAsset.ResourceTexture value) {
		this.value[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return textureSet.imgui(graphics, value, SEARCH);
	}

	@Override
	public ClientAsset.ResourceTexture build() {
		return value[0];
	}
}
