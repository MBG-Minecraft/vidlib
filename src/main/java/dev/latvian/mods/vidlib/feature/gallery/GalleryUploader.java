package dev.latvian.mods.vidlib.feature.gallery;

import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import net.minecraft.resources.Identifier;

public interface GalleryUploader<K> {
	Identifier getIcon();

	String getTooltip();

	default ImColorVariant getColor() {
		return ImColorVariant.GREEN;
	}

	void render(Gallery<K> gallery, GalleryImageImBuilder builder, ImGraphics graphics, boolean clicked);
}
