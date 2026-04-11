package dev.mrbeastgaming.mods.hub.link;

import dev.latvian.apps.tinyserver.HTTPServer;
import dev.latvian.apps.tinyserver.http.HTTPRequest;
import dev.latvian.apps.tinyserver.http.response.HTTPResponse;
import dev.latvian.apps.tinyserver.http.response.error.client.BadRequestError;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.HubUserConfig;
import dev.mrbeastgaming.mods.hub.api.HubAPI;
import dev.mrbeastgaming.mods.hub.api.token.UserToken;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.net.http.HttpResponse;
import java.util.stream.IntStream;

public class LinkHubUserScreen extends Screen {
	public static void open(Minecraft mc) {
		mc.pushGuiLayer(new ConfirmScreen(value -> {
			mc.popGuiLayer();

			if (value) {
				mc.pushGuiLayer(new LinkHubUserScreen());
			}
		},
			Component.literal("Link MrBeast Gaming Hub Profile"),
			Component.literal("You only need to link your profile once"),
			Component.literal("Link").withStyle(ChatFormatting.GREEN),
			Component.literal("Skip").withStyle(ChatFormatting.RED)
		));
	}

	public static HTTPServer<HTTPRequest> webServer;

	public LinkHubUserScreen() {
		super(Component.literal("Link MrBeast Gaming Hub Profile"));
	}

	@Override
	protected void init() {
		if (webServer == null) {
			webServer = new HTTPServer<>(HTTPRequest::new);
			webServer.setPort(IntStream.range(8080, 8090));
			webServer.setDaemon(true);
			webServer.setAddress("127.0.0.1");
			webServer.get("/desktop/finish-link/{token}", this::getFinishLink);
			int port = webServer.start();
			Util.getPlatform().openUri(HubAPI.URI_BASE.resolve("/desktop/link/" + port));
		}

		super.init();
	}

	@Override
	public void removed() {
		super.removed();

		if (webServer != null) {
			webServer.stop();
			webServer = null;
		}
	}

	private HTTPResponse getFinishLink(HTTPRequest req) throws Exception {
		VidLib.LOGGER.info("Linking Hub Profile...");
		var token = req.variable("token").asString();

		var response = HubAPI.HTTP_CLIENT.send(HubAPI.apiUsersRequestToken(token), HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() / 100 == 2) {
			var userToken = UserToken.parse(response.body().trim());

			if (userToken == null) {
				throw new BadRequestError("Invalid response token, try again");
			}

			HubUserConfig.save(HubUserConfig.load().withToken(userToken));
			var name = "%s/%08X".formatted(userToken.header().name(), userToken.header().user());
			VidLib.LOGGER.info("Logged in as " + name);

			minecraft.execute(() -> {
				minecraft.popGuiLayer();
				minecraft.toast(Component.literal("Logged In"), Component.literal(name));
			});

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

		throw new BadRequestError("Error " + response.statusCode() + " occurred, try again later");
	}
}
