package dev.latvian.mods.vidlib.feature.gallery;

import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.klib.util.Cast;
import dev.latvian.mods.vidlib.feature.client.VidLibTextures;
import dev.latvian.mods.vidlib.feature.entity.PlayerProfiles;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImUpdate;
import dev.latvian.mods.vidlib.feature.imgui.builder.ImBuilder;
import imgui.ImGui;
import net.minecraft.Util;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class GalleryImageImBuilder implements ImBuilder<GalleryImage<?>> {
	public final Collection<Gallery<?>> galleries;
	public GalleryImage<?> selected;
	public boolean fullUpdate = false;
	@Nullable
	public UUID contextId = null;
	@Nullable
	public Entity contextEntity = null;

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

			if (ImGui.isItemHovered() && graphics.beginTooltip()) {
				ImGui.text(img.displayName());
				ImGui.image(tex.getTexture().vl$getHandle(), 64F, 64F);
				graphics.endTooltip();
			}
		}

		if (!fullUpdate && ImGui.beginPopup("###gallery-popup")) {
			boolean close = false;

			graphics.pushStack();
			graphics.setItemSpacing(4F, 4F);
			ImGui.pushID("###remove");

			if (graphics.imageButton(VidLibTextures.TRASH.texturePath(), 40F, 40F, UV.FULL, 2, selected == null ? ImColorVariant.GRAY : ImColorVariant.RED)) {
				if (selected != null) {
					set(null);
					update = ImUpdate.FULL;
					close = true;
				}
			}

			graphics.hoveredTooltip("Remove");
			ImGui.popID();

			ImGui.pushID("###uploaders");
			int uploaderIndex = 0;
			boolean firstGallery = true;

			for (var gallery : galleries) {
				for (var uploader : gallery.uploaders) {
					ImGui.sameLine();
					ImGui.pushID(uploaderIndex++);
					boolean clicked = graphics.imageButton(uploader.getIcon(), 40F, 40F, UV.FULL, 2, uploader.getColor());
					uploader.render(Cast.to(gallery), this, graphics, clicked);
					graphics.hoveredTooltip(uploader.getTooltip());
					ImGui.popID();
				}

				if (firstGallery) {
					firstGallery = false;

					if (contextId != null && !contextId.equals(Util.NIL_UUID)) {
						ImGui.sameLine();
						ImGui.pushID("###pin-player");
						boolean playerClicked = graphics.imageButton(PlayerHeads.getTexture(graphics.mc, contextId).getTexture(), 40F, 40F, UV.FULL, 2, ImColorVariant.BLUE);

						if (playerClicked) {
							var live = PlayerSkins.getLiveSkin(contextEntity);

							if (live != null) {
								// Render with the skin actually shown on the entity
								PlayerHeads.refresh(graphics.mc, contextId, live);
								PlayerBodies.refresh(graphics.mc, contextId, live);
								PlayerHeads.refreshNoLayers(graphics.mc, contextId, live);
								PlayerBodies.refreshNoLayers(graphics.mc, contextId, live);
							} else {
								// If the player has an applied skin override, drop stale cached renders
								if (PlayerSkins.getAppliedSkin(graphics.mc, contextId) != null) {
									PlayerHeads.GALLERY.images.remove(contextId);
									PlayerBodies.GALLERY.images.remove(contextId);
									PlayerHeads.GALLERY_NO_LAYERS.images.remove(contextId);
									PlayerBodies.GALLERY_NO_LAYERS.images.remove(contextId);
								}

								PlayerHeads.get(graphics.mc, contextId);
								PlayerBodies.get(graphics.mc, contextId);
								PlayerHeads.getNoLayers(graphics.mc, contextId);
								PlayerBodies.getNoLayers(graphics.mc, contextId);
							}

							ImGui.openPopup("###pin-player-images");
						}

						graphics.hoveredTooltip(() -> PlayerProfiles.getName(contextId) + "'s Images");

						if (ImGui.beginPopup("###pin-player-images")) {
							var head = PlayerHeads.get(graphics.mc, contextId);
							var body = PlayerBodies.get(graphics.mc, contextId);
							var headNoLayers = PlayerHeads.getNoLayers(graphics.mc, contextId);
							var bodyNoLayers = PlayerBodies.getNoLayers(graphics.mc, contextId);

							var options = new GalleryImage<?>[]{head, body, headNoLayers, bodyNoLayers};
							var labels = new String[]{"Head", "Body", "Head (No Layers)", "Body (No Layers)"};

							for (int i = 0; i < options.length; i++) {
								if (i > 0) {
									ImGui.sameLine();
								}

								ImGui.pushID(i);
								var option = options[i];
								var optionTex = option.load(graphics.mc, false);

								if (graphics.imageButton(optionTex.getTexture(), 50F, 50F, UV.FULL, 2, null)) {
									set(option);
									update = ImUpdate.FULL;
									close = true;
									ImGui.closeCurrentPopup();
								}

								graphics.hoveredTooltip(labels[i] + ": " + option.displayName());
								ImGui.popID();
							}

							ImGui.endPopup();
						}

						ImGui.popID();
					}
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

				graphics.hoveredTooltip(image.displayName());

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
