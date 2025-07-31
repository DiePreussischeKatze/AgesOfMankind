package org.src.components.ui.editor;

import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;
import org.src.components.map.DisplayMode;
import org.src.components.map.Map;
import org.src.components.ScenarioSaver;
import org.src.core.main.Window;

public final class EditorWindow {

	private final ScenarioSaver scenarioSaver;

	private final Editor editor;
	private final Map map;
	EditorWindow(final Editor editor, final Map map) {
		this.editor = editor;
		this.map = map;

		this.scenarioSaver = new ScenarioSaver(this.map);
		scenarioSaver.loadScenario();

		setStyle();
	}

	public void draw() {

		Window.uiBegin();

		ImGui.begin("Editor", ImGuiWindowFlags.NoMove);

		if (Window.getWidth() - 100 > 299) {
			ImGui.setWindowSize(new ImVec2(Math.clamp(ImGui.getWindowSizeX(), 300, Window.getWidth() - 100), Window.getHeight()));
		}

		ImGui.setWindowPos(0, 0);

		final ImInt mode = new ImInt(map.getDisplayMode().ordinal());
		final String[] modes = {
			"Terrain",
			"Population",
			"Elevation",
			"Political",
			"Ethnicity",
			"Demographic"
		};

		if (ImGui.combo("Map rendering mode", mode, modes)) {
			editor.changeMapDisplay(DisplayMode.values()[mode.get()]);
		}

		ImGui.separator();

		if (ImGui.selectable("Add provinces (q)", editor.isModeAddProvinces())) {
			editor.setMode(EEditorMode.ADD_PROVINCES);
		}

		if (ImGui.selectable("Edit provinces (e)", editor.isModeEditProvinces())) {
			editor.setMode(EEditorMode.EDIT_PROVINCES);
		}

		if (ImGui.selectable("Select province (l)", editor.isModeSelectProvinces())) {
			editor.setMode(EEditorMode.SELECT_PROVINCES);
		}

		if (ImGui.selectable("Paint provinces (p)", editor.isModePaintProvinces())) {
			editor.setMode(EEditorMode.PAINT_PROVINCES);
		}

		ImGui.separator();

		editor.drawModeUI();

		ImGui.separator();

		if (ImGui.checkbox("Draw current province filling (f)", editor.getDrawFill())) {
			editor.toggleDrawFill();
		}

		if (ImGui.checkbox("Draw current province points (g)", editor.getDrawPoints())) {
			editor.toggleDrawPoints();
		}

		if (ImGui.checkbox("Draw other provinces' points (h)", map.getDrawProvincePoints())) {
			map.toggleDrawProvincePoints();
		}

		if (ImGui.checkbox("Draw other provinces' fillings (j)", map.getDrawProvinceFillings())) {
			map.toggleDrawProvinceFillings();
		}

		ImGui.separator();

		if (ImGui.button("Save scenario")) {
			//PointerBuffer buffer = BufferUtils.createPointerBuffer(1);
			//NFD_OpenDialog(buffer, null, "C:\\");
			scenarioSaver.saveScenario();
		}

		if (ImGui.button("Load scenario")) {
			scenarioSaver.loadScenario();
		}

		ImGui.end();

	}

	static int randomizeValue(final int value) {
		return (int) ((float) (Math.random() - 0.5) * Editor.VALUE_RANDOMIZER_RANGE * value);
	}


	private static void setStyle() {
		final ImGuiStyle style = ImGui.getStyle();
		style.setCircleTessellationMaxError(5);
		style.setFrameRounding(12);
		style.setWindowPadding(20, 10);
		style.setFramePadding(20, 5);
		style.setItemSpacing(12, 8);
		style.setItemInnerSpacing(10, 4);
		style.setGrabMinSize(17);
		style.setScrollbarSize(8);
		style.setWindowBorderSize(0);
		style.setFrameBorderSize(1);
		style.setWindowTitleAlign(0.5f, 0.5f);
		style.setSelectableTextAlign(0.5f, 0.5f);
		style.setGrabRounding(12);

		style.setColor(ImGuiCol.Text, 0, 255, 0, 255);
		style.setColor(ImGuiCol.TextDisabled, 0, 150, 0, 255);
		style.setColor(ImGuiCol.WindowBg, 0, 0, 0, 255);
		style.setColor(ImGuiCol.Border, 0, 255, 0, 150);
		style.setColor(ImGuiCol.FrameBg, 0, 0, 0, 0);
		style.setColor(ImGuiCol.FrameBgHovered, 0, 100, 0, 80);
		style.setColor(ImGuiCol.FrameBgActive, 0, 150, 0, 150);
		style.setColor(ImGuiCol.TitleBgActive, 0, 100, 0, 255);
		style.setColor(ImGuiCol.TitleBg, 0, 0, 0, 255);
		style.setColor(ImGuiCol.CheckMark, 0, 255, 0, 255);
		style.setColor(ImGuiCol.ScrollbarBg, 0, 0, 0, 0);
		style.setColor(ImGuiCol.Button, 0, 0, 0, 255);
		style.setColor(ImGuiCol.ButtonHovered, 0, 255, 0, 60);
		style.setColor(ImGuiCol.ButtonActive, 0, 255, 0, 120);
		style.setColor(ImGuiCol.SliderGrab, 0, 255, 0, 100);
		style.setColor(ImGuiCol.SliderGrabActive, 0, 255, 0, 255);
		style.setColor(ImGuiCol.Separator, 0, 255, 0, 255);
		style.setColor(ImGuiCol.Header, 0, 255, 0, 100);
		style.setColor(ImGuiCol.HeaderHovered, 0, 255, 0, 50);
		style.setColor(ImGuiCol.HeaderActive, 0, 255, 0, 150);
		style.setColor(ImGuiCol.ScrollbarGrab, 0, 255, 0, 50);
		style.setColor(ImGuiCol.ScrollbarGrabHovered, 0, 255, 0, 100);
		style.setColor(ImGuiCol.ScrollbarGrabActive, 0, 255, 0, 150);
		style.setColor(ImGuiCol.TextSelectedBg, 0, 255, 0, 170);
		style.setColor(ImGuiCol.SeparatorActive, 0, 255, 0, 255);
		style.setColor(ImGuiCol.SeparatorHovered, 0, 255, 0, 255);
		// TODO: implement own resizing
		style.setColor(ImGuiCol.ResizeGrip, 0, 0, 0, 0);
		style.setColor(ImGuiCol.ResizeGripHovered, 0, 0, 0, 0);
		style.setColor(ImGuiCol.ResizeGripActive, 0, 0, 0, 0);
	}


}
