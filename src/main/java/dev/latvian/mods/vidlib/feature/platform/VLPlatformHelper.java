package dev.latvian.mods.vidlib.feature.platform;

import com.google.gson.JsonObject;
import dev.latvian.mods.klib.KLib;
import dev.latvian.mods.klib.platform.PlatformType;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeType;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.cutscene.step.NewCutsceneStepType;
import dev.latvian.mods.vidlib.feature.font.TTFFile;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.latvian.mods.vidlib.feature.prop.Props;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.session.LevelSessionData;
import dev.latvian.mods.vidlib.feature.session.PlayerSessionData;
import dev.latvian.mods.vidlib.feature.session.ServerWorldSessionData;
import dev.latvian.mods.vidlib.feature.zone.ResolvedZone;
import dev.latvian.mods.vidlib.feature.zone.Zone;
import dev.latvian.mods.vidlib.feature.zone.ZoneHit;
import dev.mrbeastgaming.mods.hub.api.gateway.HubGatewayEventRegistry;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class VLPlatformHelper {
	public static VLPlatformHelper INSTANCE = new VLPlatformHelper();

	public PlatformType getPlatform() {
		return PlatformType.OTHER;
	}

	public void finishPacketCapture(PacketCapture packetCapture) {
	}

	public void packetCaptureMetadata(PacketCapture packetCapture, JsonObject metadata) {
	}

	public void collectDynamicResources(PackType type, Consumer<Identifier> callback) {
	}

	public void collectScreenShakeTypes(CustomRegistryTypeCollector<ByteBuf, ScreenShakeType> registry) {
		ScreenShakeType.builtInTypes(registry);
	}

	public void collectScreenEffectTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, ScreenEffect> registry) {
		ScreenEffect.builtInTypes(registry);
	}

	public void collectCutsceneStepTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, NewCutsceneStepType> registry) {
		NewCutsceneStepType.builtInTypes(registry);
	}

	public void collectTTFFiles(CustomRegistryTypeCollector<ByteBuf, TTFFile> registry) {
		TTFFile.builtInTypes(registry);
	}

	public boolean isStaff(Entity entity) {
		var gameMode = GameType.SURVIVAL;

		if (entity instanceof Player player) {
			gameMode = player.gameMode();
		}

		return CommonGameEngine.INSTANCE.isPlayerStaff(entity.entityTags(), gameMode);
	}

	public boolean isStaffOrTalent(Entity entity) {
		var gameMode = GameType.SURVIVAL;

		if (entity instanceof Player player) {
			gameMode = player.gameMode();
		}

		return CommonGameEngine.INSTANCE.isPlayerStaffOrTalent(entity.entityTags(), gameMode);
	}

	public Path getPlayerDataDirectory(MinecraftServer server) {
		return server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("vidlib");
	}

	public void collectServerGatewayEventHandlers(HubGatewayEventRegistry<MinecraftServer> registry) {
		HubServerGateway.registerBuiltIn(registry);
	}

	public Props<?> getProps(Level level) {
		throw new UnsupportedOperationException("Not supported on this platform");
	}

	public void pauseSaving(MinecraftServer server) {
		for (var level : server.getAllLevels()) {
			if (level != null) {
				level.noSave = true;
			}
		}

		server.saveEverything(true, true, true);
	}

	public void resumeSaving(MinecraftServer server) {
		for (var level : server.getAllLevels()) {
			if (level != null) {
				level.noSave = false;
			}
		}
	}

	public void displayProgressQueue(ProgressQueue queue) {
	}

	public String getVersionString() {
		return "KLib:" + KLib.VERSION + "\nVidLib:" + VidLib.VERSION;
	}

	public void generateZones(BiConsumer<String, Zone> zone) {
	}

	public void entityEnteredZone(ResolvedZone zone, Level level, Entity entity) {
	}

	public void entityExitedZone(ResolvedZone zone, Level level, Entity entity) {
	}

	public void playerClickedOnZone(Player player, ZoneHit hit) {
	}

	public ServerWorldSessionData getServerWorldData(MinecraftServer server) {
		throw new NullPointerException("WorldSessionData storage is not implemented");
	}

	public LevelSessionData getLevelData(Level level) {
		throw new NullPointerException("LevelSessionData storage is not implemented");
	}

	public PlayerSessionData getPlayerData(Player player) {
		throw new NullPointerException("PlayerSessionData storage is not implemented");
	}
}
