package dev.mrbeastgaming.mods.hub.api.data;

import com.mojang.serialization.Codec;
import dev.latvian.mods.klib.util.Hex64;
import dev.latvian.mods.vidlib.feature.platform.PlatformHelper;
import dev.mrbeastgaming.mods.hub.api.HubClientSession;

public record HubUserCapabilities(
	Hex64 capabilities,
	boolean singleplayer,
	boolean multiplayer,
	boolean adminPanel,
	boolean viewLocalReplays,
	boolean viewRemoteReplays,
	boolean viewDrive,
	boolean viewRemoteWorlds,
	boolean autoUploadFiles,
	boolean uploadUserFiles,
	boolean requireLink,
	boolean parallelUploads
) {
	public static final long SINGLEPLAYER = 1L << 0L;
	public static final long MULTIPLAYER = 1L << 1L;
	public static final long ADMIN_PANEL = 1L << 2L;
	public static final long VIEW_LOCAL_REPLAYS = 1L << 3L;
	public static final long VIEW_REMOTE_REPLAYS = 1L << 4L;
	public static final long VIEW_DRIVE = 1L << 5L;
	public static final long VIEW_REMOTE_WORLDS = 1L << 6L;
	public static final long AUTO_UPLOAD_FILES = 1L << 7L;
	public static final long UPLOAD_USER_FILES = 1L << 8L;
	public static final long REQUIRE_LINK = 1L << 9L;
	public static final long PARALLEL_UPLOADS = 1L << 10L;

	public static final boolean DEFAULT_ENABLE_ADMIN_BUTTONS = "true".equals(System.getenv("ENABLE_ADMIN_BUTTONS")) || PlatformHelper.CURRENT.isDevEnv();

	public static final HubUserCapabilities DEFAULT = new HubUserCapabilities(
		Hex64.NONE,
		DEFAULT_ENABLE_ADMIN_BUTTONS,
		true,
		DEFAULT_ENABLE_ADMIN_BUTTONS,
		DEFAULT_ENABLE_ADMIN_BUTTONS,
		DEFAULT_ENABLE_ADMIN_BUTTONS,
		DEFAULT_ENABLE_ADMIN_BUTTONS,
		false,
		true,
		false,
		!DEFAULT_ENABLE_ADMIN_BUTTONS,
		false
	);

	public static HubUserCapabilities get() {
		return HubClientSession.CURRENT.capabilities;
	}

	public static final Codec<HubUserCapabilities> CODEC = Hex64.LENIENT_CODEC.xmap(HubUserCapabilities::new, HubUserCapabilities::capabilities);

	public HubUserCapabilities(Hex64 capabilities) {
		this(
			capabilities,
			capabilities.is(SINGLEPLAYER),
			capabilities.is(MULTIPLAYER),
			capabilities.is(ADMIN_PANEL),
			capabilities.is(VIEW_LOCAL_REPLAYS),
			capabilities.is(VIEW_REMOTE_REPLAYS),
			capabilities.is(VIEW_DRIVE),
			capabilities.is(VIEW_REMOTE_WORLDS),
			capabilities.is(AUTO_UPLOAD_FILES),
			capabilities.is(UPLOAD_USER_FILES),
			capabilities.is(REQUIRE_LINK),
			capabilities.is(PARALLEL_UPLOADS)
		);
	}

	public boolean resolveRequireLink() {
		return !DEFAULT_ENABLE_ADMIN_BUTTONS && requireLink;
	}
}
