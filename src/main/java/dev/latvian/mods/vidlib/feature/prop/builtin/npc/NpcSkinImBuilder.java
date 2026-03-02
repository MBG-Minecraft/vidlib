package dev.latvian.mods.vidlib.feature.prop.builtin.npc;

import dev.latvian.mods.vidlib.feature.client.TextureSet;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ListImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.StringImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextureImBuilder;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import dev.latvian.mods.vidlib.feature.skin.SkinTextureImBuilder;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;

public class NpcSkinImBuilder extends ListImBuilder<SkinTexture> {
	private final ListImBuilder<String> randomGrabFrom = new ListImBuilder<>(StringImBuilder.TYPE);
	private final ImInt randomCount = new ImInt(10);

	public NpcSkinImBuilder() {
		super(NpcSkinImBuilderImpl::new);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		randomGrabFrom.imgui(graphics);
		ImGui.sameLine();
		if (ImGui.button("Randomly Grab###randomly-grab")) {
			items.clear();
			TextureSet textures = new TextureSet(randomGrabFrom.build());
			var list = textures.get(graphics.mc);
			Collections.shuffle(list);
			list.forEach(item -> {
				var builder = new SkinTextureImBuilder();
				builder.texture.set(item);
				items.add(builder);
			});

			super.imgui(graphics);
			return ImUpdate.FULL;
		}
		ImGui.sameLine();
		if (ImGui.button("Clear List###clear-list")) {
			items.clear();
			super.imgui(graphics);
			return ImUpdate.FULL;
		}
		ImGui.text("Random count: ");
		ImGui.sameLine();
		ImGui.inputInt("Random Count###random-count", randomCount);
		return super.imgui(graphics);
	}

	public static class NpcSkinImBuilderImpl implements ImBuilder<SkinTexture> {
		public static final ImBuilderType<SkinTexture> TYPE = NpcSkinImBuilderImpl::new;

		public final ImBuilder<ResourceLocation> texture = TextureImBuilder.SKIN.get();
		public final BooleanImBuilder slim = new BooleanImBuilder();

		@Override
		public void set(SkinTexture value) {
			if (value == null) {
				texture.set(null);
				slim.set(false);
			} else {
				texture.set(value.texture());
				slim.set(value.slim());
			}
		}

		@Override
		public ImUpdate imgui(ImGraphics graphics) {
			var update = ImUpdate.NONE;
			update = update.or(texture.imguiKey(graphics, "Texture", "texture"));
			update = update.or(slim.imguiKey(graphics, "Slim", "slim"));
			return update;
		}

		@Override
		public SkinTexture build() {
			return new SkinTexture(
				texture.build(),
				slim.build()
			);
		}
	}

}
