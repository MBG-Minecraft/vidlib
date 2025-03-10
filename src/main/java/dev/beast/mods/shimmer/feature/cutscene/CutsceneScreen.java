package dev.beast.mods.shimmer.feature.cutscene;

import com.mojang.blaze3d.platform.InputConstants;
import dev.beast.mods.shimmer.math.Rotation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

public class CutsceneScreen extends Screen {
	public final Screen previousScreen;
	public final ClientCutscene clientCutscene;

	public CutsceneScreen(ClientCutscene clientCutscene, @Nullable Screen previousScreen) {
		super(Component.empty());
		this.clientCutscene = clientCutscene;
		this.previousScreen = previousScreen;
	}

	@Override
	protected void init() {
		minecraft.options.hideGui = true;

		// if (!REClient.adminPanelVisible) {
		{
			var x = (double) (this.minecraft.getWindow().getWidth() / 2);
			var y = (double) (this.minecraft.getWindow().getHeight() / 2);
			InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR_DISABLED, x, y);
		}
	}

	@Override
	public void removed() {
		var x = (double) (this.minecraft.getWindow().getWidth() / 2);
		var y = (double) (this.minecraft.getWindow().getHeight() / 2);
		InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), GLFW.GLFW_CURSOR_NORMAL, x, y);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return Screen.hasShiftDown();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		int barSize = 35;

		var tb = clientCutscene.topBar;

		if (tb != null) {
			graphics.fill(0, 0, width, barSize, 0xFF000000);

			for (int i = 0; i < tb.size(); i++) {
				int x = (width - font.width(tb.get(i))) / 2;
				int y = (barSize - tb.size() * 10) / 2 + i * 10;
				graphics.drawString(font, tb.get(i), x, y, 0xFFFFFFFF, true);
			}
		}

		var bb = clientCutscene.bottomBar;

		if (bb != null) {
			graphics.fill(0, height - barSize, width, height, 0xFF000000);

			for (int i = 0; i < bb.size(); i++) {
				int x = (width - font.width(bb.get(i))) / 2;
				int y = (barSize - bb.size() * 10) / 2 + i * 10;
				graphics.drawString(font, bb.get(i), x, height - barSize + y, 0xFFFFFFFF, true);
			}
		}
	}

	@Override
	public double getZoom(double delta) {
		return clientCutscene.getZoom(delta);
	}

	@Override
	public boolean renderPlayer() {
		return clientCutscene.renderPlayer();
	}

	@Override
	public boolean overrideCamera() {
		return clientCutscene.overrideCamera();
	}

	@Override
	public Vec3 getCameraPosition(float delta) {
		return clientCutscene.getCameraPosition(delta);
	}

	@Override
	public Rotation getCameraRotation(float delta, Vec3 cameraPos) {
		return clientCutscene.getCameraRotation(delta, cameraPos);
	}
}
