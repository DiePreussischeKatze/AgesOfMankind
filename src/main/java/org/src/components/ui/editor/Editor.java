package org.src.components.ui.editor;

import org.joml.Vector2f;
import org.src.components.Camera;
import org.src.components.Map;
import org.src.components.province.Province;
import org.src.core.callbacks.*;
import org.src.core.helper.Component;
import org.src.core.helper.Consts;
import org.src.core.helper.ShaderID;
import org.src.core.main.Window;
import org.src.core.managers.InputManager;
import org.src.core.managers.ShaderManager;

import static org.lwjgl.glfw.GLFW.*;
import static org.src.core.helper.Helper.isInImGuiWindow;

public final class Editor extends Component {

	private final EditorWindow editorWindow;
	private EditorCursor editorCursor;

	private Camera camera;
	private Map map;

	private EditorMode mode;
	private Province currentProvince;

	private Vector2f adjustedPosition;

	private boolean gridAlignmentEnabled;
	private boolean draggingProvincePoint;

	private int heldProvincePointIndex;

	private final MouseMoveCallback moveCallback = () -> {
		adjustedPosition.x = -camera.getPosition().x + camera.getAccumulatedDragDistance().x + (InputManager.getCenteredMouseX() / Window.getWidth()) / camera.getPosition().z * 2;
		adjustedPosition.y = -camera.getPosition().y - camera.getAccumulatedDragDistance().y - (InputManager.getCenteredMouseY() / Window.getWidth()) / camera.getPosition().z * 2;

		switch (mode) {
			case EditorMode.ADD_PROVINCES:
				if (!gridAlignmentEnabled) {
					editorCursor.updatePosition(
							adjustedPosition.x,
							adjustedPosition.y
					);
				} else {
					editorCursor.updatePosition(
							(float) Math.floor(adjustedPosition.x * 1000) / 1000 + 0.0005f, // align 'em to the mouse cursor
							(float) Math.floor(adjustedPosition.y * 1000) / 1000 + 0.0005f
					);
				}
				break;
			case EditorMode.EDIT_PROVINCES:
				if (heldProvincePointIndex == -1 || !draggingProvincePoint) { return; }

				// modify the mesh
				// TODO: use drag delta to avoid a small jitter when starting to shift
				if (gridAlignmentEnabled) {
					currentProvince.getPointsPositions()[heldProvincePointIndex * Consts.POINT_POSITION_STRIDE] =
							currentProvince.getVertices()[heldProvincePointIndex * currentProvince.getMeshOffsetSum()] = (float) Math.floor(adjustedPosition.x * 1000) / 1000 + 0.0005f;
					currentProvince.getPointsPositions()[heldProvincePointIndex * Consts.POINT_POSITION_STRIDE + 1] =
							currentProvince.getVertices()[heldProvincePointIndex * currentProvince.getMeshOffsetSum() + 1] = (float) Math.floor(adjustedPosition.y * 1000) / 1000 + 0.0005f;
				} else {
					currentProvince.getPointsPositions()[heldProvincePointIndex * Consts.POINT_POSITION_STRIDE] =
							currentProvince.getVertices()[heldProvincePointIndex * currentProvince.getMeshOffsetSum()] = adjustedPosition.x;
					currentProvince.getPointsPositions()[heldProvincePointIndex * Consts.POINT_POSITION_STRIDE + 1] =
							currentProvince.getVertices()[heldProvincePointIndex * currentProvince.getMeshOffsetSum() + 1] = adjustedPosition.y;
				}

				currentProvince.refreshMesh();
				break;
		}
	};

	private final MouseLeftPressCallback leftCallback = () -> {
		if (isInImGuiWindow()) { return; }

		switch (mode) {
			case EditorMode.ADD_PROVINCES:
				if (!gridAlignmentEnabled) {
					currentProvince.addPoint(
							adjustedPosition.x,
							adjustedPosition.y
					);
				} else {
					currentProvince.addPoint(
							(float) Math.floor(adjustedPosition.x * 1000) / 1000 + 0.0005f, // align 'em to the mouse cursor
							(float) Math.floor(adjustedPosition.y * 1000) / 1000 + 0.0005f
					);
				}
				break;
			case EditorMode.EDIT_PROVINCES:
				draggingProvincePoint = true;

				heldProvincePointIndex = currentProvince.isInAnyPoint(adjustedPosition.x, adjustedPosition.y);
				break;
		}
	};

	private final MouseLeftReleaseCallback leftReleaseCallback = () -> {
		if (mode != EditorMode.EDIT_PROVINCES) { return; }

		draggingProvincePoint = false;
	};

