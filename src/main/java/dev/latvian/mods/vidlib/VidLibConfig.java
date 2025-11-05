package dev.latvian.mods.vidlib;

import dev.latvian.mods.vidlib.util.LevelOfDetailValue;

public class VidLibConfig {
	public static int cycleShadersKey = 83; // GLFW.GLFW_KEY_S;
	public static int reloadShadersKey = 88; // GLFW.GLFW_KEY_X;
	public static boolean betterDefaultGameRules = true;
	public static boolean disableCoralBlocks = true;
	public static final LevelOfDetailValue playerArmorLevelOfDetail = new LevelOfDetailValue(false, 128D);
	public static final LevelOfDetailValue heldItemLevelOfDetail = new LevelOfDetailValue(false, 64D);
	public static final LevelOfDetailValue clothingLevelOfDetail = new LevelOfDetailValue(false, 96D);
	public static final LevelOfDetailValue entityDetailsLevelOfDetail = new LevelOfDetailValue(false, 96D);
	public static final LevelOfDetailValue entityArmorLevelOfDetail = new LevelOfDetailValue(false, 128D);
	public static boolean hideWaterParticles = false;
	public static boolean fetchOfflinePlayerData = true;
	public static int structureBlockRange = 200;
	public static boolean renderSuspendedOverlay = false;
	public static boolean debugS2CPackets = System.getenv().getOrDefault("VIDLIB_DEBUG_S2C_PACKETS", "0").equals("1");
	public static boolean entityOutlineDepth = true;
	public static boolean infiniteArrows = true;
	public static boolean robert = false;
	public static int clientRenderDistance = 40;
	public static int serverRenderDistance = 32;
	public static boolean fastArrowDespawn = true;
	public static boolean arrowTrails = true;
	public static boolean strongEntityOutline = false;
	public static boolean endBatchesBeforeOutline = false;
	public static boolean legacyDataKeyStream = false;
	public static boolean logClientModList = false;
	public static boolean recordVoicechat = false;
}
