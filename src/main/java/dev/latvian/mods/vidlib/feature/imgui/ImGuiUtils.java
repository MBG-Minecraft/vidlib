package dev.latvian.mods.vidlib.feature.imgui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import imgui.ImGui;
import imgui.ImGuiViewport;
import imgui.ImVec2;
import imgui.ImVec4;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImDouble;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

public class ImGuiUtils {
	public static final ImInt INT = new ImInt();
	public static final int[] INT2 = new int[2];
	public static final int[] INT3 = new int[3];
	public static final int[] INT4 = new int[4];
	public static final ImFloat FLOAT = new ImFloat();
	public static final float[] FLOAT2 = new float[2];
	public static final float[] FLOAT3 = new float[3];
	public static final float[] FLOAT4 = new float[4];
	public static final ImVec2 VEC2 = new ImVec2();
	public static final ImVec4 VEC4 = new ImVec4();
	public static final ImDouble DOUBLE = new ImDouble();
	public static final double[] DOUBLE2 = new double[2];
	public static final double[] DOUBLE3 = new double[3];
	public static final double[] DOUBLE4 = new double[4];
	public static final ImString STRING = new ImString();
	public static final ImBoolean BOOLEAN = new ImBoolean();

	static {
		STRING.inputData.isResizable = true;
	}

	public static void resetResolution() {
		forceResolution(-1, -1);
	}

	public static boolean isResolutionForced() {
		return ImGuiHooks.forceWidth > 0 && ImGuiHooks.forceHeight > 0;
	}

	public static CompletableFuture<Void> forceResolution(int width, int height) {
		ImGuiHooks.forceWidth = width;
		ImGuiHooks.forceHeight = height;

		return CompletableFuture.runAsync(() -> {
			var fbo = Minecraft.getInstance().getMainRenderTarget();
			while (ImGuiHooks.forceWidth != fbo.width || ImGuiHooks.forceHeight != fbo.height) {
			}
		});
	}

	public static void enableNavigationWhen(BooleanSupplier condition) {
		ImGuiHooks.navigationConditions.add(condition);
	}

	public static float getDpiScale() {
		return ImGuiHooks.dpiScale;
	}

	public static int getTextureId(ResourceLocation identifier) {
		return Minecraft.getInstance().getTextureManager().getTexture(identifier).getTexture().vl$getHandle();
	}

	public static void texture(ResourceLocation id, float width, float height) {
		ImGui.image(getTextureId(id), width, height);
	}

	public static void texture(ResourceLocation id, float width, float height, float u0, float v0) {
		ImGui.image(getTextureId(id), width, height, u0, v0);
	}

	public static void texture(ResourceLocation id, float width, float height, float u0, float v0, float u1, float v1) {
		ImGui.image(getTextureId(id), width, height, u0, v0, u1, v1);
	}

	public static void framebuffer(RenderTarget fbo) {
		framebuffer(fbo, 1);
	}

	public static void framebuffer(RenderTarget fbo, float scale) {
		framebuffer(fbo, fbo.width * scale, fbo.height * scale, 0, 1, 1, 0);
	}

	public static void framebuffer(RenderTarget fbo, float width, float height, float u0, float v0, float u1, float v1) {
		ImGui.image(fbo.getColorTexture().vl$getHandle(), width, height, u0, v0, u1, v1);
	}

	public static boolean position(String id, Vector3f pos) {
		var changed = false;
		var prevColumns = ImGui.getColumnsCount();
		ImGui.columns(3, "##" + id, false);

		FLOAT.set(pos.x());
		changed = ImGui.inputFloat("X", FLOAT, 0, 0, "%.3f", ImGuiInputTextFlags.EnterReturnsTrue);
		pos.set(FLOAT.get(), pos.y(), pos.z());

		ImGui.nextColumn();

		FLOAT.set(pos.y());
		changed = ImGui.inputFloat("Y", FLOAT, 0, 0, "%.3f", ImGuiInputTextFlags.EnterReturnsTrue) | changed;
		pos.set(pos.x(), FLOAT.get(), pos.z());

		ImGui.nextColumn();

		FLOAT.set(pos.z());
		changed = ImGui.inputFloat("Z", FLOAT, 0, 0, "%.3f", ImGuiInputTextFlags.EnterReturnsTrue) | changed;
		pos.set(pos.x(), pos.y(), FLOAT.get());

		ImGui.columns(prevColumns);
		return changed;
	}

	public static int getDockId() {
		return ImGuiHooks.dockId;
	}

	/**
	 * {@link ImGui#begin(String, int) begin} an invisible and un-interactable new window that occupies the full space of the {@link ImGui#getMainViewport() main viewport}. This is useful for creating
	 * docking spaces and full-screen windows that are fully invisible outside of their children elements.
	 *
	 * @param title            unique title of the window
	 * @param imGuiWindowFlags additional window flags
	 * @return {@code true} if the window is being rendered
	 */
	public static boolean beginInvisibleWindowOverViewport(String title, int imGuiWindowFlags) {
		ImGui.pushStyleColor(ImGuiCol.WindowBg, 0x00000000);
		ImGui.setNextWindowBgAlpha(0.0f);
		int flags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoMove
			| ImGuiWindowFlags.NoDocking | ImGuiWindowFlags.NoBringToFrontOnFocus | ImGuiWindowFlags.NoNavFocus
			| ImGuiWindowFlags.None | ImGuiWindowFlags.NoInputs | imGuiWindowFlags;
		boolean window = beginWindowOverViewport(title, flags);
		ImGui.popStyleColor();
		return window;
	}

	/**
	 * {@link ImGui#begin(String, int) begin} a new window that occupies the full space of the {@link ImGui#getMainViewport() main viewport}.
	 *
	 * <p>
	 * To make the window unmovable, pass {@link ImGuiWindowFlags#NoMove}. To make the window transparent (invisible, and passthrough all interactions), visit {@link #beginInvisibleWindowOverViewport(String, int)}.
	 *
	 * @param title            unique title of the window
	 * @param imGuiWindowFlags window flags
	 * @return {@code true} if the window is being rendered
	 */
	public static boolean beginWindowOverViewport(String title, int imGuiWindowFlags) {
		ImGuiViewport viewport = ImGui.getMainViewport();
		ImGui.setNextWindowPos(viewport.getWorkPos().x, viewport.getWorkPos().y);
		ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY());
		return ImGui.begin(title, imGuiWindowFlags);
	}
}
