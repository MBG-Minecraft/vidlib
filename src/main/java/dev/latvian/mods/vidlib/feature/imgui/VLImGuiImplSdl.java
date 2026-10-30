package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.core.VLWindow;
import dev.latvian.mods.vidlib.feature.client.SDL3Compat;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLKeycode;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLVideo;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * SDL3 platform backend for ImGui.
 * <p>
 * 1.21.5 (and every earlier MC) used GLFW, so ImGui input came from
 * {@code imgui.glfw.ImGuiImplGlfw}. MC 26.3 replaced GLFW with SDL3, and the bundled imgui jar
 * only ships the GLFW backend - which cannot even be loaded on 26.3 (its class references
 * {@code org.lwjgl.glfw.GLFWErrorCallbackI}, and the {@code org.lwjgl.glfw} module is gone).
 * This class provides the same role on top of SDL3, polling the window/keyboard/mouse state
 * directly and feeding ImGui's {@link ImGuiIO}.
 */
public class VLImGuiImplSdl {
	private static final int NUM_SCANCODES = 256;

	private final Minecraft mc;
	private final IntBuffer windowPosX = org.lwjgl.BufferUtils.createIntBuffer(1);
	private final IntBuffer windowPosY = org.lwjgl.BufferUtils.createIntBuffer(1);
	private final FloatBuffer mouseX = org.lwjgl.BufferUtils.createFloatBuffer(1);
	private final FloatBuffer mouseY = org.lwjgl.BufferUtils.createFloatBuffer(1);

	private long windowId;
	private long lastFrameTime = System.nanoTime();
	private boolean wasKeyDown;
	private final boolean[] prevKeys = new boolean[NUM_SCANCODES];

	public VLImGuiImplSdl(Minecraft mc) {
		this.mc = mc;
	}

	public boolean init(long windowId, boolean installCallbacks) {
		this.windowId = windowId;

		var io = ImGui.getIO();

		io.setSetClipboardTextFn(new ImStrConsumer() {
			@Override
			public void accept(String text) {
				if (mc.getWindow().handle() == windowId) {
					mc.keyboardHandler.setClipboard(text);
				} else {
					SDL3Compat.setClipboardString(windowId, text);
				}
			}
		});

		io.setGetClipboardTextFn(new ImStrSupplier() {
			@Override
			public String get() {
				if (mc.getWindow().handle() == windowId) {
					return mc.keyboardHandler.getClipboard();
				}

				var clipboardString = SDL3Compat.getClipboardString(windowId);
				return clipboardString != null ? clipboardString : "";
			}
		});

		return true;
	}

	/**
	 * Feeds ImGui a full frame of input state. Mirrors what {@code ImGuiImplGlfw.newFrame()}
	 * did on 1.21.5 (display size, framebuffer scale, delta time, mouse and keyboard), but
	 * sourced from SDL3 instead of GLFW callbacks.
	 */
	public void newFrame() {
		var io = ImGui.getIO();

		var window = mc.getWindow();
		var vlWindow = (VLWindow) window;
		// Use the unscaled window dimensions: vidlib shrinks Window#getWidth/Height to the
		// ImGui viewport area, and reading those back here would shrink ImGui every frame.
		int screenWidth = vlWindow.vl$getUnscaledWidth();
		int screenHeight = vlWindow.vl$getUnscaledHeight();
		int framebufferWidth = vlWindow.vl$getUnscaledFramebufferWidth();
		int framebufferHeight = vlWindow.vl$getUnscaledFramebufferHeight();

		io.setDisplaySize(screenWidth, screenHeight);
		io.setDisplayFramebufferScale(framebufferWidth / (float) screenWidth, framebufferHeight / (float) screenHeight);

		long now = System.nanoTime();
		io.setDeltaTime((now - lastFrameTime) / 1_000_000_000F);
		lastFrameTime = now;

		// --- keyboard: SDL scancodes map directly onto ImGui's key indices ---
		ByteBuffer keys = SDLKeyboard.SDL_GetKeyboardState();
		short mods = SDLKeyboard.SDL_GetModState();
		int ctrl = SDLKeycode.SDL_KMOD_LCTRL | SDLKeycode.SDL_KMOD_RCTRL;
		int shift = SDLKeycode.SDL_KMOD_LSHIFT | SDLKeycode.SDL_KMOD_RSHIFT;
		int alt = SDLKeycode.SDL_KMOD_LALT | SDLKeycode.SDL_KMOD_RALT;
		int gui = SDLKeycode.SDL_KMOD_LGUI | SDLKeycode.SDL_KMOD_RGUI;
		io.setKeyCtrl((mods & ctrl) != 0);
		io.setKeyShift((mods & shift) != 0);
		io.setKeyAlt((mods & alt) != 0);
		io.setKeySuper((mods & gui) != 0);

		if (keys != null) {
			for (int scancode = 0; scancode < NUM_SCANCODES; scancode++) {
				boolean down = keys.get(scancode) != 0;
				if (down || prevKeys[scancode]) {
					io.setKeysDown(scancode, down);
				}
				prevKeys[scancode] = down;
			}
		}

		// --- mouse: global position translated into window space ---
		if (!mc.mouseHandler.isMouseGrabbed()) {
			windowPosX.rewind();
			windowPosY.rewind();
			SDLVideo.SDL_GetWindowPosition(windowId, windowPosX, windowPosY);

			mouseX.rewind();
			mouseY.rewind();
			SDLMouse.SDL_GetGlobalMouseState(mouseX, mouseY);

			io.setMousePos(
				mouseX.get(0) - windowPosX.get(0),
				mouseY.get(0) - windowPosY.get(0)
			);
		} else {
			io.setMousePos(-1F, -1F);
		}

		int buttonMask = SDLMouse.SDL_GetGlobalMouseState(null, null);
		// SDL's bitmask for a button is 1 << (button - 1); SDL_BUTTON_MASK is private in LWJGL.
		io.setMouseDown(0, (buttonMask & (1 << (SDLMouse.SDL_BUTTON_LEFT - 1))) != 0);
		io.setMouseDown(1, (buttonMask & (1 << (SDLMouse.SDL_BUTTON_RIGHT - 1))) != 0);
		io.setMouseDown(2, (buttonMask & (1 << (SDLMouse.SDL_BUTTON_MIDDLE - 1))) != 0);
	}

	public void dispose() {
	}
}
