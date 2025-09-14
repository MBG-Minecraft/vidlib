package dev.latvian.mods.vidlib.feature.visual;

import dev.latvian.mods.klib.math.DistanceComparator;
import dev.latvian.mods.vidlib.feature.client.VidLibRenderTypes;
import dev.latvian.mods.vidlib.feature.imgui.builder.GameProfileImBuilder;
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

	record Pin(String name, ImBoolean enabled, ResourceLocation texture) {}

	GameProfileImBuilder newPinProfileBuilder = new GameProfileImBuilder();
	ImBoolean ENABLED = new ImBoolean(false);
	ImFloat PIN_OFFSET = new ImFloat(0F);
	ImFloat PIN_SIZE = new ImFloat(1F);
	ImInt PIN_ALPHA = new ImInt(255);

	Map<UUID, Pin> PINS = new HashMap<>();

	static void draw(GuiGraphics graphics, DeltaTracker deltaTracker) {
	    var minecraft = Minecraft.getInstance();
	    var level = minecraft.level;
	    if (level == null) {
	        return;
	    }

		var worldMouse = minecraft.getWorldMouse();
	    if (worldMouse == null) {
	        return;
	    }

	    var delta = deltaTracker.getGameTimeDeltaPartialTick(false);

		for (var player : level.players().stream().sorted(new DistanceComparator<>(minecraft.gameRenderer.getMainCamera().getPosition(), Entity::position)).toList()) {
			var pin = PINS.get(player.getUUID());
			if (pin == null || !pin.enabled.get()) {
				continue;
			}

			var pos = player.getPosition(delta).add(0D, player.getBbHeight() * 1.1D, 0D);
			var color = (PIN_ALPHA.get() << 24) | 0xFFFFFF;
			var wpos = worldMouse.screen(pos);
			if (wpos != null) {
				graphics.pose().pushPose();
				graphics.pose().translate(wpos.x(), wpos.y() - 2F - PIN_OFFSET.get(), 0F);
				graphics.pose().scale(PIN_SIZE.get(), PIN_SIZE.get(), 1F);

				int s = 64;
				graphics.blit(VidLibRenderTypes.GUI_BLUR, pin.texture, -s / 2, -s, 0F, 0F, s, s, s, s, color);
				graphics.pose().popPose();
			}
		}
	}
}
