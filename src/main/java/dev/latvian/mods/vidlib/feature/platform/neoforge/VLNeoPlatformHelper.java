package dev.latvian.mods.vidlib.feature.platform.neoforge;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.latvian.mods.klib.platform.PlatformType;
import dev.latvian.mods.klib.registry.CustomRegistryTypeCollector;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeType;
import dev.latvian.mods.vidlib.feature.camera.ScreenShakeTypeRegistryEvent;
import dev.latvian.mods.vidlib.feature.capture.PacketCapture;
import dev.latvian.mods.vidlib.feature.capture.PacketCaptureEvent;
import dev.latvian.mods.vidlib.feature.dynamicresources.DynamicResourceEvent;
import dev.latvian.mods.vidlib.feature.platform.VLPlatformHelper;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.latvian.mods.vidlib.feature.prop.Props;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffect;
import dev.latvian.mods.vidlib.feature.screeneffect.ScreenEffectRegistryEvent;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShape;
import dev.latvian.mods.vidlib.feature.zone.shape.ZoneShapeRegistryEvent;
import dev.mrbeastgaming.mods.hub.api.gateway.HubGatewayEventRegistry;
import dev.mrbeastgaming.mods.hub.api.gateway.HubServerGatewayEventRegistryEvent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
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
	public void collectDynamicResources(PackType type, Consumer<Identifier> callback) {
		ModLoader.postEvent(type == PackType.CLIENT_RESOURCES ? new DynamicResourceEvent.Assets(callback) : new DynamicResourceEvent.Data(callback));
	}

	@Override
	public void collectZoneShapeTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, ZoneShape> registry) {
		super.collectZoneShapeTypes(registry);
		ModLoader.postEvent(new ZoneShapeRegistryEvent(registry));
	}

	@Override
	public void collectScreenShakeTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, ScreenShakeType> registry) {
		super.collectScreenShakeTypes(registry);
		ModLoader.postEvent(new ScreenShakeTypeRegistryEvent(registry));
	}

	@Override
	public void collectScreenEffectTypes(CustomRegistryTypeCollector<RegistryFriendlyByteBuf, ScreenEffect> registry) {
		super.collectScreenEffectTypes(registry);
		ModLoader.postEvent(new ScreenEffectRegistryEvent(registry));
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
		if (FMLLoader.getCurrent().getDist().isClient()) {
			ProgressQueue.ACTIVE.add(queue);
		}
	}

	@Override
	public String getVersionString() {
		return super.getVersionString()
			+ "\nNeoForge:" + FMLLoader.getCurrent().getVersionInfo().neoForgeVersion();
	}
}
