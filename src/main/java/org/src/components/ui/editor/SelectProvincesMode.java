package org.src.components.ui.editor;

import imgui.ImGui;
import org.src.components.map.Map;
import org.src.components.province.Province;

import static org.src.components.ui.editor.EditorWindow.inputInt;
import static org.src.components.ui.editor.EditorWindow.randomizeValue;

public final class SelectProvincesMode extends EditorMode {
	private final Map map;

	SelectProvincesMode(final Editor editor, final Map map) {
		super(editor);

		this.map = map;

		// kinda ironic
		editor.getSelection().setEnabled(false);
	}

	@Override
	public void keyPressAction(int key) {

	}

	@Override
	public void keyReleaseAction(int key) {

	}

	@Override
	public void mouseMovedAction() {

	}

	@Override
	public void mouseLeftPressedAction() {
		editor.setHeldPointIndex(-1);
		final Province nullCheck = map.findProvinceUnderPoint(editor.getAdjustedPos());
		if (nullCheck == null) {
			// de-select the province
			map.addProvinceToMesh(editor.getProvince());
			editor.rawCreateProvince();
			return;
		}
		// trying to remove a province that is being edited WILL cause trouble with the indexes
		if (editor.getProvince() == nullCheck) {
			return;
		}

		map.setLendProvince(map.findProvinceIndexUnderPoint(editor.getAdjustedPos()));
		map.addProvinceToMesh(editor.getProvince());
		editor.setEditedProvince(nullCheck);
		map.takeProvinceFromMesh(nullCheck);
	}

	@Override
	public void mouseLeftReleasedAction() {

	}

	@Override
	public void mouseRightPressedAction() {

	}

	@Override
	public void mouseRightReleasedAction() {

	}

	@Override
	public void renderGui() {

		if (ImGui.colorPicker3("Color", editor.getProvince().getColor())) {
			editor.getProvince().updateColor();
		}

		ImGui.inputText("Name", editor.getProvince().name);

		editor.getProvince().populationCount = inputInt("Population count", editor.getProvince().populationCount);

		if (ImGui.button("Randomize values")) {
			editor.getProvince().populationCount += randomizeValue(editor.getProvince().populationCount);
		}
	}

}
