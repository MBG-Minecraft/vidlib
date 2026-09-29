package dev.latvian.mods.vidlib.feature.platform;

import com.google.gson.JsonObject;
import dev.latvian.mods.klib.platform.PlatformType;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModification;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeType;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumber;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.latvian.mods.vidlib.feature.prop.Props;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.mrbeastgaming.mods.hub.api.gateway.HubGatewayEventRegistry;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGateway;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;
import java.util.function.Consumer;

public class VLPlatformHelper {
	public static VLPlatformHelper CURRENT = new VLPlatformHelper();

	public PlatformType getPlatform() {
		return PlatformType.OTHER;
	}

	public void finishPacketCapture(PacketCapture packetCapture) {
	}

	public void packetCaptureMetadata(PacketCapture packetCapture, JsonObject metadata) {
	}

	public void collectDynamicResources(PackType type, Consumer<ResourceLocation> callback) {
	}

	public void collectKNumbers(SimpleRegistryCollector<KNumber> registry) {
		KNumber.builtinTypes(registry);
	}

	public void collectKVectors(SimpleRegistryCollector<KVector> registry) {
		KVector.builtinTypes(registry);
	}

	public void collectEntityFilters(SimpleRegistryCollector<EntityFilter> registry) {
		EntityFilter.builtinTypes(registry);
	}

	public void collectBlockFilters(SimpleRegistryCollector<BlockFilter> registry) {
		BlockFilter.builtinTypes(registry);
	}

	public void collectZoneShapes(SimpleRegistryCollector<ZoneShape> registry) {
		ZoneShape.builtinTypes(registry);
	}

	public void collectIcons(SimpleRegistryCollector<Icon> registry) {
		Icon.builtinTypes(registry);
	}

	public void collectScreenShakeTypes(SimpleRegistryCollector<ScreenShakeType> registry) {
		ScreenShakeType.builtinTypes(registry);
	}

	public void collectBulkLevelModifications(SimpleRegistryCollector<BulkLevelModification> registry) {
		BulkLevelModification.builtinTypes(registry);
	}

	public void collectScreenEffects(SimpleRegistryCollector<ScreenEffect> registry) {
		ScreenEffect.builtinTypes(registry);
	}

	public void collectEntityNumbers(SimpleRegistryCollector<EntityNumber> registry) {
		EntityNumber.builtinTypes(registry);
	}

	public boolean isStaff(Entity entity) {
		var gameMode = GameType.SURVIVAL;

		if (entity instanceof Player player) {
			gameMode = player.gameMode();
		}

		return CommonGameEngine.INSTANCE.isPlayerStaff(entity.getTags(), gameMode);
	}

	public boolean isStaffOrTalent(Entity entity) {
		var gameMode = GameType.SURVIVAL;

		if (entity instanceof Player player) {
			gameMode = player.gameMode();
		}

		return CommonGameEngine.INSTANCE.isPlayerStaffOrTalent(entity.getTags(), gameMode);
	}

	public Path getPlayerDataDirectory(MinecraftServer server) {
		return server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve("vidlib");
	}

	public void collectServerGatewayEventHandlers(HubGatewayEventRegistry<MinecraftServer> registry) {
		HubServerGateway.registerBuiltIn(registry);
	}

	public boolean isReplayLevel(Level level) {
		return false;
	}

	public boolean isReplayServer(MinecraftServer server) {
		return false;
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
}
