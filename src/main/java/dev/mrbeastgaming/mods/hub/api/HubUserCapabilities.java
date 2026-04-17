package dev.mrbeastgaming.mods.hub.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;

public record HubUserCapabilities(
	boolean singleplayer,
	boolean multiplayer,
	boolean viewLocalReplays,
	boolean viewRemoteReplays,
	boolean autoUploadFiles,
	boolean uploadUserFiles
) {
	public static final boolean DEFAULT_ENABLE_ADMIN_BUTTONS = "true".equals(System.getenv("ENABLE_ADMIN_BUTTONS")) || PlatformHelper.CURRENT.isDevEnv();

	public static final HubUserCapabilities DEFAULT = new HubUserCapabilities(
		DEFAULT_ENABLE_ADMIN_BUTTONS,
		true,
		DEFAULT_ENABLE_ADMIN_BUTTONS,
		false,
		true,
		false
	);

	public static HubUserCapabilities CURRENT = DEFAULT;

	public static final Codec<HubUserCapabilities> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.optionalFieldOf("singleplayer", DEFAULT.singleplayer).forGetter(HubUserCapabilities::singleplayer),
		Codec.BOOL.optionalFieldOf("multiplayer", DEFAULT.multiplayer).forGetter(HubUserCapabilities::multiplayer),
		Codec.BOOL.optionalFieldOf("view_local_replays", DEFAULT.viewLocalReplays).forGetter(HubUserCapabilities::viewLocalReplays),
		Codec.BOOL.optionalFieldOf("view_remote_replays", DEFAULT.viewRemoteReplays).forGetter(HubUserCapabilities::viewRemoteReplays),
		Codec.BOOL.optionalFieldOf("auto_upload_files", DEFAULT.autoUploadFiles).forGetter(HubUserCapabilities::autoUploadFiles),
		Codec.BOOL.optionalFieldOf("upload_user_files", DEFAULT.uploadUserFiles).forGetter(HubUserCapabilities::uploadUserFiles)
	).apply(instance, HubUserCapabilities::new));
}
