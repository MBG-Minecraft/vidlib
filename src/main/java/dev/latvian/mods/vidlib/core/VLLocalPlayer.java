package dev.latvian.mods.vidlib.core;

import dev.latvian.mods.vidlib.feature.input.PlayerInput;
import dev.latvian.mods.vidlib.feature.session.LocalClientSessionData;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

public interface VLLocalPlayer extends VLClientPlayer {
	@Override
	default LocalPlayer vl$self() {
		return (LocalPlayer) this;
	}

	@Override
	default LocalClientSessionData vl$sessionData() {
		throw new NoMixinException(this);
	}

	static PlayerInput fromInput(long windowId, LocalPlayer player, boolean mouse) {
		var in = player.input.keyPresses;
		// Modifiers
		boolean shift = GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
		boolean control = GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
		boolean alt = GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
		boolean tab = GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_TAB) == GLFW.GLFW_PRESS;
		// Mouse
		boolean mouseLeft = mouse && GLFW.glfwGetMouseButton(windowId, 0) == GLFW.GLFW_PRESS;
		boolean mouseRight = mouse && GLFW.glfwGetMouseButton(windowId, 1) == GLFW.GLFW_PRESS;
		boolean mouseMiddle = mouse && GLFW.glfwGetMouseButton(windowId, 2) == GLFW.GLFW_PRESS;
		boolean mouseBack = mouse && GLFW.glfwGetMouseButton(windowId, 3) == GLFW.GLFW_PRESS;
		boolean mouseNext = mouse && GLFW.glfwGetMouseButton(windowId, 4) == GLFW.GLFW_PRESS;

		return PlayerInput.of(
			// Movement
			in.forward(), in.backward(), in.left(), in.right(), in.jump(), in.shift(), in.sprint(),
			// Modifiers
			shift, control, alt, tab,
			// Mouse
			mouseLeft, mouseRight, mouseMiddle, mouseBack, mouseNext
		);
	}
}
