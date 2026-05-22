package dev.latvian.mods.vidlib.feature.prop.builtin.npc;

import dev.latvian.mods.vidlib.feature.client.TextureSet;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ListImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.SetImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.StringImBuilder;
import dev.latvian.mods.vidlib.feature.skin.SkinTexture;
import dev.latvian.mods.vidlib.feature.skin.SkinTextureImBuilder;
import imgui.ImGui;
import imgui.type.ImInt;

import java.util.Objects;

public class NpcSkinImBuilder extends SetImBuilder<SkinTexture> {
	private final ListImBuilder<String> randomGrabFrom = new ListImBuilder<>(StringImBuilder.TYPE);
	private final ImInt randomCount = new ImInt(10);

	public NpcSkinImBuilder() {
		super(SkinTextureImBuilder::new);
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		randomGrabFrom.imgui(graphics);
		ImGui.sameLine();
		if (ImGui.button("Randomly Grab###randomly-grab")) {
			items.clear();
			TextureSet textures = new TextureSet(randomGrabFrom.build());
			textures.get(graphics.mc).stream()
				.limit(randomCount.get())
				.filter(Objects::nonNull)
				.forEach(item -> {
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

}
