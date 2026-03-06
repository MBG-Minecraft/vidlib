package dev.latvian.mods.vidlib.feature.gallery;

import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import net.minecraft.resources.ResourceLocation;

public interface GalleryUploader<K> {
	ResourceLocation getIcon();

	String getTooltip();

	default ImColorVariant getColor() {
		return ImColorVariant.GREEN;
	}

	void render(GalleryImageImBuilder<K> builder, ImGraphics graphics, boolean clicked);
}
