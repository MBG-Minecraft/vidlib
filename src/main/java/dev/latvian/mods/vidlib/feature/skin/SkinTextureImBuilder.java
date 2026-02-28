package dev.latvian.mods.vidlib.feature.skin;

import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.CompoundImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextureImBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SkinTextureImBuilder extends CompoundImBuilder<SkinTexture> {
	public static final ImBuilderType<SkinTexture> TYPE = SkinTextureImBuilder::new;

	public final ImBuilder<ResourceLocation> texture = TextureImBuilder.SKIN.get();
	public final BooleanImBuilder slim = new BooleanImBuilder();

	public SkinTextureImBuilder() {
		texture.set(SkinTexture.STEVE);
		slim.set(false);
		add("Texture", texture);
		add("Slim", slim);
	}

	@Override
	public void set(@Nullable SkinTexture value) {
		if (value == null) {
			texture.set(null);
			slim.set(false);
		} else {
			texture.set(value.texture());
			slim.set(value.slim());
		}
	}

	@Override
	public SkinTexture build() {
		var tex = texture.build();

		if (tex != null) {
			return new SkinTexture(tex, slim.build());
		} else {
			return null;
		}
	}
}
