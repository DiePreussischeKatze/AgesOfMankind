package org.src.components.ui.editor;

import imgui.ImGui;
import imgui.type.ImInt;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;
import static org.src.components.ui.editor.EditorWindow.inputInt;
import static org.src.components.ui.editor.EditorWindow.randomizeValue;
import static org.src.core.helper.Consts.POINT_POS_STRIDE;

public final class EditProvincesMode extends EditorMode {
	private boolean draggingPoint;
	private boolean usedSelection;

	private int[] selectedPointsIndices;

	EditProvincesMode(Editor editor) {
		super(editor);

		draggingPoint = false;
		usedSelection = false;

		selectedPointsIndices = new int[0];

		editor.getSelection().setEnabled(true);
	}

	@Override
	public void keyPressAction(int key) {
		switch (key) {
			case GLFW_KEY_X:
				if (!editor.isAnyPointSelected()) {
					return;
				}
				editor.deleteProvincePoint(editor.getHeldPointIndex());
				editor.deselectPoint();
				break;
			case GLFW_KEY_C:
				if (!editor.isAnyPointSelected()) {
					return;
				}
				editor.getProvince().insertPointBackwards(editor.getHeldPointIndex());
				editor.deselectPoint();
				break;
			case GLFW_KEY_DELETE:
				deleteAllSelectedPoints();
				break;
			case GLFW_KEY_V:
				editor.setValueRandomizerEnabled(!editor.isValueRandomizerEnabled());
				break;
		}
	}

	@Override
	public void keyReleaseAction(int key) {

	}

	@Override
	public void mouseMovedAction() {
		if (editor.isAnyPointSelected() && draggingPoint) {
			dragProvincePoint();
		}

		if (editor.getSelection().get().getWidth() > 0 && editor.getSelection().get().getHeight() > 0) {
			usedSelection = true;
		}
	}

	@Override
	public void mouseLeftPressedAction() {
		editor.setHeldPointIndex(editor.getProvince().isInAnyPoint(editor.getAdjustedPos().x,
				editor.getAdjustedPos().y));

		if (editor.isAnyPointSelected()) {
			draggingPoint = true;
			editor.getSelection().setEnabled(false);
		}

	}

	@Override
	public void mouseLeftReleasedAction() {
		if (draggingPoint) {
			editor.getSelection().setEnabled(true);
			editor.getSelection().clear();
		}

		if (usedSelection) {
			selectedPointsIndices = editor.getProvince()
					.getIntersectedPointIndices(editor.getSelection().get());
			usedSelection = false;
		} else {
			selectedPointsIndices = new int[0]; // to be sure there aren't gonna be any crashes
		}

		draggingPoint = false;
	}

	@Override
	public void mouseRightPressedAction() {

	}

	@Override
	public void mouseRightReleasedAction() {

	}

	@Override
	public void renderGui() {
		if (ImGui.button("Delete selected point (x)") && editor.isAnyPointSelected()) {
			editor.getProvince().deletePoint(editor.getHeldPointIndex());
			editor.deselectPoint();
		}

		if (ImGui.button("Add new point (c)")) {
			editor.getProvince().insertPointBackwards(editor.getHeldPointIndex());
			editor.deselectPoint();
		}

		if (ImGui.button("Delete all selected points (DEL)")) {
			deleteAllSelectedPoints();
		}
		// add later for civilisation
		//if (ImGui.colorPicker3("Color", editor.getProvince().getColor())) {
		//	editor.getProvince().updateColor();
		//}

		final ImInt type = new ImInt(editor.getProvince().type.ordinal());
		final String[] types = { "Deep sea", "Shallow sea", "Costal sea", "Bog", "Lowlands", "Highlands", "Mountains" };
		if (ImGui.combo("Province type", type, types)) {
			editor.getProvince().setType(type.get());
			System.out.println(editor.getProvince().type);
		}

		ImGui.inputText("Name", editor.getProvince().name);

		editor.getProvince().populationCount = inputInt("Population count", editor.getProvince().populationCount);

		if (ImGui.button("Randomize values")) {
			editor.getProvince().populationCount += randomizeValue(editor.getProvince().populationCount);
		}
	}

