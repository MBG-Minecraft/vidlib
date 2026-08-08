package dev.mrbeastgaming.mods.hub.api.gateway;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.mrbeastgaming.mods.hub.api.HubServerSessionData;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.net.URI;

public class HubServerGateway extends HubCommonGateway<MinecraftServer> {
	public static HubServerGateway instance;

	@Nullable
	public static HubServerGateway startGateway(MinecraftServer server, @Nullable URI uri) {
		stopGateway();
		var gateway = instance;

		if (gateway == null && uri != null) {
			gateway = new HubServerGateway(server, uri);
			gateway.start();
			instance = gateway;
		}

		return gateway;
	}

	public static void stopGateway() {
		var gateway = instance;

		if (gateway != null) {
			gateway.stop();
			instance = null;
		}
	}

	public static void tickGateway() {
		var gateway = instance;

		if (gateway != null) {
			gateway.tick();
		}
	}

	public static void updateInfo(MinecraftServer server, HubServerGateway gateway) {
		if (server.isDedicatedServer()) {
			gateway.sendName("Port " + server.getPort() + "\n" + ChatFormatting.stripFormatting(server.getMotd().replace("\\n", "\n")));
		} else {
			gateway.sendName(ChatFormatting.stripFormatting(server.getMotd().replace("\\n", "\n")));
		}

		gateway.sendStatus(server.getPlayerCount() + " Online");
	}

	public static JsonObject entityToJson(Entity entity) {
		var json = new JsonObject();
		json.addProperty("uuid", entity.getUUID().toString());
		json.addProperty("name", entity.getScoreboardName());
		json.addProperty("dimension", entity.level().dimension().location().toString());
		return json;
	}

	public static void playerLoggedIn(ServerPlayer player) {
		var gateway = HubServerGateway.instance;

		if (gateway != null) {
			var json = new JsonObject();
			json.add("player", entityToJson(player));
			gateway.send("player_logged_in", json);

			updateInfo(player.server, gateway);
		}
	}

	public static void playerLoggedOut(ServerPlayer player) {
		var gateway = HubServerGateway.instance;

		if (gateway != null) {
			var json = new JsonObject();
			json.add("player", entityToJson(player));
			gateway.send("player_logged_out", json);

			Thread.startVirtualThread(() -> player.server.execute(() -> updateInfo(player.server, gateway)));
		}
	}

	public static void playerChangedDimension(ServerPlayer player, ResourceKey<Level> fromDim, ResourceKey<Level> toDim) {
		var gateway = HubServerGateway.instance;

		if (gateway != null) {
			var json = new JsonObject();
			json.add("player", entityToJson(player));
			json.addProperty("from_dimension", fromDim.location().toString());
			json.addProperty("to_dimension", toDim.location().toString());
			gateway.send("player_changed_dimension", json);
		}
	}

	public static void updateName(String name) {
		var gateway = HubServerGateway.instance;

		if (gateway != null) {
			gateway.sendName(name);
		}
	}

	public static void updateStatus(String status) {
		var gateway = HubServerGateway.instance;

		if (gateway != null) {
			gateway.sendStatus(status);
		}
	}

	public static void registerBuiltIn(HubGatewayEventRegistry<MinecraftServer> registry) {
		registry.register("ping", HubServerGateway::ping);
		registry.registerSynced("request_restart", HubServerGateway::requestRestart);
		registry.registerSynced("run_command", HubServerGateway::runCommand);
		registry.registerSynced("update_ops", HubServerGateway::updateOps);
	}

	public static void ping(HubGatewayEvent event) {
		event.respond(new JsonPrimitive("pong"));
	}

	private static void requestRestart(MinecraftServer server, HubGatewayEvent event) {
		server.halt(false);
	}

	private static void runCommand(MinecraftServer server, HubGatewayEvent event) {
		var command = event.paramsObject().get("command").getAsString();
		server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
	}

	private static void updateOps(MinecraftServer server, HubGatewayEvent event) {
		HubServerSessionData.updateOps(server, event.paramsArray());
	}

	public final MinecraftServer server;

	public HubServerGateway(MinecraftServer server, URI uri) {
		super(server, uri);
		this.server = server;
	}

	@Override
	public void collectEventHandlers(HubGatewayEventRegistry<MinecraftServer> registry) {
		PlatformHelper.CURRENT.collectServerGatewayEventHandlers(registry);
	}

	@Override
	public void onConnected() {
		updateInfo(main, this);
	}
}
