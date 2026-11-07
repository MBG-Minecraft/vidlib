package dev.latvian.mods.vidlib.feature.session;

import dev.latvian.mods.klib.core.KLibLocalPlayer;
import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.input.PlayerInputChangedEvent;
import dev.latvian.mods.vidlib.feature.input.SyncPlayerInputToServer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;

public class LocalClientPlayerSessionData extends ClientPlayerSessionData {
	public static LocalClientPlayerSessionData of(Player player) {
		throw new NoMixinException(player);
	}

	public final Minecraft mc;

	public LocalClientPlayerSessionData(ClientWorldSessionData worldData, Minecraft mc, UUID uuid) {
		super(worldData, uuid);
		this.mc = mc;
	}

	@ApiStatus.Internal
	public void preTick(ClientLevel level, LocalPlayer player) {
		updateOverrides(player);

		input = KLibLocalPlayer.fromInput(mc, player/*, mc.gui.screen() == null && mc.isWindowActive()*/);

		if (!prevInput.equals(input)) {
			NeoForge.EVENT_BUS.post(new PlayerInputChangedEvent(player, prevInput, input));
			prevInput = input;
			mc.c2s(new SyncPlayerInputToServer(input));
		}

		for (var otherPlayer : level.players()) {
			if (otherPlayer instanceof RemotePlayer p) {
				p.vl$sessionData().preTick(mc, level, p);
			}
		}

		if (mc.options.keyJump.isDown() && PlayerActionHandler.handle(player, PlayerActionType.JUMP, true)) {
			KeyMapping.set(mc.options.keyJump.getKey(), false);
			mc.options.keyJump.setDown(false);
		}

		if (mc.options.keyShift.isDown() && !mc.hasAltDown() && PlayerActionHandler.handle(player, PlayerActionType.SNEAK, true)) {
			KeyMapping.set(mc.options.keyShift.getKey(), false);
			mc.options.keyShift.setDown(false);
		}

		if (mc.options.keySprint.isDown() && PlayerActionHandler.handle(player, PlayerActionType.SPRINT, true)) {
			KeyMapping.set(mc.options.keySprint.getKey(), false);
			mc.options.keySprint.setDown(false);
		}
	}
}
