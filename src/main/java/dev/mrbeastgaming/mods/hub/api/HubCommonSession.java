package dev.mrbeastgaming.mods.hub.api;

import dev.latvian.mods.klib.io.IOConsumer;
import dev.latvian.mods.klib.util.Async;
import dev.mrbeastgaming.mods.hub.api.data.HubCountry;
import dev.mrbeastgaming.mods.hub.api.data.HubCountryList;
import dev.mrbeastgaming.mods.hub.api.data.HubGatewayInfo;
import dev.mrbeastgaming.mods.hub.api.data.HubKeys;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.api.gateway.HubCommonGateway;
import dev.mrbeastgaming.mods.hub.file.HubFileUploads;
import dev.mrbeastgaming.mods.hub.file.HubUploadContext;
import dev.mrbeastgaming.mods.hub.file.HubUploadResponseItem;
import net.minecraft.Util;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HubCommonSession<G extends HubCommonGateway<?>> {
	public UUID id = Util.NIL_UUID;
	public HubGatewayInfo gatewayInfo = null;
	public HubUser user = null;
	public HubProject project = null;
	public HubKeys keys = HubKeys.NONE;
	public HubKeys sessionKeys = HubKeys.NONE;
	public byte[] sessionSalt = new byte[0];
	public HubCountryList countries = HubCountryList.EMPTY;
	public HubCountry country = HubCountry.UNKNOWN;

	public G gateway = null;
	public HubUploadContext uploadContext = null;

	public CompletableFuture<List<HubUploadResponseItem>> upload(String intent, IOConsumer<HubFileUploads> callback) {
		return CompletableFuture.supplyAsync(() -> uploadBlocking(intent, callback), Async.EXECUTOR);
	}

	public List<HubUploadResponseItem> uploadBlocking(String intent, IOConsumer<HubFileUploads> callback) {
		var ctx = uploadContext;
		return ctx == null ? List.of() : ctx.uploadBlocking(project, gatewayInfo, gateway, 5, intent, callback);
	}
}
