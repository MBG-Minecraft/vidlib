package dev.mrbeastgaming.mods.hub.api.gateway.event;

import com.mojang.serialization.JsonOps;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.api.HubClientSessionData;
import dev.mrbeastgaming.mods.hub.api.HubUserCapabilities;
import dev.mrbeastgaming.mods.hub.api.HubUserFlags;
import dev.mrbeastgaming.mods.hub.api.gateway.HubGatewayEventRegistryEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.function.BiConsumer;

@EventBusSubscriber(modid = VidLib.ID, value = Dist.CLIENT)
public class ClientGatewayEvents {
	public static void registerSynced(HubGatewayEventRegistryEvent event, String method, BiConsumer<Minecraft, HubGatewayEvent> callback) {
		event.register(method, e -> Minecraft.getInstance().execute(() -> callback.accept(Minecraft.getInstance(), e)));
	}

	@SubscribeEvent
	public static void hubGatewayEventRegistry(HubGatewayEventRegistryEvent event) {
		registerSynced(event, "restart", ClientGatewayEvents::restart);
		registerSynced(event, "toast", ClientGatewayEvents::toast);
		event.register("flags", ClientGatewayEvents::flags);
		event.register("capabilities", ClientGatewayEvents::capabilities);
	}

	private static void restart(Minecraft mc, HubGatewayEvent event) {
		if (mc.level != null) {
			mc.vl$exitToTitle();
		}

		// Display GUI
		mc.stop();
	}

	private static void toast(Minecraft mc, HubGatewayEvent event) {
		var params = event.paramsObject();
		var title = params.get("title").getAsString();
		var subtitle = params.has("subtitle") ? params.get("subtitle").getAsString() : "";
		mc.toast(Component.literal(title), subtitle.isEmpty() ? Component.empty() : Component.literal(subtitle));
	}

	private static void flags(HubGatewayEvent event) {
		var data = HubClientSessionData.CURRENT;

		if (data != null) {
			var flags = HubUserFlags.CODEC.parse(JsonOps.INSTANCE, event.paramsObject()).getOrThrow();
			HubClientSessionData.CURRENT = data.withUser(data.user().withFlags(flags));
		}
	}

	private static void capabilities(HubGatewayEvent event) {
		var data = HubClientSessionData.CURRENT;

		if (data != null) {
			var capabilities = HubUserCapabilities.CODEC.parse(JsonOps.INSTANCE, event.paramsObject()).getOrThrow();
			HubClientSessionData.CURRENT = data.withCapabilities(capabilities);
		}
	}
}
