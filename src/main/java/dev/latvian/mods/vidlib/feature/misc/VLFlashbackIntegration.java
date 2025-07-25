package dev.latvian.mods.vidlib.feature.misc;

import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.imgui.ImGraphics;
import dev.latvian.mods.vidlib.feature.imgui.ImNumberType;
import dev.latvian.mods.vidlib.feature.net.S2CPacketBundleBuilder;
import dev.latvian.mods.vidlib.feature.prop.PropHitResult;
import dev.latvian.mods.vidlib.feature.prop.PropListType;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientConfigurationPacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class VLFlashbackIntegration {
	private static int selectedProp = 0;
	private static PropListType selectedPropList = PropListType.LEVEL;
	private static boolean openSelectedPropPopup = false;

	public static void init() {
		VidLib.LOGGER.info("Flashback integration loaded");
		FlashbackIntegration.CONFIG_SNAPSHOT.add(VLFlashbackIntegration::configSnapshot);
		FlashbackIntegration.GAME_SNAPSHOT.add(VLFlashbackIntegration::gameSnapshot);
		FlashbackIntegration.ENTITY_SNAPSHOT.add(VLFlashbackIntegration::entitySnapshot);
		FlashbackIntegration.ENTITY_MENU.add(VLFlashbackIntegration::entityMenu);
		FlashbackIntegration.VISUALS_MENU.add(VLFlashbackIntegration::visualsMenu);
		FlashbackIntegration.RENDER_FILTER_MENU.add(VLFlashbackIntegration::renderFilterMenu);
		FlashbackIntegration.CLICK_TARGET.add(VLFlashbackIntegration::clickTarget);
		FlashbackIntegration.HANDLE_CLICK_TARGET.add(VLFlashbackIntegration::handleClickTarget);
		FlashbackIntegration.POPUPS.add(VLFlashbackIntegration::popups);
	}

	private static void configSnapshot(List<Packet<? super ClientConfigurationPacketListener>> packets) {
		// VidLib.LOGGER.info("Flashback Config snapshot");
	}

	private static void gameSnapshot(List<Packet<? super ClientGamePacketListener>> packets) {
		VidLib.LOGGER.info("Flashback Game snapshot");

		var mc = Minecraft.getInstance();
		var packets2 = new S2CPacketBundleBuilder(mc.level);
		mc.player.vl$sessionData().sync(packets2, mc.player, 1);
		packets2.sendUnbundled(packets::add);
	}

	private static void entitySnapshot(Entity entity, List<Packet<? super ClientGamePacketListener>> packets) {
		var packets2 = new S2CPacketBundleBuilder(Minecraft.getInstance().level);
		entity.replaySnapshot(packets2);
		packets2.sendUnbundled(packets::add);
	}

	private static void entityMenu(Entity entity) {
	}

	private static void visualsMenu() {
	}

	private static void renderFilterMenu() {
	}

	@Nullable
	private static HitResult clickTarget(Vec3 from, Vec3 to) {
		var mc = Minecraft.getInstance();
		var ctx = new ClipContext(from, to, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player);
		var result = mc.level.getProps().clip(ctx, true);
		return result;
	}

	private static boolean handleClickTarget(HitResult result) {
		if (result instanceof PropHitResult propResult) {
			selectedProp = propResult.prop.id;
			selectedPropList = propResult.prop.spawnType.listType;
			openSelectedPropPopup = true;
			return true;
		}

		return false;
	}

	private static void popups() {
		if (openSelectedPropPopup) {
			ImGui.openPopup("###vidlib-prop-popup");
			openSelectedPropPopup = false;
		}

		if (selectedProp != 0 && ImGui.beginPopup("###vidlib-prop-popup")) {
			var mc = Minecraft.getInstance();
			var propList = mc.level.getProps().propLists.get(selectedPropList);
			var prop = propList == null ? null : propList.get(selectedProp);

			if (prop != null) {
				ImGui.text(prop.toString());
				var graphics = new ImGraphics(mc);
				graphics.pushStack();
				graphics.setDefaultStyle();
				graphics.setNumberType(ImNumberType.DOUBLE);
				graphics.setNumberRange(null);
				prop.imgui(graphics, mc.getDeltaTracker().getGameTimeDeltaPartialTick(false));
				graphics.popStack();
			}

			ImGui.endPopup();
		}

		if (!ImGui.isPopupOpen("###vidlib-prop-popup")) {
			selectedProp = 0;
		}
	}
}
