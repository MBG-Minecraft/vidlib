package dev.latvian.mods.vidlib.feature.gallery;

import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.imgui.AsyncFileSelector;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
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
import java.util.List;
import java.util.UUID;

public class GalleryImageImBuilder implements ImBuilder<GalleryImage> {
	public interface Uploader {
		ResourceLocation getIcon();

		String getTooltip();

		void render(GalleryImageImBuilder builder, ImGraphics graphics, boolean clicked);
	}

	public record FileUploader(Gallery gallery, ImagePreProcessor preProcessor) implements Uploader {
		@Override
		public ResourceLocation getIcon() {
			return VidLibTextures.FOLDER;
		}

		@Override
		public String getTooltip() {
			return "Open...";
		}

		@Override
		public void render(GalleryImageImBuilder builder, ImGraphics graphics, boolean clicked) {
			if (clicked) {
				AsyncFileSelector.openFileDialog(null, "Select Pin Image", "png").thenAccept(pathString -> {
					var path = pathString == null ? null : Path.of(pathString);

					if (path != null && Files.exists(path) && Files.isRegularFile(path)) {
						graphics.mc.execute(() -> {
							try {
								builder.set(gallery.upload(graphics.mc, UUID.randomUUID(), path, preProcessor));
								builder.fullUpdate = true;
							} catch (Exception ex) {
								throw new RuntimeException(ex);
							}
						});
					}
				});
			}
		}
	}

	public final List<Gallery> galleries;
	public final List<Uploader> uploaders;
	public GalleryImage selected;
	public boolean fullUpdate = false;

	public GalleryImageImBuilder(List<Gallery> galleries, List<Uploader> uploaders) {
		this.galleries = galleries;
		this.uploaders = uploaders;
	}

	@Override
	public void set(@Nullable GalleryImage value) {
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
			if (graphics.imageButton(tex.getTexture(), ImGui.getFrameHeight() - 4F, ImGui.getFrameHeight() - 4F, 0F, 0F, 1F, 1F, 2, null)) {
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

			int count = 0;
			float s = 50F;

			if (graphics.imageButton(VidLibTextures.TRASH, s, s, 0F, 0F, 1F, 1F, 2, selected == null ? ImColorVariant.GRAY : ImColorVariant.RED)) {
				set(null);
				update = ImUpdate.FULL;
				close = true;
			}

			ImGuiUtils.hoveredTooltip("Remove");

			ImGui.sameLine();
			count++;

			var list = new ArrayList<GalleryImage>();

			for (var gallery : galleries) {
				list.addAll(gallery.images.values());
			}

			if (list.size() >= 2) {
				list.sort((o1, o2) -> o1.displayName().compareToIgnoreCase(o2.displayName()));
			}

			for (var image : list) {
				var imageTex = image.load(graphics.mc, false);

				if (graphics.imageButton(imageTex.getTexture(), s, s, 0F, 0F, 1F, 1F, 2, null)) {
					set(image);
					update = ImUpdate.FULL;
					close = true;
				}

				ImGuiUtils.hoveredTooltip(image.displayName());

				if (++count % 5 != 0) {
					ImGui.sameLine();
				}
			}

			for (var uploader : uploaders) {
				boolean clicked = graphics.imageButton(uploader.getIcon(), s, s, 0F, 0F, 1F, 1F, 2, ImColorVariant.GREEN);
				uploader.render(this, graphics, clicked);
				ImGuiUtils.hoveredTooltip(uploader.getTooltip());

				if (++count % 5 != 0) {
					ImGui.sameLine();
				}
			}

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
	public GalleryImage build() {
		return selected;
	}
}
