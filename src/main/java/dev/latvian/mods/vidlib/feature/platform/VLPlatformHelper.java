package dev.latvian.mods.vidlib.feature.platform;

import com.google.gson.JsonObject;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.auto.AutoCallback;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModification;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeType;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.font.TTFFile;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.mrbeastgaming.mods.hub.api.gateway.HubGatewayEvent;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class VLPlatformHelper {
	public static VLPlatformHelper CURRENT = new VLPlatformHelper();

	public void load(Class<? extends Annotation> annotation, Set<ElementType> elementTypes, AutoCallback callback) {
	}

	public void finishPacketCapture(PacketCapture packetCapture) {
	}

	public void packetCaptureMetadata(PacketCapture packetCapture, JsonObject metadata) {
	}

	public void collectDynamicResources(PackType type, Consumer<Identifier> callback) {
	}

	public void collectZoneShapes(CustomRegistryTypeCollector<ByteBuf, ZoneShape> registry) {
		ZoneShape.builtInTypes(registry);
	}

	public void collectIcons(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, Icon> registry) {
		Icon.builtInTypes(registry);
	}

	public void collectScreenShakeTypes(CustomRegistryTypeCollector<ByteBuf, ScreenShakeType> registry) {
		ScreenShakeType.builtInTypes(registry);
	}

	public void collectBulkLevelModifications(CustomRegistryTypeCollector<ByteBuf, BulkLevelModification> registry) {
		BulkLevelModification.builtInTypes(registry);
	}

	public void collectScreenEffects(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, ScreenEffect> registry) {
		ScreenEffect.builtInTypes(registry);
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

	public void collectGatewayEventHandlers(Map<String, Consumer<HubGatewayEvent>> map) {
	}
}
