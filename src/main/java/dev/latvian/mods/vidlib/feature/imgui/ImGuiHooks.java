package dev.latvian.mods.vidlib.feature.imgui;

import com.mojang.blaze3d.platform.Window;
import dev.latvian.mods.vidlib.VidLibClientEventHandler;
import dev.latvian.mods.vidlib.core.VLMouseHandler;
import dev.latvian.mods.vidlib.feature.font.TTFFile;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import imgui.ImFontConfig;
import imgui.ImFontGlyphRangesBuilder;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.internal.ImGuiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWWindowContentScaleCallback;

/**
 * Internal hooks for ImGui lifecycle management.
 */
@ApiStatus.Internal
public class ImGuiHooks {
	private static final short[] GLYPH_RANGES = {
		0x0020, 0x007F,   // Basic Latin
		0x00A0, 0x00FF,   // Latin-1 Supplement
		0x0100, 0x017F,   // Latin Extended-A
		0x2000, 0x206F,   // General Punctuation
		0x2190, 0x21FF,   // Arrows
		0x2300, 0x23FF,   // Miscellaneous Technical
		0x2460, 0x24FF,   // Enclosed Alphanumerics
		0x2500, 0x257F,   // Box Drawing
		0x2580, 0x259F,   // Block Elements
		0x25A0, 0x25FF,   // Geometric Shapes
		0x2600, 0x26FF,   // Miscellaneous Symbols
		0x2700, 0x27BF,   // Dingbats
		0,
	};

	private static final short[] MATERIAL_ICON_RANGES = {
		(short) '\ue000', (short) '\uf8ff', // Material Icons
		0,
	};

	private static ImGuiImplGlfw imGuiGlfw;
	private static ImGuiImplGl3 imGuiGl3;
	private static ImGuiContext imGuiContext;
	private static ImPlotContext imPlotContext;
	private static boolean captureMouse, captureKeyboard;
	private static boolean active = false;

	private static boolean endingFrame = false;
	public static boolean reloadFonts = false;

	static int dockId;
	static float dpiScale = 1.0f;

	public static int initialized = 2; // FIXME

	public static void enable() {
		if (initialized == 0) {
			initialized = 1;
		}
	}

