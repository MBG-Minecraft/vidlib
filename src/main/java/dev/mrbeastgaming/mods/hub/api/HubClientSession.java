package dev.mrbeastgaming.mods.hub.api;

import dev.latvian.mods.klib.io.IOConsumer;
import dev.latvian.mods.klib.io.checksum.SHA1;
import dev.latvian.mods.vidlib.VidLib;
import dev.latvian.mods.vidlib.feature.progressqueue.ProgressQueue;
import dev.mrbeastgaming.mods.hub.HubConfig;
import dev.mrbeastgaming.mods.hub.api.data.HubCountryList;
import dev.mrbeastgaming.mods.hub.api.data.HubGameServer;
import dev.mrbeastgaming.mods.hub.api.data.HubMinecraftProfile;
import dev.mrbeastgaming.mods.hub.api.data.HubParticipant;
import dev.mrbeastgaming.mods.hub.api.data.HubUserCapabilities;
import dev.mrbeastgaming.mods.hub.api.gateway.HubClientGateway;
import dev.mrbeastgaming.mods.hub.file.HubFileUploads;
import dev.mrbeastgaming.mods.hub.file.HubUploadContext;
import dev.mrbeastgaming.mods.hub.file.HubUploadResponseItem;
import net.minecraft.client.Minecraft;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class HubClientSession extends HubCommonSession<HubClientGateway> {
	public static final ProgressQueue CHECK_PROGRESS_QUEUE = ProgressQueue.createBlocking("Checking Files...");
	public static final ProgressQueue UPLOAD_PROGRESS_QUEUE = ProgressQueue.createBlocking("Uploading Files...");
	public static HubClientSession CURRENT = new HubClientSession();

	public HubUserCapabilities capabilities = HubUserCapabilities.DEFAULT;
	public HubParticipant participant = null;
	public HubMinecraftProfile.LinkData minecraftLink = null;
	public String authServerId = "";
	public List<HubGameServer> servers = List.of();

	public static void load(Minecraft mc) {
		var userToken = HubConfig.userToken;
		var projectToken = HubConfig.projectToken;

		VidLib.LOGGER.info("Loading Hub client session data...");

		var session = new HubClientSession();
		var oldMinecraftLink = CURRENT.minecraftLink;

		try {
			var data = HubAPI.MinecraftAPI.postClientSession(new HubClientSessionRequest(projectToken, true, HubConfig.identity));

			session.id = data.id();
			session.gatewayInfo = data.gatewayInfo().orElse(null);
			session.user = data.user().orElse(null);
			session.project = data.project().orElse(null);
			session.capabilities = data.capabilities();
			session.participant = data.participant().orElse(null);
			session.minecraftLink = oldMinecraftLink;
			session.keys = data.keys();
			session.sessionKeys = data.sessionKeys();
			session.sessionSalt = data.sessionSalt();
			session.countries = HubCountryList.of(data.ctx().relevantCountries().values());
			session.country = data.country();
			session.servers = data.servers();

			if (!userToken.isEmpty() && session.sessionSalt.length > 0) {
				try (var out = new ByteArrayOutputStream()) {
					out.write(session.sessionSalt);
					out.write(userToken.getBytes(StandardCharsets.ISO_8859_1));
					session.authServerId = SHA1.TYPE.digest(out.toByteArray()).toString();
				}
			}

			session.uploadContext = projectToken.isEmpty() || session.project == null ? null : new HubUploadContext(
				projectToken,
				session.project,
				CHECK_PROGRESS_QUEUE,
				UPLOAD_PROGRESS_QUEUE,
				mc.getUser().getProfileId()
			);

			CURRENT = session;

			var userName = session.user == null ? "Public User" : session.user.toString();

			if (session.project != null) {
				VidLib.LOGGER.info("Client logged in '" + session.project.toString() + "' as '" + userName + "'");
			} else {
				VidLib.LOGGER.warn("Client logged in a misconfigured project as '" + userName + "'");
			}

			var gateway = HubClientGateway.startGateway(mc, session.gatewayInfo);

			if (gateway != null) {
				gateway.updateInfo();
			}
		} catch (Exception ex) {
			VidLib.LOGGER.error("Failed to load Hub client session data", ex);
		}

		HubProjectsResponse.ALL.forget();
	}

	@Override
	public List<HubUploadResponseItem> uploadBlocking(String intent, IOConsumer<HubFileUploads> callback) {
		var ctx = uploadContext;
		return ctx == null ? List.of() : ctx.uploadBlocking(project, gatewayInfo, gateway, capabilities.parallelUploads(), intent, callback);
	}
}
