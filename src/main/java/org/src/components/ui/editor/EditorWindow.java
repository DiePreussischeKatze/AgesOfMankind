package org.src.components.ui.editor;

import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.src.components.map.Map;
import org.src.components.ScenarioSaver;
import org.src.core.main.Window;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

// TODO: implement a good ui design
public final class EditorWindow {

	private final ScenarioSaver scenarioSaver;

	private final ImGuiImplGl3 imGuiImplGl3;
	private final ImGuiImplGlfw imGuiImplGlfw;

	private final ImGuiIO imGuiIO;

	private final Editor editor;
	private final Map map;
	public EditorWindow(final Editor editor, final Map map) {
		this.editor = editor;
		this.map = map;

		this.scenarioSaver = new ScenarioSaver(this.map);
		scenarioSaver.loadScenario();

		this.imGuiImplGl3 = new ImGuiImplGl3();
		this.imGuiImplGlfw = new ImGuiImplGlfw();

		ImGui.createContext();

		this.imGuiImplGlfw.init(Window.getId(), true);
		this.imGuiImplGl3.init();

		imGuiIO = ImGui.getIO();
		imGuiIO.getFonts().addFontDefault();


	}

	public void dispose() {
		this.imGuiImplGlfw.shutdown();
		this.imGuiImplGl3.shutdown();
		ImGui.destroyContext();
	}

	public void draw() {
		glDisable(GL_MULTISAMPLE);
		this.imGuiImplGl3.newFrame();
		this.imGuiImplGlfw.newFrame();

		ImGui.newFrame();
		ImGui.begin("Editor", ImGuiWindowFlags.NoMove);

		ImGui.getWindowDrawList().addCircle(new ImVec2(0.5f, 0.5f), 10f, 2000);
		ImGui.text("Modes:");

		if (ImGui.selectable("Add provinces (q)", editor.getMode() == EditorMode.ADD_PROVINCES)) {
			editor.setMode(EditorMode.ADD_PROVINCES);
		}

		if (ImGui.selectable("Edit provinces (e)", editor.getMode() == EditorMode.EDIT_PROVINCES)) {
			editor.setMode(EditorMode.EDIT_PROVINCES);
		}

		if (ImGui.selectable("Paint provinces (p)", editor.getMode() == EditorMode.PAINT_PROVINCES)) {
			editor.setMode(EditorMode.PAINT_PROVINCES);
		}

		if (ImGui.selectable("Select province (l)", editor.getMode() == EditorMode.SELECT_PROVINCES)) {
			editor.setMode(EditorMode.SELECT_PROVINCES);
		}

		ImGui.separator();

		tryRenderingForAddProvincesMode();
		tryRenderingForEditProvincesMode();

		ImGui.separator();

		if (ImGui.checkbox("Draw current province filling (f)", editor.getProvince().getDrawFill())) {
			editor.getProvince().toggleDrawFill();
		}

		if (ImGui.checkbox("Draw current province points (g)", editor.getProvince().getDrawPoints())) {
			editor.getProvince().toggleDrawPoints();
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

		ImGui.render();
		this.imGuiImplGl3.renderDrawData(ImGui.getDrawData());
		ImGui.endFrame();
		glEnable(GL_MULTISAMPLE);
	}

	private void tryRenderingForAddProvincesMode() {
		if (editor.getMode() != EditorMode.ADD_PROVINCES) { return; }

		ImGui.text("province length: " + editor.getProvince().getPivotAmount());
		ImGui.text("current province ID: " + map.getLendProvinceId());

		ImGui.separator();

		if (ImGui.button("Delete last point (z)")) {
			editor.getProvince().deleteLastPoint();
		}

		if (ImGui.button("Clear province points")) {
			editor.getProvince().clearProvincePoints();
			editor.setHeldPointIndex(-1);
		}

		if (ImGui.button("New province (n)")) {
			editor.newProvince();
		}

		ImGui.separator();

		if (ImGui.checkbox("Toggle province magnet (t)", editor.getEnabledMagnet())) {
			editor.setEnabledMagnet(!editor.getEnabledMagnet());
		}

		if (ImGui.checkbox("Toggle grid alignment", editor.getGridAlignment())) {
			editor.toggleGridAlignment();
		}

	}

	private void tryRenderingForEditProvincesMode() {
		if (editor.getMode() != EditorMode.EDIT_PROVINCES) { return; }

		if (ImGui.button("Delete selected point (x)") && editor.isAnyPointSelected()) {
			editor.getProvince().deletePoint(editor.getHeldPointIndex());
			editor.setHeldPointIndex(-1);
		}

		if (ImGui.button("Add new point (c)")) {
			editor.getProvince().insertPointBackwards(editor.getHeldPointIndex());
			editor.setHeldPointIndex(-1);
		}

		if (ImGui.button("Delete all selected points (DEL)")) {
			editor.deleteAllSelectedPoints();
		}

		if (ImGui.colorPicker3("Change province color", editor.getProvince().getColor())) {
			editor.getProvince().updateColor();
		}

	}

}
