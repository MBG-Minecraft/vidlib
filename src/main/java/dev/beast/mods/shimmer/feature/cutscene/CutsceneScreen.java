package dev.beast.mods.shimmer.feature.cutscene;

import com.mojang.blaze3d.platform.InputConstants;
import dev.beast.mods.shimmer.math.KMath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class CutsceneScreen extends Screen {
	public final Screen previousScreen;
	public final ClientCutscene clientCutscene;

	public CutsceneScreen(ClientCutscene clientCutscene, @Nullable Screen previousScreen) {
		super(Component.empty());
		this.clientCutscene = clientCutscene;
		this.previousScreen = null;
	}

	@Override
	protected void init() {
		minecraft.options.hideGui = true;

		// if (!REClient.adminPanelVisible) {
		{
			var x = (double) (this.minecraft.getWindow().getWidth() / 2);
			var y = (double) (this.minecraft.getWindow().getHeight() / 2);
			InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212995, x, y);
		}
	}

	@Override
	public void removed() {
		minecraft.options.hideGui = false;
		minecraft.gameRenderer.shutdownEffect();
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
		return KMath.lerp(delta, clientCutscene.prevZoom, clientCutscene.zoom);
	}

	@Override
	public boolean renderPlayer() {
		return true;
	}

	@Override
	public boolean overrideCamera() {
		return true;
	}

	@Override
	public Vec3 getCameraPosition(float delta) {
		return clientCutscene.prevOrigin.lerp(clientCutscene.origin, delta);
	}

	@Override
	public Vector3f getCameraRotation(float delta, Vec3 cameraPos) {
		var target = clientCutscene.prevTarget.lerp(clientCutscene.target, delta);

		double dx = target.x - cameraPos.x;
		double dy = target.y - cameraPos.y;
		double dz = target.z - cameraPos.z;
		double hl = Math.sqrt(dx * dx + dz * dz);

		return new Vector3f(Mth.wrapDegrees((float) (Math.toDegrees(Mth.atan2(dz, dx)) - 90F)), Mth.wrapDegrees((float) (-(Math.toDegrees(Mth.atan2(dy, hl))))), 0F);
	}
}
