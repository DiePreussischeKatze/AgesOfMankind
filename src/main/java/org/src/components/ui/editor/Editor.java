package org.src.components.ui.editor;

import org.joml.Vector2f;
import org.src.components.Camera;
import org.src.components.Map;
import org.src.components.Selection;
import org.src.components.province.Province;
import org.src.core.callbacks.*;
import org.src.core.helper.Component;
import org.src.core.helper.ShaderID;
import org.src.core.main.Window;
import org.src.core.managers.InputManager;
import org.src.core.managers.ShaderManager;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.src.components.ui.editor.EditorMode.*;
import static org.src.core.helper.Consts.POINT_POS_STRIDE;
import static org.src.core.helper.Helper.isInImGuiWindow;

public final class Editor extends Component {
	// TODO: divide the editor into classes containing each mode
	private final EditorWindow editorWindow;
	private EditorCursor editorCursor;

	private Camera camera;
	private Map map;
	private Selection selection;

	private EditorMode mode;
	private Province editedProvince;

	private Vector2f adjustedPos;

	private boolean gridAlignmentEnabled;
	private boolean draggingPoint;

	private int heldPointIndex;

	private int[] selectedPointsIndices;

	private final MouseMoveCallback moveCallback = () -> {
		adjustedPos.x = -camera.getPos().x + camera.getAccumulatedDragDist().x + (InputManager.getCenteredMouseX() / Window.getWidth()) / camera.getPos().z * 2;
		adjustedPos.y = -camera.getPos().y - camera.getAccumulatedDragDist().y - (InputManager.getCenteredMouseY() / Window.getWidth()) / camera.getPos().z * 2;

		//System.out.println(currentProvince.isInAnyPoint(adjustedPosition.x, adjustedPosition.y));
		//System.out.println(editedProvince.isInProvince(adjustedPos));

		switch (mode) {
			case ADD_PROVINCES:
				if (!gridAlignmentEnabled) {
					editorCursor.updatePos(adjustedPos);
				} else {
					editorCursor.updatePos(
							(float) Math.floor(adjustedPos.x * 500) / 500 + 0.001f, // align 'em to the mouse cursor
							(float) Math.floor(adjustedPos.y * 500) / 500 + 0.001f
					);
				}
				break;
			case EDIT_PROVINCES:
				if (!isAnyPointSelected() || !draggingPoint) { return; }

				// modify the mesh
				// TODO: use drag delta to avoid a small jitter when starting to shift
				if (gridAlignmentEnabled) {
					editedProvince.getPointsPoses()[heldPointIndex * POINT_POS_STRIDE] =
							editedProvince.getVertices()[heldPointIndex * editedProvince.getMeshStride()] = (float) Math.floor(adjustedPos.x * 500) / 500 + 0.001f;
					editedProvince.getPointsPoses()[heldPointIndex * POINT_POS_STRIDE + 1] =
							editedProvince.getVertices()[heldPointIndex * editedProvince.getMeshStride() + 1] = (float) Math.floor(adjustedPos.y * 500) / 500 + 0.001f;
				} else {
					editedProvince.getPointsPoses()[heldPointIndex * POINT_POS_STRIDE] =
							editedProvince.getVertices()[heldPointIndex * editedProvince.getMeshStride()] = adjustedPos.x;
					editedProvince.getPointsPoses()[heldPointIndex * POINT_POS_STRIDE + 1] =
							editedProvince.getVertices()[heldPointIndex * editedProvince.getMeshStride() + 1] = adjustedPos.y;
				}

				editedProvince.refreshMesh();
				break;
		}
	};

	private final MouseLeftPressCallback leftCallback = () -> {
		if (isInImGuiWindow()) { return; }

		switch (mode) {
			case ADD_PROVINCES:
				heldPointIndex = -1;

				if (!gridAlignmentEnabled) {
					editedProvince.addPoint(
							adjustedPos.x,
							adjustedPos.y
					);
				} else {
					editedProvince.addPoint(
							(float) Math.floor(adjustedPos.x * 500) / 500 + 0.001f, // align 'em to the mouse cursor
							(float) Math.floor(adjustedPos.y * 500) / 500 + 0.001f
					);
				}
				break;
			case EDIT_PROVINCES:
				heldPointIndex = editedProvince.isInAnyPoint(adjustedPos.x, adjustedPos.y);
				if (heldPointIndex != -1) {
					draggingPoint = true;
					selection.setEnabled(false);
				}
				break;
			case SELECT_PROVINCES:
				heldPointIndex = -1;
				final Province nullCheck = map.findProvinceUnderPoint(adjustedPos);
				if (nullCheck == null) {
					break;
				}
				// trying to remove a province that is being edited WILL cause trouble with the indexes
				if (editedProvince == nullCheck) {
					break;
				}

				map.setLendProvince(map.findProvinceIndexUnderPoint(adjustedPos));
				map.addProvinceToMesh(editedProvince);
				editedProvince = nullCheck;
				map.takeProvinceOut(nullCheck);
				break;
		}
	};

	private final MouseLeftReleaseCallback leftReleaseCallback = () -> {
		if (draggingPoint) {
			selection.setEnabled(true);
			selection.clear();
		}

		if (mode == EDIT_PROVINCES) {
			draggingPoint = false;
		}

	};

