package dev.latvian.mods.vidlib.feature.imgui;

import imgui.flag.ImGuiStyleVar;

public interface ImStyleVarConsumer {
	void setStyleVar(int key, float value);

	void setStyleVar(int key, float x, float y);

	default void setAlpha(float value) {
		setStyleVar(ImGuiStyleVar.Alpha, value);
	}

	default void setDisabledAlpha(float value) {
		setStyleVar(ImGuiStyleVar.DisabledAlpha, value);
	}

	default void setWindowPadding(float x, float y) {
		setStyleVar(ImGuiStyleVar.WindowPadding, x, y);
	}

	default void setWindowRounding(float value) {
		setStyleVar(ImGuiStyleVar.WindowRounding, value);
	}

	default void setWindowBorderSize(float value) {
		setStyleVar(ImGuiStyleVar.WindowBorderSize, value);
	}

	default void setWindowMinSize(float x, float y) {
		setStyleVar(ImGuiStyleVar.WindowMinSize, x, y);
	}

	default void setWindowTitleAlign(float x, float y) {
		setStyleVar(ImGuiStyleVar.WindowTitleAlign, x, y);
	}

	@Deprecated
	default void setWindowMenuButtonPosition(int value) {
		// setStyleVar(ImGuiStyleVar.WindowMenuButtonPosition, value);
	}

	default void setChildRounding(float value) {
		setStyleVar(ImGuiStyleVar.ChildRounding, value);
	}

	default void setChildBorderSize(float value) {
		setStyleVar(ImGuiStyleVar.ChildBorderSize, value);
	}

	default void setPopupRounding(float value) {
		setStyleVar(ImGuiStyleVar.PopupRounding, value);
	}

	default void setPopupBorderSize(float value) {
		setStyleVar(ImGuiStyleVar.PopupBorderSize, value);
	}

	default void setFramePadding(float x, float y) {
		setStyleVar(ImGuiStyleVar.FramePadding, x, y);
	}

	default void setFrameRounding(float value) {
		setStyleVar(ImGuiStyleVar.FrameRounding, value);
	}

	default void setFrameBorderSize(float value) {
		setStyleVar(ImGuiStyleVar.FrameBorderSize, value);
	}

	default void setItemSpacing(float x, float y) {
		setStyleVar(ImGuiStyleVar.ItemSpacing, x, y);
	}

	default void setItemInnerSpacing(float x, float y) {
		setStyleVar(ImGuiStyleVar.ItemInnerSpacing, x, y);
	}

	default void setCellPadding(float x, float y) {
		setStyleVar(ImGuiStyleVar.CellPadding, x, y);
	}

	@Deprecated
	default void setTouchExtraPadding(float x, float y) {
		// setStyleVar(ImGuiStyleVar.TouchExtraPadding, x, y);
	}

	default void setIndentSpacing(float value) {
		setStyleVar(ImGuiStyleVar.IndentSpacing, value);
	}

	@Deprecated
	default void setColumnsMinSpacing(float value) {
		// setStyleVar(ImGuiStyleVar.ColumnsMinSpacing, value);
	}

	default void setScrollbarSize(float value) {
		setStyleVar(ImGuiStyleVar.ScrollbarSize, value);
	}

	default void setScrollbarRounding(float value) {
		setStyleVar(ImGuiStyleVar.ScrollbarRounding, value);
	}

	default void setGrabMinSize(float value) {
		setStyleVar(ImGuiStyleVar.GrabMinSize, value);
	}

	default void setGrabRounding(float value) {
		setStyleVar(ImGuiStyleVar.GrabRounding, value);
	}

	@Deprecated
	default void setLogSliderDeadzone(float value) {
		// setStyleVar(ImGuiStyleVar.LogSliderDeadzone, value);
	}

	default void setTabRounding(float value) {
		setStyleVar(ImGuiStyleVar.TabRounding, value);
	}

	@Deprecated
	default void setTabBorderSize(float value) {
		// setStyleVar(ImGuiStyleVar.TabBorderSize, value);
	}

	@Deprecated
	default void setTabMinWidthForCloseButton(float value) {
		// setStyleVar(ImGuiStyleVar.TabMinWidthForCloseButton, value);
	}

	@Deprecated
	default void setColorButtonPosition(int value) {
		// setStyleVar(ImGuiStyleVar.ColorButtonPosition, value);
	}

	default void setButtonTextAlign(float x, float y) {
		setStyleVar(ImGuiStyleVar.ButtonTextAlign, x, y);
	}

	default void setSelectableTextAlign(float x, float y) {
		setStyleVar(ImGuiStyleVar.SelectableTextAlign, x, y);
	}

	@Deprecated
	default void setDisplayWindowPadding(float x, float y) {
		// setStyleVar(ImGuiStyleVar.DisplayWindowPadding, value);
	}

	@Deprecated
	default void setDisplaySafeAreaPadding(float x, float y) {
		// setStyleVar(ImGuiStyleVar.DisplaySafeAreaPadding, value);
	}

	@Deprecated
	default void setMouseCursorScale(float value) {
		// setStyleVar(ImGuiStyleVar.MouseCursorScale, value);
	}

	@Deprecated
	default void setAntiAliasedLines(boolean value) {
		// setStyleVar(ImGuiStyleVar.AntiAliasedLines, value);
	}

	@Deprecated
	default void setAntiAliasedLinesUseTex(boolean value) {
		// setStyleVar(ImGuiStyleVar.AntiAliasedLinesUseTex, value);
	}

	@Deprecated
	default void setAntiAliasedFill(boolean value) {
		// setStyleVar(ImGuiStyleVar.AntiAliasedFill, value);
	}

	@Deprecated
	default void setCurveTessellationTol(float value) {
		// setStyleVar(ImGuiStyleVar.CurveTessellationTol, value);
	}

	@Deprecated
	default void setCircleTessellationMaxError(float value) {
		// setStyleVar(ImGuiStyleVar.CircleTessellationMaxError, value);
	}
}
