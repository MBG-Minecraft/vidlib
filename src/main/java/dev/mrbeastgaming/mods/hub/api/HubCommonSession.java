package dev.mrbeastgaming.mods.hub.api;

import dev.mrbeastgaming.mods.hub.api.data.HubKeys;
import dev.mrbeastgaming.mods.hub.api.data.HubProject;
import dev.mrbeastgaming.mods.hub.api.data.HubUser;
import dev.mrbeastgaming.mods.hub.file.UploadContext;
import net.minecraft.Util;

import java.util.UUID;

public class HubCommonSession {
	public UUID id = Util.NIL_UUID;
	public HubUser user = null;
	public HubProject project = null;
	public HubKeys keys = null;
	public HubKeys sessionKeys = null;
	public byte[] sessionSalt = new byte[0];
	public UploadContext uploadContext = null;
}
