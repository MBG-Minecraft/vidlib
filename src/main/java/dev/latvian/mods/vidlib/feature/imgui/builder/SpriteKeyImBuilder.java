package dev.latvian.mods.vidlib.feature.imgui.builder;

import dev.latvian.mods.klib.util.ID;
import dev.latvian.mods.vidlib.feature.client.TextureSet;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.visual.SpriteKey;
import imgui.type.ImString;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;

public class SpriteKeyImBuilder implements ImBuilder<SpriteKey> {
	public static final ImBuilderType<SpriteKey> TYPE = SpriteKeyImBuilder::new;
	public static final ImString SEARCH = ImGuiUtils.resizableString();

	public final Identifier[] atlas;
	public final ClientAsset.ResourceTexture[] specialSprite;
	public final Identifier[] sprite;

	public SpriteKeyImBuilder() {
		this.atlas = new Identifier[1];
		this.specialSprite = new ClientAsset.ResourceTexture[1];
		this.sprite = new Identifier[1];
	}

	@Override
	public void set(SpriteKey value) {
		if (value == null || value == SpriteKey.EMPTY) {
			atlas[0] = null;
			sprite[0] = null;
		} else {
			atlas[0] = value.isSpecial() ? null : value.atlas();
			sprite[0] = value.sprite();
		}
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var atlases = new ArrayList<>(graphics.mc.getAtlasManager().atlasById.keySet());
		atlases.sort(Identifier::compareNamespaced);
		var update = graphics.combo("###atlas", atlas, "Texture", atlases, ID::idToString, null);

		if (update.isFull()) {
			sprite[0] = null;
			SEARCH.set("");
		}

		if (atlas[0] == null) {
			update = update.or(TextureSet.ALL.imgui(graphics, specialSprite, SEARCH));
		} else {
			try {
				var list = new ArrayList<>(graphics.mc.getAtlasManager().getAtlasOrThrow(atlas[0]).getTextures().keySet());
				list.sort(Identifier::compareNamespaced);
				update = update.or(graphics.combo("###sprite", sprite, "", list, ID::idToString, SEARCH));
			} catch (Throwable ex) {
				graphics.stackTrace(ex);
			}
		}

		return update;
	}

	@Override
	public SpriteKey build() {
		if (atlas[0] == null && sprite[0] == null) {
			return SpriteKey.EMPTY;
		} else if (atlas[0] == null) {
			return SpriteKey.special(sprite[0]);
		} else {
			return SpriteKey.of(atlas[0], sprite[0] == null ? SpriteKey.MISSING_SPRITE : sprite[0]);
		}
	}
}
