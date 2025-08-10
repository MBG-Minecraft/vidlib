package dev.latvian.mods.vidlib.feature.imgui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.klib.math.Range;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiComboFlags;
import imgui.flag.ImGuiStyleVar;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ImGraphics {
	private static class VarStackStack {
		private VarStackStack parent;
		private int pushedStyle = 0;
		private int pushedColors = 0;
		private int pushedItemFlags = 0;
		private float currentFontScale = 1F;
		private FloatList pushedFontScales = null;
		private ImNumberType numberType = null;
		private Range numberRange = null;
	}

	public final Minecraft mc;
	public final boolean inGame;
	public final boolean isReplay;
	public final boolean isNeoForgeServer;
	public final boolean isClientOnly;
	public final boolean adminPanel;
	public final boolean isAdmin;
	private VarStackStack stack;

	public ImGraphics(Minecraft mc) {
		this.mc = mc;
		this.inGame = mc.player != null && mc.level != null;
		this.isReplay = inGame && mc.level.isReplayLevel();
		this.isNeoForgeServer = inGame && "neoforge".equals(mc.player.connection.serverBrand());
		this.isClientOnly = isReplay || !isNeoForgeServer;
		this.adminPanel = VidLibClientOptions.getAdminPanel();
		this.isAdmin = inGame && (mc.isLocalServer() || mc.player.hasPermissions(2));
	}

	public void pushStack() {
		var newStack = new VarStackStack();
		newStack.parent = stack;

		if (stack != null) {
			newStack.numberType = stack.numberType;
			newStack.numberRange = stack.numberRange;
		}

		stack = newStack;
	}

	public void pushRootStack() {
		pushStack();
		setDefaultStyle();
		setNumberType(ImNumberType.DOUBLE);
		setNumberRange(null);
	}

	public void popStack() {
		if (stack == null) {
			throw new RuntimeException("popStack() called without a matching pushStack()");
		}

		if (stack.pushedStyle > 0) {
			ImGui.popStyleVar(stack.pushedStyle);
		}

		if (stack.pushedColors > 0) {
			ImGui.popStyleColor(stack.pushedColors);
		}

		for (int i = 0; i < stack.pushedItemFlags; i++) {
			imgui.internal.ImGui.popItemFlag();
		}

		if (stack.pushedFontScales != null) {
			var font = ImGui.getFont();

			for (int i = stack.pushedFontScales.size() - 1; i >= 0; i--) {
				font.setScale(stack.pushedFontScales.getFloat(i));
				ImGui.popFont();
			}
		}

		stack = stack.parent;
	}

	public void setStyleVar(int key, float value) {
		ImGui.pushStyleVar(key, value);
		stack.pushedStyle++;
	}

	public void setStyleVar(int key, float x, float y) {
		ImGui.pushStyleVar(key, x, y);
		stack.pushedStyle++;
	}

	public void setStyleCol(int key, int argb) {
		setStyleCol(key, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
	}

	public void setStyleCol(int key, float r, float g, float b, float a) {
		ImGui.pushStyleColor(key, r, g, b, a);
		stack.pushedColors++;
	}

	public void setStyleCol(int key, int r, int g, int b, int a) {
		ImGui.pushStyleColor(key, r, g, b, a);
		stack.pushedColors++;
	}

	public void setStyleCol(int key, Color value) {
		setStyleCol(key, value.red(), value.green(), value.blue(), value.alpha());
	}

	public void setItemFlag(int key, boolean flag) {
		imgui.internal.ImGui.pushItemFlag(key, flag);
		stack.pushedItemFlags++;
	}

	public void setFontScale(float scale) {
		var font = ImGui.getFont();
		font.setScale(scale);
		ImGui.pushFont(font);

		if (stack.pushedFontScales == null) {
			stack.pushedFontScales = new FloatArrayList(1);
		}

		stack.pushedFontScales.add(stack.currentFontScale);
		stack.currentFontScale = scale;
	}

	public void setNumberType(ImNumberType type) {
		stack.numberType = type;
	}

	public ImNumberType getNumberType() {
		return stack.numberType;
	}

	public void setNumberRange(@Nullable Range range) {
		stack.numberRange = range;
	}

	@Nullable
	public Range getNumberRange() {
		return stack.numberRange;
	}

	public void setDefaultStyle() {
		setStyleVar(ImGuiStyleVar.WindowPadding, 15F, 15F);
		setStyleVar(ImGuiStyleVar.WindowRounding, 5F);
		setStyleVar(ImGuiStyleVar.FramePadding, 5F, 5F);
		setStyleVar(ImGuiStyleVar.FrameRounding, 4F);
		setStyleVar(ImGuiStyleVar.ChildRounding, 4F);
		setStyleVar(ImGuiStyleVar.PopupRounding, 4F);
		setStyleVar(ImGuiStyleVar.PopupBorderSize, 0F);
		setStyleVar(ImGuiStyleVar.ItemSpacing, 12F, 8F);
		setStyleVar(ImGuiStyleVar.ItemInnerSpacing, 8F, 6F);
		setStyleVar(ImGuiStyleVar.IndentSpacing, 25F);
		setStyleVar(ImGuiStyleVar.ScrollbarSize, 15F);
		setStyleVar(ImGuiStyleVar.ScrollbarRounding, 9F);
		setStyleVar(ImGuiStyleVar.GrabMinSize, 5F);
		setStyleVar(ImGuiStyleVar.GrabRounding, 3F);
		setStyleVar(ImGuiStyleVar.WindowBorderSize, 0F);
		setStyleVar(ImGuiStyleVar.SelectableTextAlign, 0F, 0.5F);
		setStyleVar(ImGuiStyleVar.Alpha, 1F);

		setStyleCol(ImGuiCol.WindowBg, 0xEF292930);
		setStyleCol(ImGuiCol.FrameBg, 0xFF1D1D23); // checkboxes, sliders
		setStyleCol(ImGuiCol.TitleBg, 0xEF384756);
		setStyleCol(ImGuiCol.TitleBgActive, 0xEF70517F);
		setStyleCol(ImGuiCol.MenuBarBg, 0xFF17171C);
		setStyleCol(ImGuiCol.TitleBgCollapsed, 0xEF517F70);
	}

	public void setWarningText() {
		setStyleCol(ImGuiCol.Text, 0xFFFFFF55);
	}

	public void setErrorText() {
		setStyleCol(ImGuiCol.Text, 0xFFFF5555);
	}

	public void setSuccessText() {
		setStyleCol(ImGuiCol.Text, 0xFF8CFF95);
	}

	public void setInfoText() {
		setStyleCol(ImGuiCol.Text, 0xFF63BEFF);
	}

	public void setRedButton() {
		setStyleCol(ImGuiCol.Button, 0xFFA0243B);
		setStyleCol(ImGuiCol.ButtonHovered, 0xFFFF5E5E);
		setStyleCol(ImGuiCol.ButtonActive, 0xFFEE3333);
	}

	public void setGreenButton() {
		setStyleCol(ImGuiCol.Button, 0xFF249E3E);
		setStyleCol(ImGuiCol.ButtonHovered, 0xFF2EC618);
		setStyleCol(ImGuiCol.ButtonActive, 0xFF49ED34);
	}

	public void setButtonColor(Color col) {
		setStyleCol(ImGuiCol.Button, col);
		setStyleCol(ImGuiCol.ButtonHovered, col.lerp(0.3F, dev.latvian.mods.klib.color.Color.WHITE));
		setStyleCol(ImGuiCol.ButtonActive, col.lerp(0.1F, Color.WHITE));
	}

	public void stackTrace(Throwable throwable) {
		pushStack();
		setErrorText();
		setStyleVar(ImGuiStyleVar.ItemSpacing, 0F, 0F);

		var stackTrace = throwable.getStackTrace();

		ImGui.textWrapped(throwable + " [" + stackTrace.length + " lines]");

		for (var e : stackTrace) {
			int cni = e.getClassName().lastIndexOf('.');

			ImGui.text("  at " + (cni == -1 ? e.getClassName() : e.getClassName().substring(cni + 1)) + ".");
			ImGui.sameLine();
			pushStack();
			setSuccessText();
			ImGui.text(e.getMethodName());
			popStack();
			ImGui.sameLine();
			ImGui.text("(");

			boolean exit = false;

			if (e.isNativeMethod()) {
				ImGui.sameLine();
				pushStack();
				setWarningText();
				ImGui.text("Native Method");
				popStack();
			} else if (e.getFileName() == null) {
				ImGui.sameLine();
				pushStack();
				setWarningText();
				ImGui.text("Unknown Source");
				popStack();
			} else {
				ImGui.sameLine();
				pushStack();
				setWarningText();
				ImGui.text(e.getFileName());

				if (e.getFileName().equals("Main.java")) {
					exit = true;
				}

				popStack();

				if (e.getLineNumber() >= 0) {
					ImGui.sameLine();
					ImGui.text(":");
					ImGui.sameLine();
					pushStack();
					setInfoText();
					ImGui.text(String.valueOf(e.getLineNumber()));
					popStack();
				}
			}

			ImGui.sameLine();
			ImGui.text(")");

			if (exit) {
				break;
			}
		}

		popStack();
	}

	public void redTextIf(String text, boolean condition) {
		if (condition) {
			pushStack();
			setErrorText();
			ImGui.text(text);
			popStack();
		} else {
			ImGui.text(text);
		}
	}

	public void redWrappedTextIf(String text, boolean condition) {
		if (condition) {
			pushStack();
			setErrorText();
			ImGui.textWrapped(text);
			popStack();
		} else {
			ImGui.textWrapped(text);
		}
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

	public void setNextWindowOverViewport() {
		var viewport = ImGui.getMainViewport();
		ImGui.setNextWindowPos(viewport.getWorkPos().x, viewport.getWorkPos().y);
		ImGui.setNextWindowSize(viewport.getWorkSizeX(), viewport.getWorkSizeY());
	}

	public <E> ImUpdate combo(String label, String defaultPreview, Object[] selected, List<? extends E> options, Function<E, String> nameFunction, int comboFlags) {
		var result = ImUpdate.NONE;

		if (ImGui.beginCombo(label, selected[0] == null ? defaultPreview : nameFunction.apply((E) selected[0]), comboFlags)) {
			for (int i = 0; i < options.size(); i++) {
				var option = options.get(i);
				boolean isSelected = selected[0] == option;

				if (ImGui.selectable(nameFunction.apply(option) + "###" + i, isSelected)) {
					selected[0] = option;
					result = ImUpdate.FULL;
				}

				if (isSelected) {
					ImGui.setItemDefaultFocus();
				}
			}

			ImGui.endCombo();
		}

		return result;
	}

	public <E> ImUpdate combo(String label, String defaultPreview, Object[] selected, E[] options, Function<E, String> nameFunction, int comboFlags) {
		return combo(label, defaultPreview, selected, Arrays.asList(options), nameFunction, comboFlags);
	}

	public <E> ImUpdate combo(String label, String defaultPreview, Object[] selected, E[] options) {
		return combo(label, defaultPreview, selected, options, (Function) KLibCodecs.DEFAULT_NAME_GETTER, ImGuiComboFlags.None);
	}

	public <E> ImUpdate combo(String label, String defaultPreview, Object[] selected, List<? extends E> options) {
		return combo(label, defaultPreview, selected, options, (Function) KLibCodecs.DEFAULT_NAME_GETTER, ImGuiComboFlags.None);
	}

	public ImUpdate easingCombo(String label, Easing[] selected) {
		return combo(label, "Select Easing...", selected, Easing.VALUES);
	}

	public void hideMainMenuBar() {
		BuiltInImGui.mainMenuOpen = false;
	}
}
