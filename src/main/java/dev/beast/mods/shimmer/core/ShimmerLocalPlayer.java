package dev.beast.mods.shimmer.core;

import dev.beast.mods.shimmer.feature.input.PlayerInput;
import dev.beast.mods.shimmer.feature.session.ShimmerLocalClientSessionData;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

public interface ShimmerLocalPlayer extends ShimmerClientPlayer, ShimmerClientEntityContainer {
	@Override
	default ShimmerLocalClientSessionData shimmer$sessionData() {
		throw new NoMixinException();
	}

	static PlayerInput fromInput(long windowId, LocalPlayer player, boolean mouse) {
		var in = player.input.keyPresses;
		// Modifiers
		boolean shift = Screen.hasShiftDown();
		boolean control = Screen.hasControlDown();
		boolean alt = Screen.hasAltDown();
		boolean tab = GLFW.glfwGetKey(windowId, GLFW.GLFW_KEY_TAB) == GLFW.GLFW_PRESS;
		// Mouse
		boolean mouseLeft = mouse && GLFW.glfwGetMouseButton(windowId, 0) == GLFW.GLFW_PRESS;
		boolean mouseRight = mouse && GLFW.glfwGetMouseButton(windowId, 1) == GLFW.GLFW_PRESS;
		boolean mouseMiddle = mouse && GLFW.glfwGetMouseButton(windowId, 2) == GLFW.GLFW_PRESS;
		boolean mouseBack = mouse && GLFW.glfwGetMouseButton(windowId, 3) == GLFW.GLFW_PRESS;
		boolean mouseNext = mouse && GLFW.glfwGetMouseButton(windowId, 4) == GLFW.GLFW_PRESS;

		return new PlayerInput(
			PlayerInput.getFlags(
				// Movement
				in.forward(), in.backward(), in.left(), in.right(), in.jump(), in.shift(), in.sprint(),
				// Modifiers
				shift, control, alt, tab,
				// Mouse
				mouseLeft, mouseRight, mouseMiddle, mouseBack, mouseNext
			),
			// Movement
			in.forward(), in.backward(), in.left(), in.right(), in.jump(), in.shift(), in.sprint(),
			// Modifiers
			shift, control, alt, tab,
			// Mouse
			mouseLeft, mouseRight, mouseMiddle, mouseBack, mouseNext
		);
	}
}
