package dev.latvian.mods.vidlib.feature.imgui;

import dev.latvian.mods.vidlib.core.api.client.VLWindow;
import dev.latvian.mods.vidlib.feature.client.SDL3Compat;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.flag.ImGuiKey;
import net.minecraft.client.Minecraft;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLKeycode;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLScancode;
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
					int imguiKey = sdlScancodeToImguiKey(scancode);

					if (imguiKey != ImGuiKey.None) {
						io.addKeyEvent(imguiKey, down);
					}
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

	public static int sdlScancodeToImguiKey(int sdlScancode) {
		return switch (sdlScancode) {
			case SDLScancode.SDL_SCANCODE_TAB -> ImGuiKey.Tab;
			case SDLScancode.SDL_SCANCODE_LEFT -> ImGuiKey.LeftArrow;
			case SDLScancode.SDL_SCANCODE_RIGHT -> ImGuiKey.RightArrow;
			case SDLScancode.SDL_SCANCODE_UP -> ImGuiKey.UpArrow;
			case SDLScancode.SDL_SCANCODE_DOWN -> ImGuiKey.DownArrow;
			case SDLScancode.SDL_SCANCODE_PAGEUP -> ImGuiKey.PageUp;
			case SDLScancode.SDL_SCANCODE_PAGEDOWN -> ImGuiKey.PageDown;
			case SDLScancode.SDL_SCANCODE_HOME -> ImGuiKey.Home;
			case SDLScancode.SDL_SCANCODE_END -> ImGuiKey.End;
			case SDLScancode.SDL_SCANCODE_INSERT -> ImGuiKey.Insert;
			case SDLScancode.SDL_SCANCODE_DELETE -> ImGuiKey.Delete;
			case SDLScancode.SDL_SCANCODE_BACKSPACE -> ImGuiKey.Backspace;
			case SDLScancode.SDL_SCANCODE_SPACE -> ImGuiKey.Space;
			case SDLScancode.SDL_SCANCODE_RETURN -> ImGuiKey.Enter;
			case SDLScancode.SDL_SCANCODE_ESCAPE -> ImGuiKey.Escape;
			case SDLScancode.SDL_SCANCODE_LCTRL -> ImGuiKey.LeftCtrl;
			case SDLScancode.SDL_SCANCODE_LSHIFT -> ImGuiKey.LeftShift;
			case SDLScancode.SDL_SCANCODE_LALT -> ImGuiKey.LeftAlt;
			case SDLScancode.SDL_SCANCODE_LGUI -> ImGuiKey.LeftSuper;
			case SDLScancode.SDL_SCANCODE_RCTRL -> ImGuiKey.RightCtrl;
			case SDLScancode.SDL_SCANCODE_RSHIFT -> ImGuiKey.RightShift;
			case SDLScancode.SDL_SCANCODE_RALT -> ImGuiKey.RightAlt;
			case SDLScancode.SDL_SCANCODE_RGUI -> ImGuiKey.RightSuper;
			case SDLScancode.SDL_SCANCODE_APPLICATION -> ImGuiKey.Menu;
			case SDLScancode.SDL_SCANCODE_0 -> ImGuiKey._0;
			case SDLScancode.SDL_SCANCODE_1 -> ImGuiKey._1;
			case SDLScancode.SDL_SCANCODE_2 -> ImGuiKey._2;
			case SDLScancode.SDL_SCANCODE_3 -> ImGuiKey._3;
			case SDLScancode.SDL_SCANCODE_4 -> ImGuiKey._4;
			case SDLScancode.SDL_SCANCODE_5 -> ImGuiKey._5;
			case SDLScancode.SDL_SCANCODE_6 -> ImGuiKey._6;
			case SDLScancode.SDL_SCANCODE_7 -> ImGuiKey._7;
			case SDLScancode.SDL_SCANCODE_8 -> ImGuiKey._8;
			case SDLScancode.SDL_SCANCODE_9 -> ImGuiKey._9;
			case SDLScancode.SDL_SCANCODE_A -> ImGuiKey.A;
			case SDLScancode.SDL_SCANCODE_B -> ImGuiKey.B;
			case SDLScancode.SDL_SCANCODE_C -> ImGuiKey.C;
			case SDLScancode.SDL_SCANCODE_D -> ImGuiKey.D;
			case SDLScancode.SDL_SCANCODE_E -> ImGuiKey.E;
			case SDLScancode.SDL_SCANCODE_F -> ImGuiKey.F;
			case SDLScancode.SDL_SCANCODE_G -> ImGuiKey.G;
			case SDLScancode.SDL_SCANCODE_H -> ImGuiKey.H;
			case SDLScancode.SDL_SCANCODE_I -> ImGuiKey.I;
			case SDLScancode.SDL_SCANCODE_J -> ImGuiKey.J;
			case SDLScancode.SDL_SCANCODE_K -> ImGuiKey.K;
			case SDLScancode.SDL_SCANCODE_L -> ImGuiKey.L;
			case SDLScancode.SDL_SCANCODE_M -> ImGuiKey.M;
			case SDLScancode.SDL_SCANCODE_N -> ImGuiKey.N;
			case SDLScancode.SDL_SCANCODE_O -> ImGuiKey.O;
			case SDLScancode.SDL_SCANCODE_P -> ImGuiKey.P;
			case SDLScancode.SDL_SCANCODE_Q -> ImGuiKey.Q;
			case SDLScancode.SDL_SCANCODE_R -> ImGuiKey.R;
			case SDLScancode.SDL_SCANCODE_S -> ImGuiKey.S;
			case SDLScancode.SDL_SCANCODE_T -> ImGuiKey.T;
			case SDLScancode.SDL_SCANCODE_U -> ImGuiKey.U;
			case SDLScancode.SDL_SCANCODE_V -> ImGuiKey.V;
			case SDLScancode.SDL_SCANCODE_W -> ImGuiKey.W;
			case SDLScancode.SDL_SCANCODE_X -> ImGuiKey.X;
			case SDLScancode.SDL_SCANCODE_Y -> ImGuiKey.Y;
			case SDLScancode.SDL_SCANCODE_Z -> ImGuiKey.Z;
			case SDLScancode.SDL_SCANCODE_F1 -> ImGuiKey.F1;
			case SDLScancode.SDL_SCANCODE_F2 -> ImGuiKey.F2;
			case SDLScancode.SDL_SCANCODE_F3 -> ImGuiKey.F3;
			case SDLScancode.SDL_SCANCODE_F4 -> ImGuiKey.F4;
			case SDLScancode.SDL_SCANCODE_F5 -> ImGuiKey.F5;
			case SDLScancode.SDL_SCANCODE_F6 -> ImGuiKey.F6;
			case SDLScancode.SDL_SCANCODE_F7 -> ImGuiKey.F7;
			case SDLScancode.SDL_SCANCODE_F8 -> ImGuiKey.F8;
			case SDLScancode.SDL_SCANCODE_F9 -> ImGuiKey.F9;
			case SDLScancode.SDL_SCANCODE_F10 -> ImGuiKey.F10;
			case SDLScancode.SDL_SCANCODE_F11 -> ImGuiKey.F11;
			case SDLScancode.SDL_SCANCODE_F12 -> ImGuiKey.F12;
			case SDLScancode.SDL_SCANCODE_F13 -> ImGuiKey.F13;
			case SDLScancode.SDL_SCANCODE_F14 -> ImGuiKey.F14;
			case SDLScancode.SDL_SCANCODE_F15 -> ImGuiKey.F15;
			case SDLScancode.SDL_SCANCODE_F16 -> ImGuiKey.F16;
			case SDLScancode.SDL_SCANCODE_F17 -> ImGuiKey.F17;
			case SDLScancode.SDL_SCANCODE_F18 -> ImGuiKey.F18;
			case SDLScancode.SDL_SCANCODE_F19 -> ImGuiKey.F19;
			case SDLScancode.SDL_SCANCODE_F20 -> ImGuiKey.F20;
			case SDLScancode.SDL_SCANCODE_F21 -> ImGuiKey.F21;
			case SDLScancode.SDL_SCANCODE_F22 -> ImGuiKey.F22;
			case SDLScancode.SDL_SCANCODE_F23 -> ImGuiKey.F23;
			case SDLScancode.SDL_SCANCODE_F24 -> ImGuiKey.F24;
			case SDLScancode.SDL_SCANCODE_APOSTROPHE -> ImGuiKey.Apostrophe;
			case SDLScancode.SDL_SCANCODE_COMMA -> ImGuiKey.Comma;
			case SDLScancode.SDL_SCANCODE_MINUS -> ImGuiKey.Minus;
			case SDLScancode.SDL_SCANCODE_PERIOD -> ImGuiKey.Period;
			case SDLScancode.SDL_SCANCODE_SLASH -> ImGuiKey.Slash;
			case SDLScancode.SDL_SCANCODE_SEMICOLON -> ImGuiKey.Semicolon;
			case SDLScancode.SDL_SCANCODE_EQUALS -> ImGuiKey.Equal;
			case SDLScancode.SDL_SCANCODE_LEFTBRACKET -> ImGuiKey.LeftBracket;
			case SDLScancode.SDL_SCANCODE_BACKSLASH -> ImGuiKey.Backslash;
			case SDLScancode.SDL_SCANCODE_RIGHTBRACKET -> ImGuiKey.RightBracket;
			case SDLScancode.SDL_SCANCODE_GRAVE -> ImGuiKey.GraveAccent;
			case SDLScancode.SDL_SCANCODE_CAPSLOCK -> ImGuiKey.CapsLock;
			case SDLScancode.SDL_SCANCODE_SCROLLLOCK -> ImGuiKey.ScrollLock;
			case SDLScancode.SDL_SCANCODE_NUMLOCKCLEAR -> ImGuiKey.NumLock;
			case SDLScancode.SDL_SCANCODE_PRINTSCREEN -> ImGuiKey.PrintScreen;
			case SDLScancode.SDL_SCANCODE_PAUSE -> ImGuiKey.Pause;
			case SDLScancode.SDL_SCANCODE_KP_0 -> ImGuiKey.Keypad0;
			case SDLScancode.SDL_SCANCODE_KP_1 -> ImGuiKey.Keypad1;
			case SDLScancode.SDL_SCANCODE_KP_2 -> ImGuiKey.Keypad2;
			case SDLScancode.SDL_SCANCODE_KP_3 -> ImGuiKey.Keypad3;
			case SDLScancode.SDL_SCANCODE_KP_4 -> ImGuiKey.Keypad4;
			case SDLScancode.SDL_SCANCODE_KP_5 -> ImGuiKey.Keypad5;
			case SDLScancode.SDL_SCANCODE_KP_6 -> ImGuiKey.Keypad6;
			case SDLScancode.SDL_SCANCODE_KP_7 -> ImGuiKey.Keypad7;
			case SDLScancode.SDL_SCANCODE_KP_8 -> ImGuiKey.Keypad8;
			case SDLScancode.SDL_SCANCODE_KP_9 -> ImGuiKey.Keypad9;
			case SDLScancode.SDL_SCANCODE_KP_DECIMAL -> ImGuiKey.KeypadDecimal;
			case SDLScancode.SDL_SCANCODE_KP_DIVIDE -> ImGuiKey.KeypadDivide;
			case SDLScancode.SDL_SCANCODE_KP_MULTIPLY -> ImGuiKey.KeypadMultiply;
			case SDLScancode.SDL_SCANCODE_KP_MINUS -> ImGuiKey.KeypadSubtract;
			case SDLScancode.SDL_SCANCODE_KP_PLUS -> ImGuiKey.KeypadAdd;
			case SDLScancode.SDL_SCANCODE_KP_ENTER -> ImGuiKey.KeypadEnter;
			case SDLScancode.SDL_SCANCODE_KP_EQUALS -> ImGuiKey.KeypadEqual;
			case SDLScancode.SDL_SCANCODE_AC_BACK -> ImGuiKey.AppBack;
			case SDLScancode.SDL_SCANCODE_AC_FORWARD -> ImGuiKey.AppForward;
			default -> ImGuiKey.None;
		};
	}
}
