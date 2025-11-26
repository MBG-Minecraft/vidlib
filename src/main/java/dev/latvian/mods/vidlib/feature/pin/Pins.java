package dev.latvian.mods.vidlib.feature.pin;

import dev.latvian.mods.klib.math.DistanceComparator;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.auto.AutoInit;
import dev.latvian.mods.vidlib.feature.client.ImagePreProcessor;
import dev.latvian.mods.vidlib.feature.client.TexturedRenderType;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.gallery.Gallery;
import dev.latvian.mods.vidlib.feature.gallery.GalleryImageImBuilder;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.builder.Color3ImBuilder;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

public interface Pins {
	ImBoolean ENABLED = new ImBoolean(true);
	ImFloat PIN_SIZE = new ImFloat(256F);
	ImFloat PIN_OFFSET = new ImFloat(0F);
	ImInt PIN_ALPHA = new ImInt(255);

	Map<UUID, Pin> PINS = new Object2ObjectOpenHashMap<>();

	ResourceLocation PIN_TEXTURE = VidLib.id("textures/misc/pin/1.png");

	Gallery GALLERY = new Gallery("pins", () -> VidLibPaths.USER.get().resolve("pin-gallery"));

	ImagePreProcessor PRE_PROCESSOR = ImagePreProcessor.FIT_SQUARE.andThen(ImagePreProcessor.CLOSEST_4);
	GalleryImageImBuilder IMAGE_IM_BUILDER = new GalleryImageImBuilder(GALLERY, PRE_PROCESSOR);

	TexturedRenderType CIRCLE_RENDER_TYPE = VidLibRenderTypes.MASKED_GUI.apply(VidLib.id("textures/misc/circle.png"));

	@AutoInit(AutoInit.Type.TEXTURES_RELOADED)
	static void reload(TextureManager manager, Executor backgroundExecutor, Executor gameExecutor) throws IOException {
		GALLERY.load(manager, backgroundExecutor, gameExecutor);
	}

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
			if (pin.enabled) {
				var entity = level.getEntity(pin.uuid);

				if (entity != null) {
					var img = GALLERY.get(pin.texture);

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
				graphics.pose().translate(0F, -pinSize * PIN_OFFSET.get(), 0F);

				int x = -pinSize / 2;
				int y = -pinSize;
				int w = pinSize;
				int h = pinSize;

				int xi = Mth.floor(x + w * 103F / 512F);
				int yi = Mth.floor(y + h * 137F / 512F);
				int wi = Mth.ceil(w * 308F / 512F);
				int hi = Mth.ceil(h * 308F / 512F);

				// RenderSystem.setShaderTexture(1, null);

				graphics.blit(CIRCLE_RENDER_TYPE, screenPin.image().textureId(), xi, yi, 0F, 0F, wi, hi, wi, hi, pinAlpha | 0xFFFFFF);
				graphics.blit(VidLibRenderTypes.GUI, PIN_TEXTURE, x, y, 0F, 0F, w, h, w, h, pinAlpha | screenPin.pin().color.rgb());
				graphics.pose().popPose();
			}
		}
	}

	static void imgui(ImGraphics graphics, Entity entity) {
		var pin = PINS.get(entity.getUUID());

		if (pin != null) {
			if (ImGui.checkbox("Pin###pin-visible", pin.enabled)) {
				pin.enabled = !pin.enabled;
			}

			ImGui.sameLine();

			if (graphics.button("Remove Pin Image###pin-image", ImColorVariant.RED)) {
				PINS.remove(entity.getUUID());
			}

			ImGui.sameLine();

			Color3ImBuilder.UNIT.set(pin.color);

			if (Color3ImBuilder.UNIT.imgui(graphics).isAny()) {
				pin.color = Color3ImBuilder.UNIT.build();
			}
		} else {
			if (IMAGE_IM_BUILDER.imguiKey(graphics, "Pin", "pin-image").isFull()) {
				if (IMAGE_IM_BUILDER.isValid()) {
					var newPin = new Pin(entity.getUUID());
					newPin.texture = IMAGE_IM_BUILDER.build().id();
					PINS.put(newPin.uuid, newPin);
				} else {
					PINS.remove(entity.getUUID());
				}

				IMAGE_IM_BUILDER.set(null);
			}
		}
	}
}
