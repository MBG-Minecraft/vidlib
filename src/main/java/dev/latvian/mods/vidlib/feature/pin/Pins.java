package dev.latvian.mods.vidlib.feature.pin;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.util.UndashedUuid;
import dev.latvian.mods.klib.math.DistanceComparator;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.imgui.AsyncFileSelector;
import dev.latvian.mods.vidlib.feature.imgui.ImColorVariant;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.world.entity.Entity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface Pins {
	ImBoolean ENABLED = new ImBoolean(true);
	ImFloat PIN_SIZE = new ImFloat(256F);
	ImFloat PIN_OFFSET = new ImFloat(0F);
	ImInt PIN_ALPHA = new ImInt(255);

	Map<UUID, Pin> PINS = new HashMap<>();

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
			if (pin.enabled().get()) {
				var entity = level.getEntity(pin.uuid());

				if (entity != null) {
					list.add(new ScreenPin(entity, pin, entity.getPosition(delta).add(0D, entity.getBbHeight() * 1.1D, 0D)));
				}
			}
		}

		if (list.isEmpty()) {
			return;
		} else if (list.size() >= 2) {
			list.sort(new DistanceComparator<>(mc.gameRenderer.getMainCamera().getPosition(), ScreenPin::pos));
		}

		for (var screenPin : list) {
			var color = (PIN_ALPHA.get() << 24) | 0xFFFFFF;
			var wpos = worldMouse.screen(screenPin.pos());

			if (wpos != null) {
				graphics.pose().pushPose();
				graphics.pose().translate(wpos.x(), wpos.y() - 2F, 0F);
				graphics.pose().translate(0F, -pinSize * PIN_OFFSET.get(), 0F);
				graphics.blit(VidLibRenderTypes.GUI_BLUR, screenPin.pin().texture(), -pinSize / 2, -pinSize, 0F, 0F, pinSize, pinSize, pinSize, pinSize, color);
				graphics.pose().popPose();
			}
		}
	}

	static void imgui(ImGraphics graphics, Entity entity) {
		var pin = PINS.get(entity.getUUID());

		if (pin != null) {
			ImGui.checkbox("Pin###pin-visible", pin.enabled());
			ImGui.sameLine();

			if (graphics.button("Remove Pin Image###pin-image", ImColorVariant.RED)) {
				PINS.remove(entity.getUUID());
			}
		} else if (ImGui.button("Set Pin Image...###pin-image")) {
			AsyncFileSelector.openFileDialog(null, "Select Pin Image", "png").thenAccept(pathString -> {
				var path = pathString == null ? null : Path.of(pathString);

				if (path != null && Files.exists(path)) {
					graphics.mc.execute(() -> {
						try (var stream = Files.newInputStream(path)) {
							var resourceLocation = VidLib.id("textures/vidlib/cache/pins/" + UndashedUuid.toString(entity.getUUID()) + ".png");
							var image = NativeImage.read(stream);
							var texture = new DynamicTexture(() -> entity.getUUID().toString(), image);
							graphics.mc.getTextureManager().register(resourceLocation, texture);
							PINS.put(entity.getUUID(), new Pin(entity.getUUID(), entity.getScoreboardName(), new ImBoolean(true), resourceLocation, pathString));
						} catch (IOException ex) {
							throw new RuntimeException(ex);
						}
					});
				}
			});
		}
	}
}
