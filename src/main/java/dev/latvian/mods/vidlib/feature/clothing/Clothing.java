package dev.latvian.mods.vidlib.feature.clothing;

import dev.latvian.mods.klib.util.ID;
import net.minecraft.resources.ResourceKey;

public interface Clothing {
	static ResourceKey<ClothingSet> create(String id) {
		return ClothingPresets.createId(ID.vidlib(id));
	}

	ResourceKey<ClothingSet> NONE = create("none");
	ResourceKey<ClothingSet> TEMPLATE = create("template");
	ResourceKey<ClothingSet> X = create("x");
	ResourceKey<ClothingSet> OBSCURED = create("obscured");
	ResourceKey<ClothingSet> BODY_QUESTION_MARK = create("body_question_mark");
	ResourceKey<ClothingSet> FACE_QUESTION_MARK = create("face_question_mark");
}
