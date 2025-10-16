package dev.latvian.mods.vidlib.feature.cutscene;

import com.mojang.blaze3d.platform.InputConstants;
import dev.latvian.mods.klib.math.Rotation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

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
		return Screen.hasShiftDown() && minecraft.isLocalServer();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		var barVisibility = Mth.lerp(delta, clientCutscene.state.prevBarVisibility, clientCutscene.state.barVisibility);

		if (barVisibility > 0F) {
			int barSize = 35;

			graphics.fill(0, 0, width, barSize, 0xFF000000);

			if (!clientCutscene.state.topBar.isEmpty()) {
				var tb = new ArrayList<FormattedCharSequence>(clientCutscene.state.topBar.size());

				for (var line : clientCutscene.state.topBar) {
					tb.addAll(font.split(line, width - 60));
				}

				for (int i = 0; i < tb.size(); i++) {
					int x = (width - font.width(tb.get(i))) / 2;
					int y = (barSize - tb.size() * 10) / 2 + i * 10;
					graphics.drawString(font, tb.get(i), x, y, 0xFFFFFFFF, true);
				}
			}

			graphics.fill(0, height - barSize, width, height, 0xFF000000);

			if (!clientCutscene.state.bottomBar.isEmpty()) {
				var bb = new ArrayList<FormattedCharSequence>(clientCutscene.state.bottomBar.size());

				for (var line : clientCutscene.state.bottomBar) {
					bb.addAll(font.split(line, width - 60));
				}

				for (int i = 0; i < bb.size(); i++) {
					int x = (width - font.width(bb.get(i))) / 2;
					int y = (barSize - bb.size() * 10) / 2 + i * 10;
					graphics.drawString(font, bb.get(i), x, height - barSize + y, 0xFFFFFFFF, true);
				}
			}
		}
	}

	@Override
	public double getFOVModifier(double delta) {
		return clientCutscene.getFOVModifier(delta);
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
	public boolean hideGui() {
		return true;
	}

	@Override
	@Nullable
	public Biome.Precipitation getWeatherOverride() {
		return clientCutscene.getWeatherOverride();
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