	@Override
	public void update(final double deltaTime) {

	}

	@Override
	public void draw() {
		final int i = editor.getProvince().isInAnyPoint(editor.getAdjustedPos().x, editor.getAdjustedPos().y);

		if (i == -1 || i == editor.getHeldPointIndex()) { return; }

		ShaderManager.get(ShaderID.EDITOR).bind();
		ShaderManager.get(ShaderID.EDITOR).setFloat2(
				"offset",
				editor.getProvince().getPointsPoses()[i * POINT_POS_STRIDE],
				editor.getProvince().getPointsPoses()[i * POINT_POS_STRIDE + 1]
		);
		ShaderManager.get(ShaderID.EDITOR).setFloat3("color", 1f, 0.8f, 0.2f);
		editor.getCursor().getBoxMesh().draw();

		if (!editor.isAnyPointSelected() || editor.getProvince().getPointsPoses().length == 0) { return; }

		// TODO: Find the cause
		if (editor.getHeldPointIndex() > editor.getProvince().getPointsPoses().length || editor.getHeldPointIndex() < 0) {
			System.err.println("A crash was prevented!");
			return;
		}

		ShaderManager.get(ShaderID.EDITOR).bind();
		ShaderManager.get(ShaderID.EDITOR).setFloat2(
				"offset",
				editor.getProvince().getPointsPoses()[editor.getHeldPointIndex() * POINT_POS_STRIDE],
				editor.getProvince().getPointsPoses()[editor.getHeldPointIndex() * POINT_POS_STRIDE + 1]
		);
		ShaderManager.get(ShaderID.EDITOR).setFloat3("color", 0.8f, 0.6f, 0.1f);
		editor.getCursor().getBoxMesh().draw();
	}

	private void dragProvincePoint() {
		if (editor.isGridAlignmentEnabled()) {
			editor.getProvince().getPointsPoses()[editor.getHeldPointIndex() * POINT_POS_STRIDE] = editor.getProvince().getVertices()[editor.getHeldPointIndex() * editor.getProvince().getVertexStride()] = (float) Math.floor(editor.getAdjustedPos().x * 500) / 500 + 0.001f;
			editor.getProvince().getPointsPoses()[editor.getHeldPointIndex() * POINT_POS_STRIDE + 1] = editor.getProvince().getVertices()[editor.getHeldPointIndex() * editor.getProvince().getVertexStride() + 1] = (float) Math.floor(editor.getAdjustedPos().y * 500) / 500 + 0.001f;
		} else {
			editor.getProvince().getPointsPoses()[editor.getHeldPointIndex() * POINT_POS_STRIDE] = editor.getProvince().getVertices()[editor.getHeldPointIndex() * editor.getProvince().getVertexStride()] = editor.getAdjustedPos().x;
			editor.getProvince().getPointsPoses()[editor.getHeldPointIndex() * POINT_POS_STRIDE + 1] = editor.getProvince().getVertices()[editor.getHeldPointIndex() * editor.getProvince().getVertexStride() + 1] = editor.getAdjustedPos().y;
		}

		editor.getProvince().refreshMesh();
		editor.lookForNeighbors();
	}

	private void deleteAllSelectedPoints() {
		for (int i = 0; i < selectedPointsIndices.length; i += POINT_POS_STRIDE) {
			editor.getProvince().deletePointWithoutRefresh(selectedPointsIndices[i] / 2);

			// we have to offset the indices of the array as we have modified the array
			// itself
			for (int j = i + POINT_POS_STRIDE; j < selectedPointsIndices.length; j += POINT_POS_STRIDE) {
				// no need to update the odd indices as they come in x;y pairs (might actually
				// only wanna store the actual ids of the points)
				selectedPointsIndices[j] -= POINT_POS_STRIDE;
			}
		}
		selectedPointsIndices = new int[0];
		editor.getProvince().refreshMaxPoints();
		editor.getProvince().refreshMesh();
		editor.lookForNeighbors();
	}

}
