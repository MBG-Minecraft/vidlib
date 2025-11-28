package dev.latvian.mods.vidlib.feature.gallery;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.imgui.AsyncFileSelector;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import imgui.ImGui;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

public class GalleryImageImBuilder implements ImBuilder<GalleryImage> {
	private static final ResourceLocation FOLDER_ICON = VidLib.id("textures/misc/folder.png");
	private static final ResourceLocation TRASH_ICON = VidLib.id("textures/misc/trash.png");

	public final Gallery gallery;
	public final ImagePreProcessor preProcessor;
	public UUID selected;
	private boolean fullUpdate = false;

	public GalleryImageImBuilder(Gallery gallery, ImagePreProcessor preProcessor) {
		this.gallery = gallery;
		this.preProcessor = preProcessor;
	}

	@Override
	public void set(@Nullable GalleryImage value) {
		selected = value == null ? null : value.id();
	}

	@Override
	public ImUpdate imgui(ImGraphics graphics) {
		var update = ImUpdate.NONE;
		var img = build();
		var tex = img == null ? null : img.load(graphics.mc);

		if (tex == null) {
			if (ImGui.button("Select Image...###gallery-popup-button")) {
				ImGui.openPopup("###gallery-popup");
			}
		} else if (graphics.imageButton(tex.getTexture(), ImGui.getFrameHeight(), ImGui.getFrameHeight(), 0F, 0F, 1F, 1F, 2)) {
			ImGui.openPopup("###gallery-popup");
		}

		if (ImGui.beginPopup("###gallery-popup")) {
			int count = 0;
			float s = 50F;

			if (selected != null) {
				if (graphics.imageButton(TRASH_ICON, s, s, 0F, 0F, 1F, 1F, 2)) {
					set(null);
					update = ImUpdate.FULL;
				}

				ImGuiUtils.hoveredTooltip("Remove");
				ImGui.sameLine();
				count++;
			}

			var list = new ArrayList<>(gallery.images.values());

			if (list.size() >= 2) {
				list.sort((o1, o2) -> o1.displayName().compareToIgnoreCase(o2.displayName()));
			}

			for (var image : list) {
				var imageTex = image.load(graphics.mc);

				if (graphics.imageButton(imageTex.getTexture(), s, s, 0F, 0F, 1F, 1F, 2)) {
					set(image);
					update = ImUpdate.FULL;
				}

				ImGuiUtils.hoveredTooltip(image.displayName());

				if (++count % 5 != 0) {
					ImGui.sameLine();
				}
			}

			if (graphics.imageButton(FOLDER_ICON, s, s, 0F, 0F, 1F, 1F, 2)) {
				selectFile(graphics);
			}

			ImGuiUtils.hoveredTooltip("Upload...");

			ImGui.endPopup();
		}

		if (fullUpdate) {
			fullUpdate = false;
			update = ImUpdate.FULL;
		}

		return update;
	}

	private void selectFile(ImGraphics graphics) {
		AsyncFileSelector.openFileDialog(null, "Select Pin Image", "png").thenAccept(pathString -> {
			var path = pathString == null ? null : Path.of(pathString);

			if (path != null && Files.exists(path) && Files.isRegularFile(path)) {
				graphics.mc.execute(() -> {
					try {
						set(gallery.upload(graphics.mc, UUID.randomUUID(), path, preProcessor));
						fullUpdate = true;
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				});
			}
		});
	}

	@Override
	public boolean isValid() {
		return gallery.get(selected) != null;
	}

	@Override
	public GalleryImage build() {
		return gallery.get(selected);
	}
}
