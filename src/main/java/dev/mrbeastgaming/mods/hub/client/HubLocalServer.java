package dev.mrbeastgaming.mods.hub.client;

import dev.latvian.apps.tinyhttp.HTTPServer;
import dev.latvian.apps.tinyhttp.http.HTTPRequest;
import dev.latvian.apps.tinyhttp.http.response.HTTPResponse;
import dev.latvian.apps.tinyhttp.http.response.error.client.BadRequestError;
import dev.latvian.mods.klib.platform.PlatformHelper;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.VidLibClient;
import dev.mrbeastgaming.mods.hub.HubUserConfig;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.token.UserToken;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.net.http.HttpResponse;

public class HubLocalServer {
	public static HTTPServer<HTTPRequest> webServer;

	public static int getWebServer() {
		if (webServer == null) {
			webServer = new HTTPServer<>(HTTPRequest::new);
			webServer.setDynamicPort();
			webServer.setDaemon(true);
			webServer.setAddress("127.0.0.1");
			webServer.get("/desktop/finish-link/{token}", HubLocalServer::getFinishLink);
			webServer.start();
		}

		return webServer.getBoundPort();
	}

	private static HTTPResponse getFinishLink(HTTPRequest req) throws Exception {
		VidLib.LOGGER.info("Linking Hub Profile...");
		var token = req.variable("token").asString();

		var response = HubAPI.HTTP_CLIENT.send(HubAPI.apiUsersRequestToken(token), HttpResponse.BodyHandlers.ofString());

		int statusCode = response.statusCode();
		var error = "";

		var mc = Minecraft.getInstance();

		if (statusCode / 100 == 2) {
			var userToken = UserToken.parse(response.body().trim());

			if (userToken == null) {
				throw new BadRequestError("Invalid response token, try again");
			}

			HubUserConfig.save(HubUserConfig.load().withToken(userToken));
			var name = "%s#%08X".formatted(userToken.header().name(), userToken.header().user());
			VidLib.LOGGER.info("Logged in as " + name);
			VidLibClient.loadHub();

			mc.execute(() -> {
				if (mc.screen instanceof LinkHubUserWaitingScreen) {
					mc.popGuiLayer();
				}

				mc.toast(Component.literal("Logged In"), Component.literal(name));
				GLFW.glfwFocusWindow(mc.getWindow().handle());
				VidLibClient.checkFileSync(false);

				if (!PlatformHelper.CURRENT.isDevEnv()) {
					LinkMinecraftScreen.handle(mc, true);
				}
			});
		} else {
			mc.execute(() -> {
				if (mc.screen instanceof LinkHubUserWaitingScreen link) {
					link.button.active = true;
					link.setMessage(Component.literal(("Error " + statusCode + "\n" + error).trim()));
				}

				GLFW.glfwFocusWindow(mc.getWindow().handle());
			});
		}

		return HTTPResponse.ok().html("""
			<!DOCTYPE html>
			<html lang="en">
			<head>
			  <meta charset="utf-8" />
			  <meta http-equiv="X-UA-Compatible" content="IE=edge" />
			  <meta name="viewport" content="width=device-width, initial-scale=1" />
			  <title>MrBeast Gaming Hub Link</title>
			  <meta name="theme-color" content="#262728" />
			  <link rel="stylesheet" href="https://mrbeastgaming.dev/style.css" />
			</head>
			<body>
			  <div class="content">
			    <h1><a href="/">MrBeast Gaming Hub</a></h1>
			    <p>You may close this page now.</p>
			  </div>
			  <script>window.close();</script>
			</body>
			</html>
			""");
	}
}
