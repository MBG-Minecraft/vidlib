package dev.latvian.mods.vidlib.feature.imgui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.easing.Easing;
import dev.latvian.mods.klib.math.Range;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.util.FormattedCharSinkPartBuilder;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.extension.imnodes.ImNodes;
import imgui.extension.imnodes.flag.ImNodesColorStyle;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiStyleVar;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class ImGraphics {
	private static class VarStackStack {
		private VarStackStack parent;
		private int pushedStyle = 0;
		private int pushedColors = 0;
		private int pushedItemFlags = 0;
		private int pushedNodesStyle = 0;
		private int pushedNodesColors = 0;
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
		this.isNeoForgeServer = inGame && mc.isServerNeoForge();
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
		// setDefaultStyle();
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

		for (int i = 0; i < stack.pushedNodesStyle; i++) {
			ImNodes.popStyleVar();
		}

		for (int i = 0; i < stack.pushedNodesColors; i++) {
			ImNodes.popColorStyle();
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

	public void setStyleCol(int key, float r, float g, float b, float a) {
		ImGui.pushStyleColor(key, r, g, b, a);
		stack.pushedColors++;
	}

	public void setStyleCol(int key, int r, int g, int b, int a) {
		ImGui.pushStyleColor(key, r, g, b, a);
		stack.pushedColors++;
	}

	public void setStyleCol(int key, int argb) {
		setStyleCol(key, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
	}

	public void setStyleCol(int key, Color value) {
		setStyleCol(key, value.red(), value.green(), value.blue(), value.alpha());
	}

	public void setNodesStyleVar(int key, float value) {
		ImNodes.pushStyleVar(key, value);
		stack.pushedNodesStyle++;
	}

	public void setNodesStyleVar(int key, float x, float y) {
		ImNodes.pushStyleVar(key, x, y);
		stack.pushedNodesStyle++;
	}

	public void setNodesStyleCol(int key, int r, int g, int b, int a) {
		ImNodes.pushColorStyle(key, a << 24 | b << 16 | g << 8 | r);
		stack.pushedNodesColors++;
	}

	public void setNodesStyleCol(int key, float r, float g, float b, float a) {
		setNodesStyleCol(key, (int) (r * 255F), (int) (g * 255F), (int) (b * 255F), (int) (a * 255F));
	}

	public void setNodesStyleCol(int key, int argb) {
		setNodesStyleCol(key, (argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, (argb >> 24) & 0xFF);
	}

	public void setNodesStyleCol(int key, Color value) {
		setNodesStyleCol(key, value.red(), value.green(), value.blue(), value.alpha());
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

	public static void setFullDefaultStyle(ImGuiStyle style) {
		setDefaultStyle(style);
		style.setWindowPadding(4F, 4F);
		style.setFramePadding(4F, 1F);
		style.setPopupBorderSize(0F);
		style.setItemSpacing(6F, 4F);
		style.setItemInnerSpacing(8F, 6F);
	}

	public static void setDefaultStyle(ImGuiStyle style) {
		style.setWindowMenuButtonPosition(ImGuiDir.None);
		style.setWindowRounding(4F);
		style.setFrameRounding(3F);
		style.setChildRounding(3F);
		style.setPopupRounding(3F);
		style.setScrollbarRounding(1F);
		style.setGrabRounding(2F);

		style.setIndentSpacing(25F);
		style.setScrollbarSize(13F);
		style.setGrabMinSize(16F);
		style.setWindowBorderSize(0F);
		style.setSelectableTextAlign(0F, 0.5F);
		style.setAlpha(1F);

		setColor(style, ImGuiCol.WindowBg, 0xFF222228);
		setColor(style, ImGuiCol.PopupBg, 0xE30D0D11);
		setColor(style, ImGuiCol.FrameBg, 0xFF15151C);
		setColor(style, ImGuiCol.TitleBg, 0xFF010101);
		setColor(style, ImGuiCol.TitleBgActive, 0xFF010101);
		setColor(style, ImGuiCol.MenuBarBg, 0xFF17171C);
		setColor(style, ImGuiCol.TitleBgCollapsed, 0xEF517F70);
		setColor(style, ImGuiCol.SliderGrab, 0xFF446692);

		setColor(style, ImGuiCol.Button, ImColorVariant.DEFAULT.color.argb());
		setColor(style, ImGuiCol.ButtonHovered, ImColorVariant.DEFAULT.hoverColor.argb());
		setColor(style, ImGuiCol.ButtonActive, ImColorVariant.DEFAULT.activeColor.argb());
	}

	public static void setColor(ImGuiStyle style, int key, int color) {
		style.setColor(key, ARGB.toABGR(color));
	}

	public void setText(ImColorVariant variant) {
		setStyleCol(ImGuiCol.Text, variant.textColor);
	}

	public void setWarningText() {
		setText(ImColorVariant.YELLOW);
	}

	public void setErrorText() {
		setText(ImColorVariant.RED);
	}

	public void setSuccessText() {
		setText(ImColorVariant.GREEN);
	}

	public void setInfoText() {
		setText(ImColorVariant.BLUE);
	}

	public void setStyle(Style style) {
		if (style.getColor() != null) {
			setStyleCol(ImGuiCol.Text, 0xFF000000 | style.getColor().getValue());
		}
	}

	public void setButton(ImColorVariant variant) {
		setStyleCol(ImGuiCol.Button, variant.color);
		setStyleCol(ImGuiCol.ButtonHovered, variant.hoverColor);
		setStyleCol(ImGuiCol.ButtonActive, variant.activeColor);
	}

	public void setButtonColor(Color col) {
		setStyleCol(ImGuiCol.Button, col);
		setStyleCol(ImGuiCol.ButtonHovered, col.lerp(0.3F, Color.WHITE));
		setStyleCol(ImGuiCol.ButtonActive, col.lerp(0.1F, Color.WHITE));
	}

	public void setNodesPin(ImColorVariant variant) {
		setNodesStyleCol(ImNodesColorStyle.Pin, variant.color);
		setNodesStyleCol(ImNodesColorStyle.PinHovered, variant.hoverColor);
	}

	public void setNodesLink(ImColorVariant variant) {
		setNodesStyleCol(ImNodesColorStyle.Link, variant.color);
		setNodesStyleCol(ImNodesColorStyle.LinkHovered, variant.hoverColor);
		setNodesStyleCol(ImNodesColorStyle.LinkSelected, variant.activeColor);
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

	public void smallText(String text) {
		pushStack();
		setFontScale(0.75F);
		ImGui.text(text);
		popStack();
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

	public <E> ImUpdate combo(String label, Object[] selected, Collection<? extends E> options, Function<E, String> nameFunction, @Nullable ImString search) {
		var result = ImUpdate.NONE;
		var searchText = search != null && options.size() > 16 ? search.get().toLowerCase(Locale.ROOT) : null;

		if (ImGui.beginCombo(label, selected[0] == null ? "Select..." : nameFunction.apply((E) selected[0]), ImGuiInputTextFlags.None)) {
			float y = ImGui.getCursorPos().y;

			if (searchText != null) {
				ImGui.setCursorPosY(y);
				ImGui.setNextItemWidth(-1F);
				ImGui.inputTextWithHint("###search", "Search...", search);
			}

			int i = 0;

			for (var option : options) {
				boolean isSelected = selected[0] == option;
				var itemLabel = nameFunction.apply(option);

				if ((isSelected || searchText == null || searchText.isEmpty() || itemLabel.toLowerCase(Locale.ROOT).contains(searchText)) && ImGui.selectable(itemLabel + "###" + i, isSelected)) {
					selected[0] = option;
					result = ImUpdate.FULL;
				}

				if (isSelected) {
					ImGui.setItemDefaultFocus();
				}

				i++;
			}

			ImGui.endCombo();
		}

		return result;
	}

	public <E> ImUpdate combo(String label, Object[] selected, E[] options, Function<E, String> nameFunction) {
		return combo(label, selected, Arrays.asList(options), nameFunction, null);
	}

	public <E> ImUpdate combo(String label, Object[] selected, E[] options) {
		return combo(label, selected, options, (Function) KLibCodecs.DEFAULT_NAME_GETTER);
	}

	public ImUpdate easingCombo(String label, Easing[] selected) {
		return combo(label, selected, Easing.VALUES);
	}

	public void hideMainMenuBar() {
		BuiltInImGui.mainMenuOpen = false;
	}

	public boolean collapsingHeader(String label, int imGuiTreeNodeFlags) {
		pushStack();
		setStyleCol(ImGuiCol.Header, 0xFF000000);
		boolean open = ImGui.collapsingHeader(label, imGuiTreeNodeFlags);
		popStack();
		return open;
	}

	public boolean collapsingHeader(String label, ImBoolean visible, int imGuiTreeNodeFlags) {
		pushStack();
		setStyleCol(ImGuiCol.Header, 0xFF000000);
		boolean open = ImGui.collapsingHeader(label, visible, imGuiTreeNodeFlags);
		popStack();
		return open;
	}

	public boolean button(String label, @Nullable ImColorVariant variant, float width) {
		if (variant != null) {
			pushStack();
			setButton(variant);
		}

		boolean clicked = ImGui.button(label, width, 0F);

		if (variant != null) {
			popStack();
		}

		return clicked;
	}

	public boolean button(String label, @Nullable ImColorVariant variant) {
		return button(label, variant, 0F);
	}

	public boolean smallButton(String label, @Nullable ImColorVariant variant) {
		if (variant != null) {
			pushStack();
			setButton(variant);
		}

		boolean clicked = ImGui.smallButton(label);

		if (variant != null) {
			popStack();
		}

		return clicked;
	}

	public void setRedButton() {
		setButton(ImColorVariant.RED);
	}

	public void setGreenButton() {
		setButton(ImColorVariant.GREEN);
	}

	public void text(List<FormattedCharSinkPartBuilder.Part> parts) {
		ImGui.beginGroup();
		pushStack();
		setStyleVar(ImGuiStyleVar.ItemSpacing, 0F, ImGui.getStyle().getItemSpacingY());

		for (int i = 0; i < parts.size(); i++) {
			if (i > 0) {
				ImGui.sameLine();
			}

			var part = parts.get(i);
			setStyle(part.style());
			ImGui.textUnformatted(part.text());
		}

		popStack();
		ImGui.endGroup();
	}

	public boolean iconButton(ImIcons icon, String id, String tooltip, @Nullable ImColorVariant variant) {
		if (variant != null) {
			pushStack();
			setButton(variant);
		}

		boolean clicked = ImGui.button(icon + id);

		if (variant != null) {
			popStack();
		}

		ImGuiUtils.hoveredTooltip(tooltip);
		return clicked;
	}

	public boolean iconButton(ImIcons icon, String id, String tooltip, @Nullable ImColorVariant variant, ImBoolean value) {
		if (iconButton(icon, id, tooltip, variant)) {
			value.set(!value.get());
			return true;
		}

		return false;
	}

	public boolean toggleButton(ImIcons icon, String id, String tooltip, ImBoolean value) {
		return iconButton(icon, (value.get() ? ImIcons.CHECK : ImIcons.CLOSE) + id, tooltip + (value.get() ? ": Enabled" : ": Disabled"), value.get() ? null : ImColorVariant.GRAY, value);
	}
}
