package org.src.components.ui.editor;

import imgui.ImGui;
import org.src.components.map.Map;
import org.src.components.province.Province;
import org.src.rendering.wrapper.Mesh;

import static org.src.components.ui.editor.EEditorMode.EDIT_PROVINCES;

public final class SelectProvincesMode extends EditorMode {
	private final Map map;

	private boolean shouldChangeToEditProvincesOnClick;

	private final GhostProvince ghostProvince;

	SelectProvincesMode(final Editor editor, final Map map) {
		super(editor);

		this.map = map;
		this.shouldChangeToEditProvincesOnClick = true;
		this.ghostProvince = new GhostProvince();

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
		final Province highlight = map.findProvinceUnderPoint(editor.getAdjustedPos());
		if (highlight == null) {
			ghostProvince.changeMesh(new Mesh(new byte[] {2, 3}));
			return;
		}

		ghostProvince.changeMesh(new Mesh(highlight.getVertices(), highlight.getIndices(), new byte[] { 2, 3}));
	}

	@Override
	public void dispose() {
		ghostProvince.dispose();
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

		map.setLendProvinceID(map.findProvinceIndexUnderPoint(editor.getAdjustedPos()));
		map.addProvinceToMesh(editor.getProvince());
		editor.setProvince(nullCheck);
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
		ghostProvince.draw();
	}

}
