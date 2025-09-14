package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.klib.math.DistanceComparator;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface PlayerPins {
	record Pin(UUID uuid, String name, ImBoolean enabled, ResourceLocation texture, String path) {
	}

	ImBoolean ENABLED = new ImBoolean(true);
	ImFloat PIN_SIZE = new ImFloat(256F);
	ImFloat PIN_OFFSET = new ImFloat(0F);
	ImInt PIN_ALPHA = new ImInt(255);

	Map<UUID, Pin> PINS = new HashMap<>();

	static void draw(GuiGraphics graphics, DeltaTracker deltaTracker) {
		if (!ENABLED.get()) {
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

		for (var player : level.players().stream().sorted(new DistanceComparator<>(mc.gameRenderer.getMainCamera().getPosition(), Entity::position)).toList()) {
			var pin = PINS.get(player.getUUID());

			if (pin == null || !pin.enabled.get()) {
				continue;
			}

			var pos = player.getPosition(delta).add(0D, player.getBbHeight() * 1.1D, 0D);
			var color = (PIN_ALPHA.get() << 24) | 0xFFFFFF;

			var wpos = worldMouse.screen(pos);

			if (wpos != null) {
				graphics.pose().pushPose();
				graphics.pose().translate(wpos.x(), wpos.y() - 2F, 0F);
				graphics.pose().translate(0F, -pinSize * PIN_OFFSET.get(), 0F);
				graphics.blit(VidLibRenderTypes.GUI_BLUR, pin.texture, -pinSize / 2, -pinSize, 0F, 0F, pinSize, pinSize, pinSize, pinSize, color);
				graphics.pose().popPose();
			}
		}
	}
}
