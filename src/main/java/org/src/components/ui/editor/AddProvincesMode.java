package org.src.components.ui.editor;

import imgui.ImGui;
import org.src.components.map.Map;
import org.src.components.province.Province;
import org.src.core.helper.Rect2D;

import static org.lwjgl.glfw.GLFW.*;

public final class AddProvincesMode extends EditorMode {

	private final Rect2D magnetHitbox;

	private final float MAGNET_SIZE = 0.001f;
	private final int GRID_ALIGN = 500;

	private final Map map;

	public AddProvincesMode(final Editor editor, final Map map) {
		super(editor);
		this.map = map;

		this.magnetHitbox = new Rect2D();

		editor.getSelection().setEnabled(false);
	}

	@Override
	public void keyPressAction(final int key) {
		switch (key) {
			case GLFW_KEY_Z:
				editor.getProvince().deleteLastPoint();
				break;
			case GLFW_KEY_N:
				editor.newProvince();
				break;
			case GLFW_KEY_T:
				editor.setEnabledMagnet(!editor.getMagnetEnabled());
				break;
		}
	}

	@Override
	public void keyReleaseAction(final int key) {

	}

	@Override
	public void mouseMovedAction() {
		if (!editor.isGridAlignmentEnabled()) {
			editor.updateCursorPos(editor.getAdjustedPos());
		} else {
			editor.updateCursorPos(
					(float) Math.floor(editor.getAdjustedPos().x * GRID_ALIGN) / GRID_ALIGN + 0.001f, // align 'em to the mouse cursor
					(float) Math.floor(editor.getAdjustedPos().y * GRID_ALIGN) / GRID_ALIGN + 0.001f
			);
		}

		if (editor.getMagnetEnabled()) {
			magnetHitbox.setDimensions(editor.getAdjustedPos().x - MAGNET_SIZE, editor.getAdjustedPos().y - MAGNET_SIZE, MAGNET_SIZE * 2, MAGNET_SIZE * 2);

			for (final Province province: map.getProvinces()) {
				if (province == editor.getProvince()) { continue; }

				final int index = province.getFirstIntersectedPointIndex(magnetHitbox);
				if (index != -1) {
					editor.updateCursorPos(province.getPointsPoses()[index], province.getPointsPoses()[index + 1]);
				}
			}
		}

	}

	@Override
	public void mouseLeftPressedAction() {
		editor.deselectPoint();
		addPointToProvince();
		editor.lookForNeighbors();
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
		ImGui.text("province length: " + editor.getProvince().getPivotAmount());
		ImGui.text("current province ID: " + map.getLendProvinceId());

		ImGui.separator();

		if (ImGui.button("Delete last point (z)")) {
			editor.getProvince().deleteLastPoint();
		}

		if (ImGui.button("Clear province points")) {
			editor.getProvince().clearProvincePoints();
			editor.deselectPoint();
		}

		if (ImGui.button("New province (n)")) {
			editor.newProvince();
		}

		ImGui.separator();

		if (ImGui.checkbox("Toggle province magnet (t)", editor.getMagnetEnabled())) {
			editor.setEnabledMagnet(!editor.getMagnetEnabled());
		}

		if (ImGui.checkbox("Toggle grid alignment", editor.isGridAlignmentEnabled())) {
			editor.toggleGridAlignment();
		}
	}

	@Override
	public void draw() {
		editor.getCursor().draw();
	}

	@Override
	public void update(final double deltaTime) {

	}

	@Override
	public void dispose() {

	}

	private void addPointToProvince() {
		if (!editor.isGridAlignmentEnabled()) {
			editor.getProvince().addPoint(
					editor.getCursor().getPosition().x, editor.getCursor().getPosition().y
			);

			return;
		}
		editor.getProvince().addPoint(
				(float) Math.floor(editor.getCursor().getPosition().x * GRID_ALIGN) / GRID_ALIGN + 0.001f,
				(float) Math.floor(editor.getCursor().getPosition().y * GRID_ALIGN) / GRID_ALIGN + 0.001f
		);
	}

}
