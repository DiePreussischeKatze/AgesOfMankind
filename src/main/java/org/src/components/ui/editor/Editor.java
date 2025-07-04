package org.src.components.ui.editor;

import org.joml.Vector2f;
import org.src.components.Camera;
import org.src.components.map.Map;
import org.src.components.Selection;
import org.src.components.province.Province;
import org.src.core.callbacks.*;
import org.src.core.helper.*;
import org.src.core.main.Window;
import org.src.core.managers.InputManager;
import org.src.core.managers.ShaderManager;

import static org.lwjgl.glfw.GLFW.*;
import static org.src.core.helper.Consts.POINT_POS_STRIDE;
import static org.src.core.helper.Helper.isInImGuiWindow;

public final class Editor extends Component {
	// TODO: divide the editor into classes containing each mode
	private final EditorWindow editorWindow;
	private final EditorCursor editorCursor;

	private EditorMode currentMode;

	private final Selection selection;
	private Camera camera;
	private Map map;

	private Province editedProvince;

	private Vector2f adjustedPos;

	private boolean gridAlignmentEnabled;

	private boolean magnetEnabled;

	private boolean drawProvinceFill;
	private boolean drawProvincePoints;

	private boolean valueRandomizerEnabled;

	private int heldPointIndex;

	public static final int GRID_ALIGN = 500;

	public static final float VALUE_RANDOMIZER_RANGE = 0.07f; // percent

	private final MouseMoveCallback moveCallback = () -> {
		adjustedPos.x = -camera.getPos().x + camera.getAccumulatedDragDist().x + (InputManager.getCenteredMouseX() / Window.getWidth()) / camera.getPos().z * 2;
		adjustedPos.y = -camera.getPos().y - camera.getAccumulatedDragDist().y - (InputManager.getCenteredMouseY() / Window.getWidth()) / camera.getPos().z * 2;
		currentMode.mouseMovedAction();
	};

	private final MouseLeftPressCallback leftCallback = () -> {
		if (isInImGuiWindow()) { return; }
		currentMode.mouseLeftPressedAction();
	};

	private final MouseLeftReleaseCallback leftReleaseCallback = () -> {
		currentMode.mouseLeftReleasedAction();
	};

	private final KeyPressCallback pressCallback = (final long window, final int key, final int action, final int mods) -> {
		switch (key) {
			case GLFW_KEY_Q:
				this.currentMode = new AddProvincesMode(this, map);
				break;
			case GLFW_KEY_E:
				this.currentMode = new EditProvincesMode(this);
				break;
			case GLFW_KEY_L:
				this.currentMode = new SelectProvincesMode(this, map);
				break;
			case GLFW_KEY_F:
				drawProvinceFill = !drawProvinceFill;
				break;
			case GLFW_KEY_G:
				drawProvincePoints = !drawProvincePoints;
				break;
			case GLFW_KEY_J:
				map.setDrawProvinceFillings(!map.getDrawProvinceFillings());
				break;
			case GLFW_KEY_H:
				map.toggleDrawProvincePoints();
				break;
		}
		currentMode.keyPressAction(key);
	};

	public Editor(final Camera camera, final Map map, final Selection selection) {
		this.camera = camera;
		this.map = map;
		this.selection = selection;

		this.adjustedPos = new Vector2f();

		this.editedProvince = map.createProvince();

		this.gridAlignmentEnabled = true;

		this.drawProvincePoints = this.drawProvinceFill = true;

		this.heldPointIndex = -1;

		this.currentMode = new SelectProvincesMode(this, map);

		this.editorWindow = new EditorWindow(this, map);
		this.editorCursor = new EditorCursor();

		InputManager.addMouseLeftPressCallback(leftCallback);
		InputManager.addMouseMoveCallback(moveCallback);
		InputManager.addKeyPressCallback(pressCallback);
		InputManager.addMouseLeftReleaseCallback(leftReleaseCallback);
	}

	public void rawCreateProvince() {
		editedProvince = map.createProvince();
	}

