package dev.latvian.mods.vidlib.feature.clothing;

import dev.latvian.mods.klib.gradient.Gradient;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.TextureSet;
import dev.latvian.mods.vidlib.feature.imgui.builder.CompoundImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.GradientImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextureImBuilder;
import net.minecraft.core.ClientAsset;

public class ClothingPartImBuilder extends CompoundImBuilder<ClothingPart> {
	public static final ImBuilderType<ClothingPart> TYPE = ClothingPartImBuilder::new;
	public static final TextureSet TEXTURE_SET = new TextureSet("textures/vidlib/clothing");
	public static final ClientAsset.ResourceTexture DEFAULT_TEXTURE = new ClientAsset.ResourceTexture(ID.vidlib("vidlib/clothing/tracksuit/top"));

	public final ImBuilder<ClientAsset.ResourceTexture> texture = new TextureImBuilder(TEXTURE_SET, DEFAULT_TEXTURE);
	public final ImBuilder<Ref<Gradient>> color = new GradientImBuilder();

	public ClothingPartImBuilder() {
		add("Texture", texture);
		add("Color", color);
	}

	@Override
	public void set(ClothingPart value) {
		if (value != null) {
			texture.set(new ClientAsset.ResourceTexture(value.texture().withPrefix("vidlib/clothing/")));
			color.set(value.colors());
		} else {
			texture.set(null);
			color.set(Gradient.EMPTY);
		}
	}

	@Override
	public ClothingPart build() {
		return new ClothingPart(texture.build().id().withPath(s -> s.substring(16)), color.build());
	}

	@Override
	public boolean isValid() {
		return texture.isValid() && texture.build() != null && color.isValid();
	}
}
