package dev.latvian.mods.vidlib.feature.pin;

import dev.latvian.mods.klib.math.DistanceComparator;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.klib.util.Lazy;
import dev.latvian.mods.klib.util.PathIDGenerator;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.gallery.Gallery;
import dev.latvian.mods.vidlib.feature.gallery.GalleryFileUploader;
import dev.latvian.mods.vidlib.feature.gallery.GalleryImageImBuilder;
import dev.latvian.mods.vidlib.feature.gallery.PlayerBodies;
import dev.latvian.mods.vidlib.feature.gallery.PlayerHeads;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color3ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcon;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Pins {
	ImBoolean ENABLED = new ImBoolean(true);
	ImFloat PIN_SIZE = new ImFloat(256F);
	ImFloat PIN_OFFSET = new ImFloat(0F);
	ImInt PIN_ALPHA = new ImInt(255);

	Map<UUID, Pin> PINS = new Object2ObjectOpenHashMap<>();

	ImagePreProcessor PRE_PROCESSOR = ImagePreProcessor.FIT_SQUARE.andThen(ImagePreProcessor.CLOSEST_4);

	@ClientAutoRegister
	Gallery<UUID> GALLERY = Gallery.ofUUIDKey("pins", () -> VidLibPaths.USER.get().resolve("pin-gallery"), TriState.TRUE).addUploader(new GalleryFileUploader<>(PathIDGenerator.RANDOM_UUID, PRE_PROCESSOR));

	List<Gallery<?>> PIN_GALLERIES = new ArrayList<>(List.of(GALLERY, PlayerBodies.GALLERY, PlayerHeads.GALLERY));
	Lazy<GalleryImageImBuilder> IMAGE_IM_BUILDER = Lazy.of(() -> new GalleryImageImBuilder(PIN_GALLERIES));

	MenuItem MENU_ITEM = MenuItem.menu(ImIcons.LOCATION, "Pins", (graphics, items) -> {
		items.add(MenuItem.item(ImIcon.NONE, "Enabled", ENABLED));
		items.add(MenuItem.sliderFloat("Size", PIN_SIZE::get, PIN_SIZE::set, 0F, 1024F));
		items.add(MenuItem.sliderFloat("Offset", PIN_OFFSET::get, PIN_OFFSET::set, 0F, 1F));
		items.add(MenuItem.sliderInt("Alpha", PIN_ALPHA::get, PIN_ALPHA::set, 1, 255));
	});

	static void draw(GuiGraphics graphics, DeltaTracker deltaTracker) {
		if (!ENABLED.get() || PINS.isEmpty()) {
			return;
		}

		var mc = Minecraft.getInstance();
		var level = mc.level;

		if (level == null) {
			return;
		}

		var projectedCoordinates = mc.getProjectedCoordinates();

		if (projectedCoordinates == null) {
			return;
		}

		var delta = deltaTracker.getGameTimeDeltaPartialTick(false);
		int pinSize = (int) (PIN_SIZE.get() * mc.getEffectScale());

		var list = new ArrayList<ScreenPin>(PINS.size());

		for (var entry : PINS.entrySet()) {
			var pin = entry.getValue();

			if (pin.enabled && pin.isSet()) {
				var entity = level.getEntity(entry.getKey());

				if (entity != null) {
					var img = pin.getImage();

					if (img != null) {
						list.add(new ScreenPin(entity, pin, img, entity.getPosition(delta).add(0D, entity.getBbHeight() * 1.1D, 0D)));
					}
				}
			}
		}

		if (list.isEmpty()) {
			return;
		} else if (list.size() >= 2) {
			list.sort(new DistanceComparator<>(mc.gameRenderer.getMainCamera().getPosition(), ScreenPin::pos));
		}

		for (var screenPin : list) {
			var wpos = projectedCoordinates.screen(screenPin.pos());

			if (wpos != null) {
				int pinAlpha = PIN_ALPHA.get() << 24;

				graphics.pose().pushPose();
				graphics.pose().translate(wpos.x(), wpos.y() - 2F, 0F);
				graphics.pose().translate(-pinSize / 2F, -pinSize * (1F + PIN_OFFSET.get()), 0F);
				graphics.pose().scale(pinSize / 512F, pinSize / 512F, 1F);

				var shape = screenPin.pin().shapeOverride == null ? screenPin.pin().shape : screenPin.pin().shapeOverride;
				int size = shape.size;
				int color = shape.transparentBackground ? screenPin.pin().color.argb() : (pinAlpha | screenPin.pin().color.rgb());

				screenPin.image().load(mc, true);

				if (!shape.transparentBackground) {
					graphics.blit(VidLibRenderTypes.GUI, shape.maskTexture, shape.x, shape.y, 0F, 0F, size, size, size, size, color);
				}

				if (!screenPin.pin().background.isTransparent()) {
					graphics.blit(VidLibRenderTypes.GUI, shape.maskTexture, shape.x, shape.y, 0F, 0F, size, size, size, size, screenPin.pin().background.withAlpha(screenPin.pin().background.alphaf() * (PIN_ALPHA.get() / 255F)).argb());
				}

				graphics.blit(shape.maskedRenderType, screenPin.image().textureId(), shape.x, shape.y, 1F, 1F, size - 2, size - 2, size, size, pinAlpha | 0xFFFFFF);

				if (shape.overlayTexture != null) {
					graphics.blit(VidLibRenderTypes.GUI, shape.overlayTexture, 0, 0, 0F, 0F, 512, 512, 512, 512, color);
				}

				graphics.pose().popPose();
			}
		}
	}

	static void imgui(ImGraphics graphics, Entity entity) {
		var pin = PINS.get(entity.getUUID());

		if (ImGui.checkbox("Pin###pin-visible", pin != null && pin.enabled)) {
			if (pin != null) {
				pin.enabled = !pin.enabled;
			}
		}

		ImGui.sameLine();

		var imageImBuilder = IMAGE_IM_BUILDER.get();

		imageImBuilder.set(pin == null ? null : pin.getImage());

		if (imageImBuilder.imguiKey(graphics, "", "pin-image").isFull()) {
			if (pin == null) {
				pin = new Pin();
				PINS.put(entity.getUUID(), pin);
			}

			pin.setImage(imageImBuilder.isValid() ? imageImBuilder.build() : null);

			if (pin.isSet()) {
				pin.enabled = true;
			}
		}

		imageImBuilder.set(null);

		if (pin != null && pin.isSet()) {
			ImGui.sameLine();

			if (pin.shape.transparentBackground) {
				Color4ImBuilder.UNIT.set(pin.color);

				if (Color4ImBuilder.UNIT.imguiKey(graphics, "", "color").isAny()) {
					pin.color = Color4ImBuilder.UNIT.build();
				}
			} else {
				Color3ImBuilder.UNIT.set(pin.color);

				if (Color3ImBuilder.UNIT.imguiKey(graphics, "", "color").isAny()) {
					pin.color = Color3ImBuilder.UNIT.build();
				}
			}

			ImGui.sameLine();

			Color4ImBuilder.UNIT.set(pin.background);

			if (Color4ImBuilder.UNIT.imguiKey(graphics, "", "background").isAny()) {
				pin.background = Color4ImBuilder.UNIT.build();
			}

			ImGui.sameLine();

			ImGui.pushID("###pin-shape-button");

			if (graphics.imageButton(pin.shape.iconTexture, ImGui.getFrameHeight() - 4F, ImGui.getFrameHeight() - 4F, UV.FULL, 2, null)) {
				// pin.shape = PinShape.VALUES[(pin.shape.ordinal() + 1) % PinShape.VALUES.length];
				ImGui.openPopup("###pin-shape-popup");
			}

			if (ImGui.isItemHovered()) {
				ImGui.beginTooltip();
				ImGui.text("Shape: " + pin.shape.displayName);
				ImGui.image(graphics.mc.getTextureManager().getTexture(pin.shape.iconTexture).getTexture().vl$getHandle(), 64F, 64F);
				ImGui.endTooltip();
			}

			if (ImGui.beginPopup("###pin-shape-popup")) {
				for (int i = 0; i < PinShape.VALUES.length; i++) {
					if (i % 4 != 0) {
						ImGui.sameLine();
					}

					var shape = PinShape.VALUES[i];

					ImGui.pushID(i);

					if (graphics.imageButton(shape.iconTexture, 40F, 40F, UV.FULL, 2, null)) {
						pin.shape = shape;
						ImGui.closeCurrentPopup();
					}

					if (ImGui.isItemHovered()) {
						ImGui.setTooltip(shape.displayName);
						pin.shapeOverride = shape;
					}

					ImGui.popID();
				}

				ImGui.endPopup();
			} else {
				pin.shapeOverride = null;
			}

			ImGui.popID();
		}
	}

	static void fbVisualsMenu(ImGraphics graphics) {
		ImGui.checkbox("Enabled###pins-enabled", ENABLED);
		ImGui.sliderFloat("Pin Size###pin-size", PIN_SIZE.getData(), 0F, 1024F);
		ImGui.sliderFloat("Pin Offset###pin-offset", PIN_OFFSET.getData(), 0F, 1F);
		ImGui.sliderInt("Pin Alpha###pin-alpha", PIN_ALPHA.getData(), 1, 255);
	}
}
