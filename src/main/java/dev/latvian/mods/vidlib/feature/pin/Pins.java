package dev.latvian.mods.vidlib.feature.pin;

import dev.latvian.mods.klib.math.DistanceComparator;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.ClientAutoRegister;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.gallery.Gallery;
import dev.latvian.mods.vidlib.feature.gallery.GalleryImageImBuilder;
import dev.latvian.mods.vidlib.feature.gallery.PlayerBodies;
import dev.latvian.mods.vidlib.feature.gallery.PlayerHeads;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color3ImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color4ImBuilder;
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

	@ClientAutoRegister
	Gallery GALLERY = new Gallery("pins", () -> VidLibPaths.USER.get().resolve("pin-gallery"), TriState.TRUE);

	ImagePreProcessor PRE_PROCESSOR = ImagePreProcessor.FIT_SQUARE.andThen(ImagePreProcessor.CLOSEST_4);
	GalleryImageImBuilder.Uploader UPLOADER = new GalleryImageImBuilder.FileUploader(GALLERY, PRE_PROCESSOR);

	GalleryImageImBuilder IMAGE_IM_BUILDER = new GalleryImageImBuilder(
		List.of(GALLERY, PlayerBodies.GALLERY, PlayerHeads.GALLERY),
		List.of(UPLOADER, PlayerBodies.UPLOADER, PlayerHeads.UPLOADER)
	);

	static void draw(GuiGraphics graphics, DeltaTracker deltaTracker) {
		if (!ENABLED.get() || PINS.isEmpty()) {
			return;
		}

		var mc = Minecraft.getInstance();
		var level = mc.level;

		if (level == null) {
			return;
		}

		var worldMouse = mc.getWorldMouse();

		if (worldMouse == null) {
			return;
		}

		var delta = deltaTracker.getGameTimeDeltaPartialTick(false);
		int pinSize = (int) (PIN_SIZE.get() * mc.getEffectScale());

		var list = new ArrayList<ScreenPin>(PINS.size());

		for (var pin : PINS.values()) {
			if (pin.enabled && pin.isSet()) {
				var entity = level.getEntity(pin.uuid);

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
			var wpos = worldMouse.screen(screenPin.pos());

			if (wpos != null) {
				int pinAlpha = PIN_ALPHA.get() << 24;

				graphics.pose().pushPose();
				graphics.pose().translate(wpos.x(), wpos.y() - 2F, 0F);
				graphics.pose().translate(-pinSize / 2F, -pinSize * (1F + PIN_OFFSET.get()), 0F);
				graphics.pose().scale(pinSize / 512F, pinSize / 512F, 1F);

				var s = screenPin.pin().shape;

				screenPin.image().load(mc, true);
				graphics.blit(VidLibRenderTypes.GUI, s.maskTexture, s.x, s.y, 0F, 0F, s.w, s.h, s.w, s.h, pinAlpha | screenPin.pin().color.rgb());

				if (!screenPin.pin().background.isTransparent()) {
					graphics.blit(VidLibRenderTypes.GUI, s.maskTexture, s.x, s.y, 0F, 0F, s.w, s.h, s.w, s.h, screenPin.pin().background.withAlpha(screenPin.pin().background.alphaf() * (PIN_ALPHA.get() / 255F)).argb());
				}

				graphics.blit(s.maskedRenderType, screenPin.image().textureId(), s.x, s.y, 0F, 0F, s.w, s.h, s.w, s.h, pinAlpha | 0xFFFFFF);
				graphics.blit(VidLibRenderTypes.GUI, s.overlayTexture, 0, 0, 0F, 0F, 512, 512, 512, 512, pinAlpha | screenPin.pin().color.rgb());
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

		IMAGE_IM_BUILDER.set(pin == null ? null : pin.getImage());

		if (IMAGE_IM_BUILDER.imguiKey(graphics, "", "pin-image").isFull()) {
			if (pin == null) {
				pin = new Pin(entity.getUUID());
				PINS.put(pin.uuid, pin);
			}

			pin.setImage(IMAGE_IM_BUILDER.isValid() ? IMAGE_IM_BUILDER.build() : null);

			if (pin.isSet()) {
				pin.enabled = true;
			}
		}

		IMAGE_IM_BUILDER.set(null);

		if (pin != null && pin.isSet()) {
			ImGui.sameLine();

			Color3ImBuilder.UNIT.set(pin.color);

			if (Color3ImBuilder.UNIT.imguiKey(graphics, "", "color").isAny()) {
				pin.color = Color3ImBuilder.UNIT.build();
			}

			ImGui.sameLine();

			Color4ImBuilder.UNIT.set(pin.background);

			if (Color4ImBuilder.UNIT.imguiKey(graphics, "", "background").isAny()) {
				pin.background = Color4ImBuilder.UNIT.build();
			}

			ImGui.sameLine();

			if (graphics.imageButton(pin.shape.iconTexture, ImGui.getFrameHeight() - 4F, ImGui.getFrameHeight() - 4F, 0F, 0F, 1F, 1F, 2, null)) {
				pin.shape = PinShape.VALUES[(pin.shape.ordinal() + 1) % PinShape.VALUES.length];
			}

			if (ImGui.isItemHovered()) {
				ImGui.beginTooltip();
				ImGui.text("Shape: " + (pin.shape.ordinal() + 1));
				ImGui.image(graphics.mc.getTextureManager().getTexture(pin.shape.iconTexture).getTexture().vl$getHandle(), 64F, 64F);
				ImGui.endTooltip();
			}
		}
	}
}
