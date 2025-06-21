package org.src.components.ui.editor;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import org.src.core.main.Window;

import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

public final class EditorWindow {

	private final ImGuiImplGl3 imGuiImplGl3;
	private final ImGuiImplGlfw imGuiImplGlfw;

	private ImGuiIO imGuiIO;


	private final Editor editor;
	public EditorWindow(final Editor editor) {
		this.editor = editor;

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
		if(ImGui.selectable("Paint provinces (p)", editor.getMode() == EditorMode.PAINT_PROVINCES)) {
			editor.setMode(EditorMode.PAINT_PROVINCES);
		}
		ImGui.separator();

		tryRenderingForAddProvincesMode();

		ImGui.separator();
		if (ImGui.checkbox("Draw current province filling", editor.getCurrentProvince().getDrawFill())) {
			editor.getCurrentProvince().setDrawFill(!editor.getCurrentProvince().getDrawFill());
		}

		if (ImGui.checkbox("Draw current province points", editor.getCurrentProvince().getDrawPoints())) {
			editor.getCurrentProvince().setDrawPoints(!editor.getCurrentProvince().getDrawPoints());
		}

		ImGui.separator();
		if (ImGui.colorPicker3("Change province color", editor.getCurrentProvince().getColor())) {
			editor.getCurrentProvince().updateColor();
		}

		ImGui.end();

		ImGui.render();
		this.imGuiImplGl3.renderDrawData(ImGui.getDrawData());
		ImGui.endFrame();
		glEnable(GL_MULTISAMPLE);
	}

	private void tryRenderingForAddProvincesMode() {
		if (editor.getMode() != EditorMode.ADD_PROVINCES) { return; }

		if (ImGui.button("Delete last point")) {
			editor.getCurrentProvince().deleteLastPoint();
		}

		ImGui.sameLine();
		ImGui.text("currentPivotID: " + editor.getCurrentProvince().getPivotAmount());

		if (ImGui.button("Clear province points")) {
			editor.getCurrentProvince().clearProvincePoints();
		}

		ImGui.sameLine();
		ImGui.text("currentProvinceID: ");

		if (ImGui.button("New province")) {
			editor.passProvince();
		}
	}

}