	public static void init(ResourceManager resourceManager) {
		var client = Minecraft.getInstance();
		imGuiGlfw = new ImGuiImplGlfw();
		imGuiGl3 = new ImGuiImplGl3();
		imGuiContext = ImGui.createContext();
		imPlotContext = ImPlot.createContext();
		ImNodes.createContext();
		var io = ImGui.getIO();
		io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
		io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
		io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleFonts);
		io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleViewports);
		io.setConfigDockingWithShift(false);
		io.setConfigWindowsMoveFromTitleBarOnly(true);

		imGuiGlfw.init(client.getWindow().getWindow(), true);
		imGuiGl3.init("#version 150"); // MC uses 150 everywhere, so we can too

		loadFonts(resourceManager);
	}

	public static void loadFonts(ResourceManager resourceManager) {
		var fonts = ImGui.getIO().getFonts();
		fonts.clear();

		var config = new ImFontConfig();
		config.setGlyphRanges(GLYPH_RANGES);
		config.setOversampleH(2);
		config.setOversampleV(2);
		config.setGlyphOffset(0, 0);

		try {
			fonts.addFontFromMemoryTTF(TTFFile.JETBRAINS_MONO_REGULAR.get().load(resourceManager), 18, config);
		} catch (Exception e) {
			fonts.addFontDefault();
		}

		try {
			var bytes = TTFFile.MATERIAL_ICONS_ROUND_REGULAR.get().load(resourceManager);
			config.setMergeMode(true);
			config.setGlyphOffset(0, 3);
			fonts.addFontFromMemoryTTF(bytes, 20, config, buildMaterialIconRanges());
			config.setGlyphOffset(0, 0);
			config.setMergeMode(false);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		fonts.build();
		imGuiGl3.updateFontsTexture();

		config.destroy();
		fonts.clearTexData();
	}

	private static short[] buildMaterialIconRanges() {
		var builder = new ImFontGlyphRangesBuilder();

		for (var icon : ImIcons.VALUES) {
			builder.addChar(icon.icon);
		}

		for (var c : ImIcons.EXTRA_ICONS.get()) {
			builder.addChar(c.toChar());
		}

		return builder.buildRanges();
	}

	public static void trackDpiScale(Window window) {
		var previous = new GLFWWindowContentScaleCallback[1];
		previous[0] = GLFW.glfwSetWindowContentScaleCallback(window.getWindow(), (handle, xScale, yScale) -> {
			dpiScale = xScale;
			if (previous[0] != null) {
				previous[0].invoke(handle, xScale, yScale);
			}
		});
		var xScale = new float[1];
		GLFW.glfwGetWindowContentScale(window.getWindow(), xScale, null);
		dpiScale = xScale[0];
	}

	public static void dispose() {
		imGuiGl3.dispose();
		imGuiGlfw.dispose();
		ImPlot.destroyContext(imPlotContext);
		ImGui.destroyContext(imGuiContext);
		ImNodes.destroyContext();
	}

	public static void startFrame(Minecraft mc) {
		if (initialized == 0) {
			return;
		}

		if (initialized == 1) {
			init(mc.getResourceManager());
			initialized = 2;
		}

		ensureEndFrame();

		if (ImGui.getIO().getKeysDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			ImGui.getIO().setKeysDown(GLFW.GLFW_KEY_TAB, false);
		}

		imGuiGlfw.newFrame();
		ImGui.newFrame();
		active = true;

		if (captureMouse) {
			// If capturing the mouse, position it off-screen, so it doesn't show hover effects
			((VLMouseHandler) mc.mouseHandler).vl$resetMouse();
		}

		// If Minecraft is capturing input, there is no world where ImGui widgets should handle
		// any mouse interactions (hover, clicking...). Setting the mouse position to (-1, -1) allows us to
		// cancel incorrect hover interactions that will sometimes occur when the Minecraft cursor is locked
		// but is invisibly "hovering" over a floating/docked ImGui window.
		if (mc.mouseHandler.isMouseGrabbed()) {
			ImGui.getIO().setMousePos(-1, -1);
		}

		handleDocking();
	}

	private static void handleDocking() {
		var client = Minecraft.getInstance();
		var window = client.getWindow();

		dockId = -1;

		// Establish a docking space and retrieve the central node
		dockId = ImGui.dockSpaceOverViewport(ImGui.getMainViewport(), ImGuiDockNodeFlags.NoDockingInCentralNode | ImGuiDockNodeFlags.PassthruCentralNode);
		var centralNode = imgui.internal.ImGui.dockBuilderGetCentralNode(dockId);

		// Get the size and position of the central node
		var windowPos = ImGui.getMainViewport().getPos();
		var windowSize = ImGui.getMainViewport().getSize();
		var centralNodePos = centralNode.getPos();
		var centralNodeSize = centralNode.getSize();

		updateViewportArea(
			client, window,
			(centralNodePos.x - windowPos.x) / (double) windowSize.x,
			(centralNodePos.y - windowPos.y) / (double) windowSize.y,
			centralNodeSize.x / (double) windowSize.x,
			centralNodeSize.y / (double) windowSize.y
		);
	}

	private static void updateViewportArea(Minecraft client, Window window, double xOffset, double yOffset, double xScale, double yScale) {
		var prevWidth = window.getWidth();
		var prevHeight = window.getHeight();
		window.vl$setViewportArea(xOffset, yOffset, xScale, yScale);

		if (window.getWidth() == 0 || window.getHeight() == 0) {
			return; // Window has been minimized...
		}

		if (window.getWidth() != prevWidth || window.getHeight() != prevHeight) {
			client.resizeDisplay();
		}
	}

	public static void beforeEndFrame() {
		if (VidLibClientEventHandler.clientLoaded) {
			BuiltInImGui.handle(Minecraft.getInstance());
		}

		endingFrame = true;
	}

	public static void endFrame(Minecraft mc) {
		endingFrame = false;

		if (initialized == 0) {
			return;
		}

		active = false;
		ImGui.render();

		var drawData = ImGui.getDrawData();

		if (drawData != null) {
			imGuiGl3.renderDrawData(drawData);
		}

		captureMouse = ImGui.getIO().getWantCaptureMouse() && !mc.mouseHandler.isMouseGrabbed();
		captureKeyboard = ImGui.getIO().getWantCaptureKeyboard();

		if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
			GLFW.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, GLFW.GLFW_TRUE);
			var backupWindowPtr = GLFW.glfwGetCurrentContext();
			ImGui.updatePlatformWindows();
			ImGui.renderPlatformWindowsDefault();
			GLFW.glfwMakeContextCurrent(backupWindowPtr);
		}

		if (reloadFonts) {
			reloadFonts = false;
			loadFonts(mc.getResourceManager());
		}
	}

	public static void ensureEndFrame() {
		if (!active) {
			return;
		}

		ImGui.endFrame();
		ImGui.updatePlatformWindows();
		active = false;
	}

	public static boolean shouldInterceptMouse() {
		return captureMouse;
	}

	public static boolean shouldInterceptKeyboard() {
		return captureKeyboard;
	}

	public static int frameX(int original) {
		var window = Minecraft.getInstance().getWindow();
		return endingFrame ? (int) (window.vl$getXOffset() * window.vl$getUnscaledFramebufferWidth()) : original;
	}

	public static int frameY(int original) {
		var window = Minecraft.getInstance().getWindow();
		return endingFrame ? (int) (window.vl$getInverseYOffset() * window.vl$getUnscaledFramebufferHeight()) : original;
	}

	public static int frameW(int original) {
		var window = Minecraft.getInstance().getWindow();
		return endingFrame ? (int) (original + window.vl$getXOffset() * window.vl$getUnscaledFramebufferWidth()) : original;
	}

	public static int frameH(int original) {
		var window = Minecraft.getInstance().getWindow();
		return endingFrame ? (int) (original + window.vl$getInverseYOffset() * window.vl$getUnscaledFramebufferHeight()) : original;
	}
}
