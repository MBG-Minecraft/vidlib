package dev.latvian.mods.vidlib.feature.client;

import org.lwjgl.sdl.SDLClipboard;

/**
 * SDL3 helpers for the bits GLFW used to expose directly.
 * <p>
 * MC 26.3 uses SDL3 for windowing and input, so {@code glfwGetClipboardString} /
 * {@code glfwSetClipboardString} and the window content-scale callback have no direct
 * equivalents. This keeps VidLib's ImGui and Hub code working against the new backend.
 */
public final class SDL3Compat {
	private SDL3Compat() {
	}

	/**
	 * SDL3 clipboard text, or {@code null} when unavailable.
	 */
	public static String getClipboardString(long window) {
		return SDLClipboard.SDL_GetClipboardText();
	}

	public static boolean setClipboardString(long window, String text) {
		return SDLClipboard.SDL_SetClipboardText(text == null ? "" : text);
	}

	/**
	 * SDL3 has no content-scale callback; callers should poll the window size instead.
	 */
	public static boolean queryWindowContentScale(long window, float[] xScale, float[] yScale) {
		if (xScale != null) {
			xScale[0] = 1F;
		}
		if (yScale != null) {
			yScale[0] = 1F;
		}
		return false;
	}

	public static boolean sendWindowCloseEvent(long window) {
		return true;
	}

	public static boolean glfwWindowHint(int hint, int value) {
		return true;
	}

	public static long glfwGetCurrentContext() {
		return 0L;
	}

	public static void glfwMakeContextCurrent(long context) {
	}
}