	private final KeyPressCallback pressCallback = (final long window, final int key, final int action, final int mods) -> {
		switch (key) {
			case GLFW_KEY_Q:
				setMode(EditorMode.ADD_PROVINCES);
				break;
			case GLFW_KEY_E:
				setMode(EditorMode.EDIT_PROVINCES);
				break;
			case GLFW_KEY_P:
				setMode(EditorMode.PAINT_PROVINCES);
				break;
			case GLFW_KEY_F:
				currentProvince.setDrawFill(!currentProvince.getDrawFill());
				break;
			case GLFW_KEY_G:
				currentProvince.setDrawPoints(!currentProvince.getDrawPoints());
				break;
			case GLFW_KEY_J:
				map.setDrawProvinceFillings(!map.getDrawProvinceFillings());
				break;
			case GLFW_KEY_H:
				map.setDrawProvincePoints(!map.getDrawProvincePoints());
				break;
		}

		if (mode == EditorMode.ADD_PROVINCES) {
			switch (key) {
				case GLFW_KEY_Z:
					currentProvince.deleteLastPoint();
					break;
				case GLFW_KEY_N:
					newProvince();
					break;
			}
		} else if (mode == EditorMode.EDIT_PROVINCES) {
			switch (key) {
				case GLFW_KEY_DELETE:
					currentProvince.deletePoint(heldProvincePointIndex);
					heldProvincePointIndex = -1;
					break;
			}
		}
	};

	public Editor(final Camera camera, final Map map) {
		this.camera = camera;
		this.map = map;

		this.editorWindow = new EditorWindow(this, map);
		this.editorCursor = new EditorCursor();

		this.adjustedPosition = new Vector2f();

		currentProvince = map.createProvince();

		mode = EditorMode.ADD_PROVINCES;

		this.gridAlignmentEnabled = true;
		this.draggingProvincePoint = false;

		this.heldProvincePointIndex = -1;

		InputManager.addMouseLeftPressCallback(leftCallback);
		InputManager.addMouseMoveCallback(moveCallback);
		InputManager.addKeyPressCallback(pressCallback);
		InputManager.addMouseLeftReleaseCallback(leftReleaseCallback);
	}

	@Override
	public void draw() {
		currentProvince.drawAlone();

		if (mode == EditorMode.ADD_PROVINCES) {
			editorCursor.draw();
		} else if (mode == EditorMode.EDIT_PROVINCES) {
			drawForEditMode();
			drawSelectedPoint();
		}
		editorWindow.draw();
	}

	private void drawForEditMode() {
		int i = currentProvince.isInAnyPoint(adjustedPosition.x, adjustedPosition.y);

		if (i == -1 || i == heldProvincePointIndex) { return; }

		ShaderManager.get(ShaderID.EDITOR).bind();
		ShaderManager.get(ShaderID.EDITOR).setFloat2(
				"offset",
				currentProvince.getPointsPositions()[i * Consts.POINT_POSITION_STRIDE],
				currentProvince.getPointsPositions()[i * Consts.POINT_POSITION_STRIDE + 1]
		);
		ShaderManager.get(ShaderID.EDITOR).setFloat3("color", 1f, 0.8f, 0.2f);
		editorCursor.getBoxMesh().draw();
	}

	private void drawSelectedPoint() {
		if (heldProvincePointIndex == -1 || currentProvince.getPointsPositions().length == 0) { return; }

		ShaderManager.get(ShaderID.EDITOR).bind();
		ShaderManager.get(ShaderID.EDITOR).setFloat2(
				"offset",
				currentProvince.getPointsPositions()[heldProvincePointIndex * Consts.POINT_POSITION_STRIDE],
				currentProvince.getPointsPositions()[heldProvincePointIndex * Consts.POINT_POSITION_STRIDE + 1]
		);
		ShaderManager.get(ShaderID.EDITOR).setFloat3("color", 0.8f, 0.6f, 0.1f);
		editorCursor.getBoxMesh().draw();
	}

	public void newProvince() {
		// we obviously don't want to make a new province while the current is empty
		if (currentProvince.getIndices().length == 0) { return; }
		map.addProvinceToMesh(currentProvince);
		currentProvince = map.createProvince();
	}

	public EditorMode getMode() {
		return mode;
	}

	public Province getCurrentProvince() {
		return currentProvince;
	}

	public boolean getGridAlignment() {
		return gridAlignmentEnabled;
	}

	public void setGridAlignment(final boolean value) {
		gridAlignmentEnabled = value;
	}

	public void setMode(EditorMode mode) {
		this.mode = mode;
	}

	public int getHeldProvincePointIndex() {
		return heldProvincePointIndex;
	}

	public void setHeldProvincePointIndex(int heldProvincePointIndex) {
		this.heldProvincePointIndex = heldProvincePointIndex;
	}

	@Override
	public void update(double deltaTime) {

	}

	@Override
	public void dispose() {}

}
