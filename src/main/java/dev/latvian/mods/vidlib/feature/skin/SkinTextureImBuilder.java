package dev.latvian.mods.vidlib.feature.skin;

import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.BooleanImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.CompoundImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilderType;
import dev.latvian.mods.vidlib.feature.imgui.builder.ListButtonImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.ListItemAction;
import dev.latvian.mods.vidlib.feature.imgui.builder.TextureImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SkinTextureImBuilder extends CompoundImBuilder<SkinTexture> implements ListButtonImBuilder {
	public static final ImBuilderType<SkinTexture> TYPE = SkinTextureImBuilder::new;

	public final ImBuilder<ResourceLocation> texture = TextureImBuilder.SKIN.get();
	public final BooleanImBuilder slim = new BooleanImBuilder();
	public ListItemAction listItemAction = ListItemAction.NONE;
	public int enableListItemButtons = -1;

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
	public ImUpdate imgui(ImGraphics graphics) {
		listItemAction = ListItemAction.NONE;
		if (enableListItemButtons != -1) {
			graphics.redTextIf("#" + (enableListItemButtons + 1), !isValid());
			ImGui.sameLine();

			if (graphics.button(ImIcons.TRASHCAN + "", ImColorVariant.RED)) {
				listItemAction = ListItemAction.DELETE;
				return ImUpdate.FULL;
			}

			graphics.hoveredTooltip("Delete");
			ImGui.sameLine();

			if (ImGui.button(ImIcons.ARROW_UP + "")) {
				listItemAction = ListItemAction.MOVE_UP;
				return ImUpdate.FULL;
			}

			graphics.hoveredTooltip("Move Up");
			ImGui.sameLine();

			if (ImGui.button(ImIcons.ARROW_DOWN + "")) {
				listItemAction = ListItemAction.MOVE_DOWN;
				return ImUpdate.FULL;
			}

			graphics.hoveredTooltip("Move Down");
			ImGui.sameLine();
		}
		return super.imgui(graphics);
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

	@Override
	public void enableListItemButtons(int index) {
		enableListItemButtons = index;
	}

	@Override
	public ListItemAction getListItemAction() {
		return listItemAction;
	}
}
