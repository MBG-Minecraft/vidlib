package dev.latvian.mods.vidlib.feature.imgui;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.GpuTexture;
import dev.latvian.mods.klib.codec.KLibCodecs;
import dev.latvian.mods.klib.color.Color;
import dev.latvian.mods.klib.math.Range;
import dev.latvian.mods.klib.texture.UV;
import dev.latvian.mods.vidlib.feature.client.VidLibClientOptions;
import dev.latvian.mods.vidlib.feature.feature.FeatureSet;
import dev.latvian.mods.vidlib.feature.imgui.icon.ImIcons;
import dev.latvian.mods.vidlib.feature.session.LocalClientSessionData;
import dev.latvian.mods.vidlib.util.FormattedCharSinkPartBuilder;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.extension.imnodes.ImNodes;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiDir;
import imgui.flag.ImGuiInputTextFlags;
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

public class ImGraphics implements ImStyleVarConsumer, ImStyleColorConsumer, ImNodesStyleVarConsumer, ImNodesStyleColorConsumer {
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
	public final LocalClientSessionData session;
	public final boolean isSinglePlayer;
	public final boolean isReplay;
	public final FeatureSet serverFeatures;
	public final boolean adminPanel;
	public final boolean isAdmin;
	private VarStackStack stack;

	public ImGraphics(Minecraft mc) {
		this.mc = mc;
		this.inGame = mc.player != null && mc.level != null;
		this.session = inGame ? mc.player.vl$sessionData() : null;
		this.isSinglePlayer = inGame && mc.isLocalServer();
		this.isReplay = inGame && mc.level.isReplayLevel();
		this.serverFeatures = inGame ? mc.level.getServerFeatures() : FeatureSet.EMPTY;
		this.adminPanel = VidLibClientOptions.getAdminPanel();
		this.isAdmin = inGame && (isSinglePlayer || mc.player.hasPermissions(2));
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

	@Override
	public void setStyleVar(int key, float value) {
		ImGui.pushStyleVar(key, value);
		stack.pushedStyle++;
	}

	@Override
	public void setStyleVar(int key, float x, float y) {
		ImGui.pushStyleVar(key, x, y);
		stack.pushedStyle++;
	}

	@Override
	public void setStyleCol(int key, int r, int g, int b, int a) {
		ImGui.pushStyleColor(key, r, g, b, a);
		stack.pushedColors++;
	}

	@Override
	public void setNodesStyleVar(int key, float value) {
		ImNodes.pushStyleVar(key, value);
		stack.pushedNodesStyle++;
	}

	@Override
	public void setNodesStyleVar(int key, float x, float y) {
		ImNodes.pushStyleVar(key, x, y);
		stack.pushedNodesStyle++;
	}

	@Override
	public void setNodesStyleCol(int key, int r, int g, int b, int a) {
		ImNodes.pushColorStyle(key, a << 24 | b << 16 | g << 8 | r);
		stack.pushedNodesColors++;
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

	public void setStyle(Style style) {
		if (style.getColor() != null) {
			setStyleCol(ImGuiCol.Text, 0xFF000000 | style.getColor().getValue());
		}
	}

	public void stackTrace(Throwable throwable) {
		pushStack();
		setErrorText();
		setItemSpacing(0F, 0F);

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

	public void text(List<FormattedCharSinkPartBuilder.Part> parts) {
		ImGui.beginGroup();
		pushStack();
		setItemSpacing(0F, ImGui.getStyle().getItemSpacingY());

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

	public boolean imageButton(@Nullable GpuTexture texture, float w, float h, UV uv, int padding, @Nullable ImColorVariant variant, Color background, Color tint) {
		if (variant != null) {
			pushStack();
			setButton(variant);
		}

		var clicked = ImGui.imageButton(
			texture == null ? 0 : texture.vl$getHandle(),
			w, h,
			uv.u0(), uv.v0(), uv.u1(), uv.v1(),
			padding,
			background.redf(), background.greenf(), background.bluef(), background.alphaf(),
			tint.redf(), tint.greenf(), tint.bluef(), tint.alphaf()
		);

		if (variant != null) {
			popStack();
		}

		return clicked;
	}

	public boolean imageButton(@Nullable GpuTexture texture, float w, float h, UV uv, int padding, @Nullable ImColorVariant variant) {
		return imageButton(texture, w, h, uv, padding, variant, Color.TRANSPARENT, Color.WHITE);
	}

	public boolean imageButton(@Nullable ResourceLocation texture, float w, float h, UV uv, int padding, @Nullable ImColorVariant variant) {
		return imageButton(texture == null ? null : mc.getTextureManager().getTexture(texture).getTexture(), w, h, uv, padding, variant);
	}
}
