package dev.latvian.mods.vidlib.feature.platform.neoforge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.klib.platform.PlatformType;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilter;
import dev.latvian.mods.vidlib.feature.block.filter.BlockFilterRegistryEvent;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModification;
import dev.latvian.mods.vidlib.feature.bulk.BulkLevelModificationRegistryEvent;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeType;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeTypeRegistryEvent;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.capture.PacketCaptureEvent;
import dev.latvian.mods.vidlib.feature.dynamicresources.DynamicResourceEvent;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilter;
import dev.latvian.mods.vidlib.feature.entity.filter.EntityFilterRegistryEvent;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumber;
import dev.latvian.mods.vidlib.feature.entity.number.EntityNumberRegistryEvent;
import dev.latvian.mods.vidlib.feature.icon.Icon;
import dev.latvian.mods.vidlib.feature.icon.IconRegistryEvent;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.latvian.mods.vidlib.feature.prop.Props;
import dev.latvian.mods.vidlib.feature.registry.SimpleRegistryCollector;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectRegistryEvent;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShapeRegistryEvent;
import dev.latvian.mods.vidlib.math.knumber.KNumber;
import dev.latvian.mods.vidlib.math.knumber.KNumberRegistryEvent;
import dev.latvian.mods.vidlib.math.kvector.KVector;
import dev.latvian.mods.vidlib.math.kvector.KVectorRegistryEvent;
import dev.mrbeastgaming.mods.hub.api.gateway.HubGatewayEventRegistry;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGatewayEventRegistryEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

public class VLNeoPlatformHelper extends VLPlatformHelper {
	public final ModContainer mod;

	public VLNeoPlatformHelper(ModContainer mod) {
		this.mod = mod;
	}

	@Override
	public PlatformType getPlatform() {
		return PlatformType.NEOFORGE;
	}

	@Override
	public void finishPacketCapture(PacketCapture packetCapture) {
		NeoForge.EVENT_BUS.post(new PacketCaptureEvent.Finished(packetCapture));
	}

	@Override
	public void packetCaptureMetadata(PacketCapture packetCapture, JsonObject metadata) {
		NeoForge.EVENT_BUS.post(new PacketCaptureEvent.Metadata(packetCapture, metadata));

		var ml = new JsonArray();

		for (var mod : ModList.get().getMods()) {
			var json = new JsonObject();
			json.addProperty("id", mod.getModId());
			json.addProperty("name", mod.getDisplayName());
			json.addProperty("version", mod.getVersion().toString());
			ml.add(json);
		}

		metadata.add("mod_list", ml);
	}

	@Override
	public void collectDynamicResources(PackType type, Consumer<ResourceLocation> callback) {
		ModLoader.postEvent(type == PackType.CLIENT_RESOURCES ? new DynamicResourceEvent.Assets(callback) : new DynamicResourceEvent.Data(callback));
	}

	@Override
	public void collectKNumbers(SimpleRegistryCollector<KNumber> registry) {
		super.collectKNumbers(registry);
		ModLoader.postEvent(new KNumberRegistryEvent(registry));
	}

	@Override
	public void collectKVectors(SimpleRegistryCollector<KVector> registry) {
		super.collectKVectors(registry);
		ModLoader.postEvent(new KVectorRegistryEvent(registry));
	}

	@Override
	public void collectEntityFilters(SimpleRegistryCollector<EntityFilter> registry) {
		super.collectEntityFilters(registry);
		ModLoader.postEvent(new EntityFilterRegistryEvent(registry));
	}

	@Override
	public void collectBlockFilters(SimpleRegistryCollector<BlockFilter> registry) {
		super.collectBlockFilters(registry);
		ModLoader.postEvent(new BlockFilterRegistryEvent(registry));
	}

	@Override
	public void collectZoneShapes(SimpleRegistryCollector<ZoneShape> registry) {
		super.collectZoneShapes(registry);
		ModLoader.postEvent(new ZoneShapeRegistryEvent(registry));
	}

	@Override
	public void collectIcons(SimpleRegistryCollector<Icon> registry) {
		super.collectIcons(registry);
		ModLoader.postEvent(new IconRegistryEvent(registry));
	}

	@Override
	public void collectScreenShakeTypes(SimpleRegistryCollector<ScreenShakeType> registry) {
		super.collectScreenShakeTypes(registry);
		ModLoader.postEvent(new ScreenShakeTypeRegistryEvent(registry));
	}

	@Override
	public void collectBulkLevelModifications(SimpleRegistryCollector<BulkLevelModification> registry) {
		super.collectBulkLevelModifications(registry);
		ModLoader.postEvent(new BulkLevelModificationRegistryEvent(registry));
	}

	@Override
	public void collectScreenEffects(SimpleRegistryCollector<ScreenEffect> registry) {
		super.collectScreenEffects(registry);
		ModLoader.postEvent(new ScreenEffectRegistryEvent(registry));
	}

	@Override
	public void collectEntityNumbers(SimpleRegistryCollector<EntityNumber> registry) {
		super.collectEntityNumbers(registry);
		ModLoader.postEvent(new EntityNumberRegistryEvent(registry));
	}

	@Override
	public boolean isStaff(Entity entity) {
		return entity.isStaff();
	}

	@Override
	public boolean isStaffOrTalent(Entity entity) {
		return entity.isStaffOrTalent();
	}

	@Override
	public void collectServerGatewayEventHandlers(HubGatewayEventRegistry<MinecraftServer> registry) {
		super.collectServerGatewayEventHandlers(registry);
		NeoForge.EVENT_BUS.post(new HubServerGatewayEventRegistryEvent(registry));
	}

	@Override
	public boolean isReplayLevel(Level level) {
		return level.vl$isReplayLevel();
	}

	@Override
	public boolean isReplayServer(MinecraftServer server) {
		return server.vl$isReplayServer();
	}

	@Override
	public Props<?> getProps(Level level) {
		return level.getProps();
	}

	@Override
	public void displayProgressQueue(ProgressQueue queue) {
		if (FMLLoader.getDist().isClient()) {
			ProgressQueue.ACTIVE.add(queue);
		}
	}
}
