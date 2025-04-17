package dev.beast.mods.shimmer;

public class ShimmerConfig {
	public static int cycleShadersKey = 83; // GLFW.GLFW_KEY_S;
	public static int reloadShadersKey = 88; // GLFW.GLFW_KEY_X;
	public static boolean betterDefaultGameRules = true;
	public static boolean loadVanillaStructures = false;
	public static boolean disableFallingBlocks = true;
	public static boolean disableCoralBlocks = true;
	public static boolean limitHeldItemRendering = true;
	public static double heldItemRenderDistance = 64D;
	public static boolean limitClothingRendering = true;
	public static double clothingRenderDistance = 64D;
	public static boolean hideWaterParticles = false;
	public static boolean fetchOfflinePlayerData = true;
	public static int structureBlockRange = 200;
	public static boolean renderSuspendedOverlay = false;
	public static boolean debugS2CPackets = System.getenv().getOrDefault("SHIMMER_DEBUG_S2C_PACKETS", "0").equals("1");
}