	private final KeyPressCallback pressCallback = (final long window, final int key, final int action, final int mods) -> {
		switch (key) {
			case GLFW_KEY_Q:
				setMode(ADD_PROVINCES);
				break;
			case GLFW_KEY_E:
				setMode(EDIT_PROVINCES);
				break;
			case GLFW_KEY_P:
				setMode(PAINT_PROVINCES);
				break;
			case GLFW_KEY_F:
				editedProvince.setDrawFill(!editedProvince.getDrawFill());
				break;
			case GLFW_KEY_G:
				editedProvince.setDrawPoints(!editedProvince.getDrawPoints());
				break;
			case GLFW_KEY_J:
				map.setDrawProvinceFillings(!map.getDrawProvinceFillings());
				break;
			case GLFW_KEY_H:
				map.setDrawProvincePoints(!map.getDrawProvincePoints());
				break;
			case GLFW_KEY_L:
				setMode(SELECT_PROVINCES);
				break;
		}

		if (mode == ADD_PROVINCES) {
			switch (key) {
				case GLFW_KEY_Z:
					editedProvince.deleteLastPoint();
					break;
				case GLFW_KEY_N:
					newProvince();
					break;
			}
		} else if (mode == EDIT_PROVINCES) {
			switch (key) {
				case GLFW_KEY_X:
					if (!isAnyPointSelected()) { return; }
					editedProvince.deletePoint(heldPointIndex);
					heldPointIndex = -1;
					break;
				case GLFW_KEY_C:
					if (!isAnyPointSelected()) { return; }
					editedProvince.insertPointBackwards(heldPointIndex);
					heldPointIndex = -1;
			}
		}
	};

	public Editor(final Camera camera, final Map map, final Selection selection) {
		this.camera = camera;
		this.map = map;
		this.selection = selection;

		this.editorWindow = new EditorWindow(this, map);
		this.editorCursor = new EditorCursor();

		this.adjustedPos = new Vector2f();

		editedProvince = map.createProvince();

		mode = ADD_PROVINCES;

		this.gridAlignmentEnabled = true;
		this.draggingPoint = false;

		this.heldPointIndex = -1;

		InputManager.addMouseLeftPressCallback(leftCallback);
		InputManager.addMouseMoveCallback(moveCallback);
		InputManager.addKeyPressCallback(pressCallback);
		InputManager.addMouseLeftReleaseCallback(leftReleaseCallback);
	}

	@Override
	public void draw() {
		editedProvince.drawAlone();

		if (mode == ADD_PROVINCES) {
			editorCursor.draw();
		} else if (mode == EDIT_PROVINCES) {
			drawForEditMode();
			drawSelectedPoint();
		}
		editorWindow.draw();
	}

	private void drawForEditMode() {
		final int i = editedProvince.isInAnyPoint(adjustedPos.x, adjustedPos.y);

		if (i == -1 || i == heldPointIndex) { return; }

		ShaderManager.get(ShaderID.EDITOR).bind();
		ShaderManager.get(ShaderID.EDITOR).setFloat2(
				"offset",
				editedProvince.getPointsPoses()[i * POINT_POS_STRIDE],
				editedProvince.getPointsPoses()[i * POINT_POS_STRIDE + 1]
		);
		ShaderManager.get(ShaderID.EDITOR).setFloat3("color", 1f, 0.8f, 0.2f);
		editorCursor.getBoxMesh().draw();
	}

	private void drawSelectedPoint() {
		if (!isAnyPointSelected() || editedProvince.getPointsPoses().length == 0) { return; }

		// TODO: Find the cause
		if (heldPointIndex > editedProvince.getPointsPoses().length || heldPointIndex < 0) { return; } // this was causing too many crashes

		ShaderManager.get(ShaderID.EDITOR).bind();
		ShaderManager.get(ShaderID.EDITOR).setFloat2(
				"offset",
				editedProvince.getPointsPoses()[heldPointIndex * POINT_POS_STRIDE],
				editedProvince.getPointsPoses()[heldPointIndex * POINT_POS_STRIDE + 1]
		);
		ShaderManager.get(ShaderID.EDITOR).setFloat3("color", 0.8f, 0.6f, 0.1f);
		editorCursor.getBoxMesh().draw();
	}

	public void newProvince() {
		// we obviously don't want to make a new province while the current is empty
		if (editedProvince.getIndices().length == 0) { return; }
		map.addProvinceToMesh(editedProvince);
		editedProvince = map.createProvince();
	}

	public EditorMode getMode() {
		return mode;
	}

	public Province getProvince() {
		return editedProvince;
	}

	public boolean getGridAlignment() {
		return gridAlignmentEnabled;
	}

	public void setGridAlignment(final boolean value) {
		gridAlignmentEnabled = value;
	}

	public void toggleGridAlignment() {
		gridAlignmentEnabled = !gridAlignmentEnabled;
	}

	public void setMode(EditorMode mode) {
		selection.setEnabled(mode == EDIT_PROVINCES);
		this.mode = mode;
	}

	public int getHeldPointIndex() {
		return heldPointIndex;
	}

	public void setHeldPointIndex(int heldPointIndex) {
		this.heldPointIndex = heldPointIndex;
	}

	public boolean isAnyPointSelected() {
		return heldPointIndex != -1;
	}

	@Override
	public void update(double deltaTime) {

	}

	@Override
	public void dispose() {}

}