	public void setEditedProvince(final Province province) {
		editedProvince = province;
	}

	public void updateCursorPos(final float x, final float y) {
		editorCursor.updatePos(x, y);
	}

	public EditorCursor getCursor() {
		return this.editorCursor;
	}

	public void updateCursorPos(final Vector2f pos) {
		editorCursor.updatePos(pos);
	}

	public Vector2f getAdjustedPos() {
		return adjustedPos;
	}

	public void drawModeUI() {
		currentMode.renderGui();
	}

	@Override
	public void draw() {
		editedProvince.drawAlone(drawProvincePoints, drawProvinceFill);

		if (isModeAddProvinces()) {
			editorCursor.draw();
		} else if (isModeEditProvinces()) {
			drawForEditMode();
			drawSelectedProvincePoint();
		}
		editorWindow.draw();
	}

	public void deleteProvincePoint(final int id) {
		editedProvince.deletePoint(id);
		lookForNeighbors();
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

	private void drawSelectedProvincePoint() {
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


	public void lookForNeighbors() {
		editedProvince.clearNeighbors();
		// this looks unoptimized but in reality its barely called even once due to the if statement
		for (final Province province: map.getProvinces()) {
			if (province == editedProvince ||
				province.hasNeighbor(editedProvince) ||
				!province.getMaxPoints().intersects(editedProvince.getMaxPoints())
			) { continue; }

			for (int i = 0; i < editedProvince.getPointsPoses().length; i += POINT_POS_STRIDE) {
				for (int j = 0; j < province.getPointsPoses().length; j += POINT_POS_STRIDE) {
					if (editedProvince.getPointsPoses()[i] == province.getPointsPoses()[j]
						&& editedProvince.getPointsPoses()[i + 1] == province.getPointsPoses()[j + 1]) {
						editedProvince.addNeighbor(province);
						province.addNeighbor(editedProvince);
					}
				}
			}
		}
	}

	public void newProvince() {
		// we obviously don't want to make a new province while the current is empty
		if (editedProvince.getIndices().length == 0) { return; }

		map.addProvinceToMesh(editedProvince);
		editedProvince = map.createProvince();
	}

	public Province getProvince() {
		return editedProvince;
	}

	public boolean isGridAlignmentEnabled() {
		return gridAlignmentEnabled;
	}

	public void toggleGridAlignment() {
		gridAlignmentEnabled = !gridAlignmentEnabled;
		magnetEnabled = false;
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

	public boolean getMagnetEnabled() {
		return magnetEnabled;
	}

	public void setEnabledMagnet(final boolean value) {
		magnetEnabled = value;
		gridAlignmentEnabled = false;
	}

	public boolean isValueRandomizerEnabled() {
		return valueRandomizerEnabled;
	}

	public void setValueRandomizerEnabled(boolean valueRandomizerEnabled) {
		this.valueRandomizerEnabled = valueRandomizerEnabled;
	}

	public boolean getDrawFill() {
		return drawProvinceFill;
	}

	public void toggleDrawFill() {
		drawProvinceFill = !drawProvinceFill;
	}

	public boolean getDrawPoints() {
		return drawProvincePoints;
	}

	public void toggleDrawPoints() {
		drawProvincePoints = !drawProvincePoints;
	}

	public Selection getSelection() {
		return selection;
	}

	@Override
	public void dispose() {}

	public boolean isModeAddProvinces() {
		return currentMode instanceof AddProvincesMode;
	}

	public boolean isModeEditProvinces() {
		return currentMode instanceof EditProvincesMode;
	}

	public boolean isModeSelectProvinces() {
		return currentMode instanceof SelectProvincesMode;
	}

	public void setMode(final EEditorMode mode) {
		switch (mode) {
			case ADD_PROVINCES:
				currentMode = new AddProvincesMode(this, map);
				break;
			case EDIT_PROVINCES:
				currentMode = new EditProvincesMode(this);
				break;
			case SELECT_PROVINCES:
				currentMode = new SelectProvincesMode(this, map);
				break;
		}
	}

}
