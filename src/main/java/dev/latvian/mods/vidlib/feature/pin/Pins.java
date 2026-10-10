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
import dev.latvian.mods.vidlib.feature.imgui.ImGuiUtils;
import dev.latvian.mods.vidlib.feature.imgui.MenuItem;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color3ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.type.ImBoolean;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Pins {
	ImBoolean ENABLED = new ImBoolean(true);

	Map<UUID, Pin> PINS = new Object2ObjectOpenHashMap<>();
	Map<UUID, Vec3> LAST_KNOWN_POSITIONS = new Object2ObjectOpenHashMap<>();

	ImagePreProcessor PRE_PROCESSOR = ImagePreProcessor.FIT_SQUARE.andThen(ImagePreProcessor.CLOSEST_4);

	@ClientAutoRegister
	Gallery<UUID> GALLERY = Gallery.ofUUIDKey("pins", () -> VidLibPaths.USER.get().resolve("pin-gallery"), TriState.TRUE).addUploader(new GalleryFileUploader<>(PathIDGenerator.RANDOM_UUID, PRE_PROCESSOR));

	List<Gallery<?>> PIN_GALLERIES = new ArrayList<>(List.of(GALLERY, PlayerBodies.GALLERY, PlayerHeads.GALLERY, PlayerBodies.GALLERY_NO_LAYERS, PlayerHeads.GALLERY_NO_LAYERS));
	Lazy<GalleryImageImBuilder> IMAGE_IM_BUILDER = Lazy.of(() -> new GalleryImageImBuilder(PIN_GALLERIES));

	MenuItem MENU_ITEM = MenuItem.item(ImIcons.LOCATION, "Pins", PinsPanel.INSTANCE);

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

		var list = new ArrayList<ScreenPin>(PINS.size());

		for (var entry : PINS.entrySet()) {
			var pin = entry.getValue();

			if (pin.enabled && pin.isSet()) {
				var uuid = entry.getKey();
				var entity = level.getEntity(uuid);

				if (entity != null) {
					var pos = entity.getPosition(delta).add(0D, entity.getBbHeight() * 1.1D, 0D);
					LAST_KNOWN_POSITIONS.put(uuid, pos);
					var img = pin.getImage();

					if (img != null) {
						list.add(new ScreenPin(entity, pin, img, pos));
					}
				} else if (pin.alwaysLoaded) {
					var lastPos = LAST_KNOWN_POSITIONS.get(uuid);

					if (lastPos != null) {
						var img = pin.getImage();

						if (img != null) {
							list.add(new ScreenPin(null, pin, img, lastPos));
						}
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
				var pin = screenPin.pin();
				int pinAlpha = pin.alpha << 24;
				int pinSize = (int) (pin.size * mc.getEffectScale());

				graphics.pose().pushPose();
				graphics.pose().translate(wpos.x(), wpos.y() - 2F, 0F);
				graphics.pose().translate(-pinSize / 2F, -pinSize * (1F + pin.offset), 0F);
				graphics.pose().scale(pinSize / 512F, pinSize / 512F, 1F);

				var shape = pin.shapeOverride == null ? pin.shape : pin.shapeOverride;
				int size = shape.size;
				int color = shape.transparentBackground ? pin.color.argb() : (pinAlpha | pin.color.rgb());

				screenPin.image().load(mc, true);

				if (!shape.transparentBackground) {
					graphics.blit(VidLibRenderTypes.GUI, shape.maskTexture, shape.x, shape.y, 0F, 0F, size, size, size, size, color);
				}

				if (!pin.background.isTransparent()) {
					graphics.blit(VidLibRenderTypes.GUI, shape.maskTexture, shape.x, shape.y, 0F, 0F, size, size, size, size, pin.background.withAlpha(pin.background.alphaf() * (pin.alpha / 255F)).argb());
				}

				graphics.blit(shape.maskedRenderType, screenPin.image().textureId(), shape.x, shape.y, 1F, 1F, size - 2, size - 2, size, size, pinAlpha | 0xFFFFFF);

				if (shape.overlayTexture != null) {
					graphics.blit(VidLibRenderTypes.GUI, shape.overlayTexture, 0, 0, 0F, 0F, 512, 512, 512, 512, color);
				}

				graphics.pose().popPose();
			}
		}
	}

	static void appearanceSliders(Pin pin) {
		ImGuiUtils.FLOAT.set(pin.size);

		if (ImGui.sliderFloat("Size###pin-size", ImGuiUtils.FLOAT.getData(), 0F, 1024F)) {
			pin.size = ImGuiUtils.FLOAT.get();
		}

		ImGuiUtils.FLOAT.set(pin.offset);

		if (ImGui.sliderFloat("Offset###pin-offset", ImGuiUtils.FLOAT.getData(), 0F, 1F)) {
			pin.offset = ImGuiUtils.FLOAT.get();
		}

		ImGuiUtils.INT.set(pin.alpha);

		if (ImGui.sliderInt("Alpha###pin-alpha", ImGuiUtils.INT.getData(), 1, 255)) {
			pin.alpha = ImGuiUtils.INT.get();
		}
	}

	static void imgui(ImGraphics graphics, Entity entity) {
		if (!ImGui.collapsingHeader("Pin")) {
			return;
		}

		settings(graphics, entity.getUUID(), PINS.get(entity.getUUID()), entity);
	}

	static void settings(ImGraphics graphics, UUID uuid, Pin pin, @Nullable Entity entity) {
		var imageImBuilder = IMAGE_IM_BUILDER.get();
		imageImBuilder.contextId = uuid;
		imageImBuilder.contextEntity = entity;
		imageImBuilder.set(pin == null ? null : pin.getImage());

		if (imageImBuilder.imguiKey(graphics, "", "pin-image").isFull()) {
			if (pin == null) {
				pin = new Pin();
				PINS.put(uuid, pin);
			}

			pin.setImage(imageImBuilder.isValid() ? imageImBuilder.build() : null);

			if (pin.isSet()) {
				pin.enabled = true;
			}
		}

		imageImBuilder.set(null);

		if (pin == null || !pin.isSet()) {
			return;
		}

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

		if (ImGui.isItemHovered() && graphics.beginTooltip()) {
			ImGui.text("Shape: " + pin.shape.displayName);
			ImGui.image(graphics.mc.getTextureManager().getTexture(pin.shape.iconTexture).getTexture().vl$getHandle(), 64F, 64F);
			graphics.endTooltip();
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
					graphics.tooltip(shape.displayName);
					pin.shapeOverride = shape;
				}

				ImGui.popID();
			}

			ImGui.endPopup();
		} else {
			pin.shapeOverride = null;
		}

		if (ImGui.checkbox("Enabled###pin-visible", pin.enabled)) {
			pin.enabled = !pin.enabled;
		}

		ImGui.sameLine();
		if (ImGui.checkbox("Always Loaded###pin-always-loaded", pin.alwaysLoaded)) {
			pin.alwaysLoaded = !pin.alwaysLoaded;
		}

		ImGui.popID();

		appearanceSliders(pin);
	}

}
