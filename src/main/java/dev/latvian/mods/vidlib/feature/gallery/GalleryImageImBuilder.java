package dev.latvian.mods.vidlib.feature.gallery;

import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import imgui.ImGui;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class GalleryImageImBuilder implements ImBuilder<GalleryImage<?>> {
	public final Collection<Gallery<?>> galleries;
	public GalleryImage<?> selected;
	public boolean fullUpdate = false;

	public GalleryImageImBuilder(Collection<Gallery<?>> galleries) {
		this.galleries = galleries;
	}

	@Override
	public void set(@Nullable GalleryImage<?> value) {
		selected = value;
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		var img = build();
		var tex = img == null ? null : img.load(graphics.mc, false);

		if (tex == null) {
			if (ImGui.button("Select Image...###gallery-popup-button")) {
				ImGui.openPopup("###gallery-popup");
			}
		} else {
			if (graphics.imageButton(tex.getTexture(), ImGui.getFrameHeight() - 4F, ImGui.getFrameHeight() - 4F, UV.FULL, 2, null)) {
				ImGui.openPopup("###gallery-popup");
			}

			if (ImGui.isItemHovered()) {
				ImGui.beginTooltip();
				ImGui.text(img.displayName());
				ImGui.image(tex.getTexture().vl$getHandle(), 64F, 64F);
				ImGui.endTooltip();
			}
		}

		if (!fullUpdate && ImGui.beginPopup("###gallery-popup")) {
			boolean close = false;

			graphics.pushStack();
			graphics.setItemSpacing(4F, 4F);
			ImGui.pushID("###remove");

			if (graphics.imageButton(VidLibTextures.TRASH, 40F, 40F, UV.FULL, 2, selected == null ? ImColorVariant.GRAY : ImColorVariant.RED)) {
				if (selected != null) {
					set(null);
					update = ImUpdate.FULL;
					close = true;
				}
			}

			ImGuiUtils.hoveredTooltip("Remove");
			ImGui.popID();

			ImGui.pushID("###uploaders");
			int uploaderIndex = 0;

			for (var gallery : galleries) {
				for (var uploader : gallery.uploaders) {
					ImGui.sameLine();
					ImGui.pushID(uploaderIndex++);
					boolean clicked = graphics.imageButton(uploader.getIcon(), 40F, 40F, UV.FULL, 2, uploader.getColor());
					uploader.render(Cast.to(gallery), this, graphics, clicked);
					ImGuiUtils.hoveredTooltip(uploader.getTooltip());
					ImGui.popID();
				}
			}

			ImGui.popID();
			graphics.popStack();

			ImGui.separator();

			graphics.pushStack();
			graphics.setItemSpacing(4F, 4F);

			var list = new ArrayList<GalleryImage<?>>();

			for (var gallery : galleries) {
				list.addAll(gallery.images.values());
			}

			if (list.size() >= 2) {
				list.sort((o1, o2) -> o1.displayName().compareToIgnoreCase(o2.displayName()));
			}

			int count = 0;

			for (var image : list) {
				var imageTex = image.load(graphics.mc, false);

				if (graphics.imageButton(imageTex.getTexture(), 50F, 50F, UV.FULL, 2, null)) {
					set(image);
					update = ImUpdate.FULL;
					close = true;
				}

				ImGuiUtils.hoveredTooltip(image.displayName());

				if (++count % 5 != 0) {
					ImGui.sameLine();
				}
			}

			graphics.popStack();

			if (close) {
				ImGui.closeCurrentPopup();
			}

			ImGui.endPopup();
		}

		if (fullUpdate) {
			fullUpdate = false;
			update = ImUpdate.FULL;
		}

		return update;
	}

	@Override
	public boolean isValid() {
		return selected != null;
	}

	@Override
	public GalleryImage<?> build() {
		return selected;
	}
}
