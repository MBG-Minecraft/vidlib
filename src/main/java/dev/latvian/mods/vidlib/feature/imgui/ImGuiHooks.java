package dev.latvian.mods.vidlib.feature.imgui;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import dev.latvian.mods.vidlib.VidLibClientEventHandler;
import dev.latvian.mods.vidlib.VidLibPaths;
import dev.latvian.mods.vidlib.feature.font.TTFFile;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.misc.FlashbackIntegration;
import dev.latvian.mods.vidlib.feature.platform.ClientGameEngine;
import imgui.ImFontConfig;
import imgui.ImFontGlyphRangesBuilder;
import imgui.ImGui;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.implot.ImPlot;
import imgui.extension.implot.ImPlotContext;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiDockNodeFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
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

	private static VLImGuiImplGlfw imGuiGlfw;
	private static ImGuiImplGl3 imGuiGl3;
	private static ImGuiContextStack context;
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

	public static void init(Minecraft mc, ResourceManager resourceManager) {
		imGuiGlfw = new VLImGuiImplGlfw(mc);
		imGuiGl3 = new ImGuiImplGl3();

		context = new ImGuiContextStack(
			new ImGuiContext(ImGui.createContext().ptr),
			new ImPlotContext(ImPlot.createContext().ptr)
		);

		var old = context.push();
		ImNodes.createContext();
		var io = ImGui.getIO();
		io.setIniFilename(VidLibPaths.USER.resolve("imgui.ini").toAbsolutePath().toString());
		io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
		io.addConfigFlags(ImGuiConfigFlags.DockingEnable);
		io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleFonts);
		io.addConfigFlags(ImGuiConfigFlags.DpiEnableScaleViewports);
		io.setConfigDockingWithShift(false);
		io.setConfigWindowsMoveFromTitleBarOnly(true);
		io.setConfigMacOSXBehaviors(Minecraft.ON_OSX);

		imGuiGl3.init("#version 410 core");
		imGuiGlfw.init(mc.getWindow().getWindow(), true);
		loadFonts(resourceManager);
		var style = ImGui.getStyle();
		ImGui.styleColorsDark(style);
		ImGraphics.setFullDefaultStyle(style);
		old.pop();
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
			fonts.addFontFromMemoryTTF(TTFFile.JETBRAINS_MONO_REGULAR.get().load(resourceManager), 20F, config);
		} catch (Exception e) {
			fonts.addFontDefault();
		}

		try {
			var bytes = TTFFile.MATERIAL_ICONS_ROUND_REGULAR.get().load(resourceManager);
			config.setMergeMode(true);
			config.setGlyphOffset(0, 3);
			fonts.addFontFromMemoryTTF(bytes, 22F, config, buildMaterialIconRanges());
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
			if (icon.icon != 0) {
				builder.addChar(icon.icon);
			}
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
		context.destroy();
		ImNodes.destroyContext();
	}

	public static void startFrame(Minecraft mc) {
		if (initialized == 0) {
			return;
		}

		if (initialized == 1) {
			init(mc, mc.getResourceManager());
			initialized = 2;
		}

		var old = context.push();

		ensureEndFrame();

		GlStateManager._disableColorLogicOp();

		var io = ImGui.getIO();

		if (io.getKeysDown(GLFW.GLFW_KEY_LEFT_CONTROL)) {
			io.setKeysDown(GLFW.GLFW_KEY_TAB, false);
		}

		imGuiGlfw.newFrame();
		ImGui.newFrame();
		active = true;

		/*
		if (captureMouse) {
			// If capturing the mouse, position it off-screen, so it doesn't show hover effects
			((VLMouseHandler) mc.mouseHandler).vl$resetMouse();
		}
		 */

		// If Minecraft is capturing input, there is no world where ImGui widgets should handle
		// any mouse interactions (hover, clicking...). Setting the mouse position to (-1, -1) allows us to
		// cancel incorrect hover interactions that will sometimes occur when the Minecraft cursor is locked
		// but is invisibly "hovering" over a floating/docked ImGui window.
		if (mc.mouseHandler.isMouseGrabbed()) {
			io.setMousePos(-1, -1);
		}

		var window = mc.getWindow();

		dockId = ImGui.dockSpaceOverViewport(ImGui.getMainViewport(), ImGuiDockNodeFlags.NoDockingInCentralNode | ImGuiDockNodeFlags.PassthruCentralNode);
		var centralNode = imgui.internal.ImGui.dockBuilderGetCentralNode(dockId);

		// Get the size and position of the central node
		var windowPos = ImGui.getMainViewport().getPos();
		var windowSize = ImGui.getMainViewport().getSize();
		var centralNodePos = centralNode.getPos();
		var centralNodeSize = centralNode.getSize();

		float h = FlashbackIntegration.isInReplay() ? 0F : 24F;
		boolean topInfoBar = h > 0F && ClientGameEngine.INSTANCE.hasTopInfoBar(mc);
		boolean bottomInfoBar = h > 0F && ClientGameEngine.INSTANCE.hasBottomInfoBar(mc);

		updateViewportArea(
			mc, window,
			(centralNodePos.x - windowPos.x) / (double) windowSize.x,
			(centralNodePos.y - windowPos.y + (topInfoBar ? h : 0D)) / (double) windowSize.y,
			centralNodeSize.x / (double) windowSize.x,
			(centralNodeSize.y - (topInfoBar ? h : 0D) - (bottomInfoBar ? h : 0D)) / (double) windowSize.y
		);

		if (topInfoBar || bottomInfoBar) {
			var graphics = new ImGraphics(mc);
			graphics.pushRootStack();
			graphics.setStyleCol(ImGuiCol.WindowBg, 0xFF000000);
			graphics.setStyleVar(ImGuiStyleVar.WindowRounding, 0F);
			graphics.setStyleVar(ImGuiStyleVar.WindowPadding, 2F, 2F);
			graphics.setStyleVar(ImGuiStyleVar.FramePadding, 2F, 0F);
			graphics.setStyleVar(ImGuiStyleVar.WindowMinSize, 0F, 0F);
			graphics.setStyleVar(ImGuiStyleVar.ItemSpacing, 2F, 0F);

			int flags = ImGuiWindowFlags.NoSavedSettings
				| ImGuiWindowFlags.NoMove
				| ImGuiWindowFlags.NoDocking
				| ImGuiWindowFlags.NoNav
				| ImGuiWindowFlags.NoDecoration;

			if (topInfoBar) {
				ImGui.setNextWindowPos(centralNodePos.x, centralNodePos.y);
				ImGui.setNextWindowSize(centralNodeSize.x, h);

				if (ImGui.begin("###top-info-bar", flags)) {
					ClientGameEngine.INSTANCE.topInfoBar(graphics, centralNodePos.x, centralNodePos.y, centralNodeSize.x, h);
				}

				ImGui.end();
			}

			if (bottomInfoBar) {
				ImGui.setNextWindowPos(centralNodePos.x, centralNodePos.y + centralNodeSize.y - h);
				ImGui.setNextWindowSize(centralNodeSize.x, h);

				if (ImGui.begin("###bottom-info-bar", flags)) {
					ClientGameEngine.INSTANCE.bottomInfoBar(graphics, centralNodePos.x, centralNodePos.y + centralNodeSize.y - h, centralNodeSize.x, h);
				}

				ImGui.end();
			}

			graphics.popStack();
		}

		old.pop();
	}

	private static void updateViewportArea(Minecraft mc, Window window, double xOffset, double yOffset, double xScale, double yScale) {
		var prevWidth = window.getWidth();
		var prevHeight = window.getHeight();
		window.vl$setViewportArea(xOffset, yOffset, xScale, yScale);

		if (window.getWidth() == 0 || window.getHeight() == 0) {
			return; // Window has been minimized...
		}

		if (window.getWidth() != prevWidth || window.getHeight() != prevHeight) {
			mc.resizeDisplay();
		}
	}

	public static void beforeEndFrame() {
		if (VidLibClientEventHandler.clientLoaded && !ImGuiAPI.getHide()) {
			var mc = Minecraft.getInstance();

			if (mc.level == null || !mc.level.isReplayLevel()) {
				var old = context.push();
				var graphics = new ImGraphics(mc);
				graphics.pushRootStack();
				BuiltInImGui.handle(graphics);
				graphics.popStack();
				old.pop();
			}
		}

		endingFrame = true;
	}

	public static void endFrame(Minecraft mc) {
		endingFrame = false;

		if (initialized == 0) {
			return;
		}

		var old = context.push();

		active = false;
		ImGui.render();

		var drawData = ImGui.getDrawData();

		if (drawData != null) {
			imGuiGl3.renderDrawData(drawData);
		}

		// captureMouse = ImGui.getIO().getWantCaptureMouse() && !mc.mouseHandler.isMouseGrabbed();
		// captureKeyboard = ImGui.getIO().getWantCaptureKeyboard();

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

		old.pop();
	}

	public static void ensureEndFrame() {
		if (!active) {
			return;
		}

		var old = context.push();
		ImGui.endFrame();
		ImGui.updatePlatformWindows();
		old.pop();
		active = false;
	}

	public static boolean shouldInterceptMouse() {
		var old = context.push();
		boolean value = ImGui.getIO().getWantCaptureMouse() && !Minecraft.getInstance().mouseHandler.isMouseGrabbed();
		old.pop();
		return value;
	}

	public static boolean shouldInterceptKeyboard() {
		var old = context.push();
		boolean value = ImGui.getIO().getWantCaptureKeyboard();
		old.pop();
		return value;
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
