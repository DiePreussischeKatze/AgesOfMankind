package org.src.components.ui.editor;

import imgui.ImGui;
import org.src.components.map.Map;
import org.src.components.province.Province;

import static org.src.components.ui.editor.EEditorMode.EDIT_PROVINCES;

public final class SelectProvincesMode extends EditorMode {
	private final Map map;

	private boolean shouldChangeToEditProvincesOnClick;

	SelectProvincesMode(final Editor editor, final Map map) {
		super(editor);

		this.map = map;
		this.shouldChangeToEditProvincesOnClick = true;

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
		editor.deselectPoint(); // for safety measures

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

		if (shouldChangeToEditProvincesOnClick) {
			editor.setMode(EDIT_PROVINCES);
		}
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
		if (ImGui.checkbox("Change to edit provinces", shouldChangeToEditProvincesOnClick)) {
			shouldChangeToEditProvincesOnClick = !shouldChangeToEditProvincesOnClick;
		}

	}

	@Override
	public void update(final double deltaTime) {

	}

	@Override
	public void draw() {

	}

}
