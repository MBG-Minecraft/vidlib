package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.TextureSet;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.visual.SpriteKey;
import imgui.type.ImString;
import net.minecraft.Util;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class SpriteKeyImBuilder implements ImBuilder<SpriteKey> {
	public static final List<ResourceLocation> ATLASES = Util.make(() -> {
		var list = new ArrayList<ResourceLocation>();
		list.add(SpriteKey.BLOCKS);
		list.add(SpriteKey.PARTICLES);
		list.add(SpriteKey.GUI);
		list.add(Sheets.BANNER_SHEET);
		list.add(Sheets.BED_SHEET);
		list.add(Sheets.CHEST_SHEET);
		list.add(Sheets.SHIELD_SHEET);
		list.add(Sheets.SIGN_SHEET);
		list.add(Sheets.SHULKER_SHEET);
		list.add(Sheets.ARMOR_TRIMS_SHEET);
		list.add(Sheets.DECORATED_POT_SHEET);
		return list;
	});

	public static final ImBuilderType<SpriteKey> TYPE = SpriteKeyImBuilder::new;
	public static final ImString SEARCH = ImGuiUtils.resizableString();

	public final ResourceLocation[] atlas;
	public final ResourceLocation[] sprite;

	public SpriteKeyImBuilder() {
		this.atlas = new ResourceLocation[1];
		this.sprite = new ResourceLocation[1];
	}

	@Override
	public void set(SpriteKey value) {
		if (value == null) {
			atlas[0] = null;
			sprite[0] = null;
		} else {
			atlas[0] = value.isSpecial() ? null : value.atlas();
			sprite[0] = value.sprite();
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = graphics.combo("###atlas", atlas, "Texture", ATLASES, ID::idToString, null);

		if (update.isFull()) {
			sprite[0] = null;
			SEARCH.set("");
		}

		if (atlas[0] == null) {
			update = update.or(TextureSet.ALL.imgui(graphics, sprite, SEARCH));
		} else {
			try {
				var list = new ArrayList<>(graphics.mc.getAtlasFromTexture(atlas[0]).getTextures().keySet());
				list.sort(ResourceLocation::compareNamespaced);
				update = update.or(graphics.combo("###sprite", sprite, "", list, ID::idToString, SEARCH));
			} catch (Throwable ex) {
				graphics.stackTrace(ex);
			}
		}

		return update;
	}

	@Override
	public SpriteKey build() {
		return atlas[0] == null ? SpriteKey.special(sprite[0] == null ? VidLibTextures.SQUARE : sprite[0]) : SpriteKey.of(atlas[0], sprite[0] == null ? SpriteKey.MISSING_SPRITE : sprite[0]);
	}
}
