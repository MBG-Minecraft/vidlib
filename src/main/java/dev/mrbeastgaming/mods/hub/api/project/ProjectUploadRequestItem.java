package dev.mrbeastgaming.mods.hub.api.project;

import dev.latvian.mods.klib.util.Hex32;
import dev.latvian.mods.klib.util.MD5;
import dev.mrbeastgaming.mods.hub.api.HubFileType;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record ProjectUploadRequestItem(
	MD5 uniqueId,
	MD5 checksum,
	long size,
	String name,
	HubFileType type,
	@Nullable Instant created,
	Hex32 assignedTo,
	@Nullable UUID assignedToMinecraft
) {
}
