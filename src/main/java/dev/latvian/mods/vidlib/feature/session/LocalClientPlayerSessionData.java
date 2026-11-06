package dev.latvian.mods.vidlib.feature.session;

import com.mojang.blaze3d.platform.Window;
import dev.latvian.mods.klib.core.KLibLocalPlayer;
import dev.latvian.mods.klib.core.NoMixinException;
import dev.latvian.mods.klib.registry.Ref;
import dev.latvian.mods.klib.util.PauseType;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.actor.ActorParticleOptions;
import dev.latvian.mods.vidlib.feature.actor.ActorRecording;
import dev.latvian.mods.vidlib.feature.atmosphere.Atmosphere;
import dev.latvian.mods.vidlib.feature.atmosphere.ClientAtmosphere;
import dev.latvian.mods.vidlib.feature.data.DataMapOverrides;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionHandler;
import dev.latvian.mods.vidlib.feature.entity.PlayerActionType;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.input.PlayerInputChangedEvent;
import dev.latvian.mods.vidlib.feature.input.SyncPlayerInputToServer;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import dev.latvian.mods.vidlib.util.ScheduledTask;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedOutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
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
	public void preTick(ClientLevel level, LocalPlayer player, Window window, PauseType paused) {
		if (DataMapOverrides.INSTANCE != null) {
			serverDataMap.overrides = DataMapOverrides.INSTANCE.serverData;

			for (var session : getAllClientSessionData()) {
				session.dataMap.overrides = DataMapOverrides.INSTANCE.getPlayerMap(session.uuid);
			}
		}

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

	@ApiStatus.Internal
	public void postTick(ClientLevel level, @Nullable LocalPlayer player, PauseType paused) {
		if (scheduledTaskHandler != null && paused.tick()) {
			scheduledTaskHandler.tick();
		}

	}
}
