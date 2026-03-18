package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.TextureSet;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import imgui.type.ImString;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class TextureImBuilder implements ImBuilder<ResourceLocation> {
	public static ImBuilderType<ResourceLocation> of(TextureSet textureSet, ResourceLocation defaultTexture) {
		return () -> new TextureImBuilder(textureSet, defaultTexture);
	}

	public static final ImBuilderType<ResourceLocation> ALL = of(TextureSet.ALL, null);
	public static final ImBuilderType<ResourceLocation> GEO = of(TextureSet.ENTITIES_AND_PROPS, ID.mc("textures/entity/skeleton/skeleton.png"));
	public static final ImBuilderType<ResourceLocation> SKIN = of(TextureSet.ENTITIES, SkinTexture.STEVE);

	public final ImString SEARCH = ImGuiUtils.resizableString();

	public final TextureSet textureSet;
	public final ResourceLocation[] value;

	public TextureImBuilder(TextureSet textureSet, @Nullable ResourceLocation defaultTexture) {
		this.textureSet = textureSet;
		this.value = new ResourceLocation[]{defaultTexture};
	}

	@Override
	public void set(ResourceLocation value) {
		this.value[0] = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		return textureSet.imgui(graphics, value, SEARCH);
	}

	@Override
	public ResourceLocation build() {
		return value[0];
	}
}
