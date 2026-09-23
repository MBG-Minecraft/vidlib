package dev.mrbeastgaming.mods.hub.api;

import dev.latvian.mods.klib.io.checksum.SHA1;
import dev.latvian.mods.vidlib.VidLib;
import dev.mrbeastgaming.mods.hub.HubProjectConfig;
import dev.mrbeastgaming.mods.hub.HubUserConfig;
import dev.mrbeastgaming.mods.hub.api.data.HubGameServer;
import dev.mrbeastgaming.mods.hub.api.data.HubKeys;
import dev.mrbeastgaming.mods.hub.api.data.HubMinecraftProfile;
import dev.mrbeastgaming.mods.hub.api.data.HubParticipant;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import dev.mrbeastgaming.mods.hub.api.gateway.HubClientGateway;
import dev.mrbeastgaming.mods.hub.file.UploadContext;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class HubClientSession {
	public UUID id = Util.NIL_UUID;
	public HubUser user = null;
	public HubProject project = null;
	public HubUserCapabilities capabilities = HubUserCapabilities.DEFAULT;
	public HubParticipant participant = null;
	public HubMinecraftProfile.LinkData minecraftLink = null;
	public HubKeys keys = null;
	public HubKeys sessionKeys = null;
	public byte[] sessionSalt = new byte[0];
	public String authServerId = "";
	public List<HubGameServer> servers = List.of();
	public UploadContext uploadContext = null;

	public static HubClientSession CURRENT = new HubClientSession();

	public static void load(Minecraft mc, @Nullable HubUserConfig userConfig, @Nullable HubProjectConfig projectConfig) {
		boolean hasAuth = userConfig != null && !userConfig.token().isEmpty();

		VidLib.LOGGER.info("Loading Hub client session data...");
		var session = new HubClientSession();
		var oldMinecraftLink = CURRENT.minecraftLink;

		try {
			var token = projectConfig == null ? "" : projectConfig.token();
			var data = HubAPI.MinecraftAPI.postClientSession(new HubClientSessionRequest(token, true));

			if (hasAuth && data.sessionSalt().length > 0) {
				try (var out = new ByteArrayOutputStream()) {
					out.write(data.sessionSalt());
					out.write(userConfig.token().getBytes(StandardCharsets.ISO_8859_1));
					session.authServerId = SHA1.TYPE.digest(out.toByteArray()).toString();
				}
			}

			session.id = data.id();
			session.user = data.ctx().user(data.user());
			session.project = data.ctx().project(data.project());
			session.capabilities = data.capabilities();
			session.participant = data.participant().orElse(null);
			session.minecraftLink = oldMinecraftLink;
			session.keys = data.keys().orElse(null);
			session.sessionKeys = data.sessionKeys().orElse(null);
			session.sessionSalt = data.sessionSalt();
			session.servers = data.servers();
			session.uploadContext = token.isEmpty() || session.project == null ? null : new UploadContext(projectConfig.token(), session.project);

			CURRENT = session;

			var userName = session.user == null ? "Public User" : session.user.toString();

			if (session.project != null) {
				VidLib.LOGGER.info("Client logged in '" + session.project.toString() + "' as '" + userName + "'");
			} else {
				VidLib.LOGGER.warn("Client logged in a misconfigured project as '" + userName + "'");
			}

			var gateway = HubClientGateway.startGateway(mc, data.gateway().orElse(null), data.gatewayToken().orElse(""));

			if (gateway != null) {
				gateway.updateInfo();
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to load Hub client session data", ex);
		}

		HubProjectsResponse.ALL.forget();
	}
}
