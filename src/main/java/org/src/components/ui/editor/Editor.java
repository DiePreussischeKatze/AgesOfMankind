package org.src.components.ui.editor;

import org.joml.Vector2f;
import org.src.components.Camera;
import org.src.components.map.DisplayMode;
import org.src.components.map.Map;
import org.src.components.Selection;
import org.src.components.province.Province;
import org.src.core.callbacks.*;
import org.src.core.helper.*;
import org.src.core.managers.InputManager;

import static org.lwjgl.glfw.GLFW.*;
import static org.src.components.ui.editor.EEditorMode.*;
import static org.src.core.helper.Consts.POINT_POS_STRIDE;
import static org.src.core.helper.Helper.isInImGuiWindow;

public final class Editor extends Component {
	private final EditorWindow editorWindow;
	private final EditorCursor editorCursor;

	private EditorMode mode;

	private double paintSETValue;
	private double paintADDValue;

	private final Selection selection;
	private Camera camera;
	private Map map;

	private Province editedProvince;

	private boolean gridAlignmentEnabled;

	private boolean magnetEnabled;

	private boolean drawProvinceFill;
	private boolean drawProvincePoints;

	private boolean valueRandomizerEnabled;

	private int heldPointIndex;

	public static final float VALUE_RANDOMIZER_RANGE = 0.07f; // percent

	private final MouseMoveCallback moveCallback = () -> {
		mode.mouseMovedAction();
	};

	private final MouseLeftPressCallback leftCallback = () -> {
		if (isInImGuiWindow()) { return; }
		mode.mouseLeftPressedAction();
	};

	private final MouseLeftReleaseCallback leftReleaseCallback = () -> {
		mode.mouseLeftReleasedAction();
	};

	private final KeyPressCallback pressCallback = (final long window, final int key, final int action, final int mods) -> {
		switch (key) {
			case GLFW_KEY_Q -> setMode(ADD_PROVINCES);
			case GLFW_KEY_E -> setMode(EDIT_PROVINCES);
			case GLFW_KEY_L -> setMode(SELECT_PROVINCES);
			case GLFW_KEY_P -> setMode(PAINT_PROVINCES);
			case GLFW_KEY_F -> toggleDrawFill();
			case GLFW_KEY_G -> toggleDrawPoints();
			case GLFW_KEY_J -> map.toggleDrawProvinceFillings();
			case GLFW_KEY_H -> map.toggleDrawProvincePoints();
		}
		mode.keyPressAction(key);
	};

	private final KeyReleaseCallback releaseCallback = (final long window, final int key, final int action, final int mods) -> {
		mode.keyReleaseAction(key);
	};


	public Editor(final Camera camera, final Map map, final Selection selection) {
		this.camera = camera;
		this.map = map;
		this.selection = selection;

		this.editedProvince = map.createProvince();

		this.gridAlignmentEnabled = true;

		this.drawProvincePoints = this.drawProvinceFill = true;

		this.paintADDValue = this.paintSETValue = 0.0;
		this.heldPointIndex = -1;

		this.mode = new SelectProvincesMode(this, map);

		this.editorWindow = new EditorWindow(this, map);
		this.editorCursor = new EditorCursor();

		// that's a wacky way of not having any province selected when starting the editor
		map.addProvinceToMesh(this.editedProvince);
		this.editedProvince = map.createProvince();

		InputManager.addMouseLeftPressCallback(leftCallback);
		InputManager.addMouseMoveCallback(moveCallback);
		InputManager.addKeyPressCallback(pressCallback);
		InputManager.addKeyReleaseCallback(releaseCallback);
		InputManager.addMouseLeftReleaseCallback(leftReleaseCallback);
	}

	public void rawCreateProvince() {
		editedProvince = map.createProvince();
	}

	public void setProvince(final Province province) {
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
		return camera.getAdjustedMousePos();
	}

	public void drawModeUI() {
		mode.renderGui();
	}

	@Override
	public void draw() {
		editedProvince.drawAlone(drawProvincePoints, drawProvinceFill);
		mode.draw();
		editorWindow.draw();
	}

	public void deleteProvincePoint(final int id) {
		editedProvince.deletePoint(id);
		map.lookForNeighbors(editedProvince);
	}

	public void newProvince() {
		// we obviously don't want to make a new province while the current is empty
		if (editedProvince.getIndices().length == 0) { return; }

		map.addProvinceToMesh(editedProvince);
		editedProvince = map.createProvince();
	}

	// this should be the preferred way of changing the display hwen using the editor
	public void changeMapDisplay(final DisplayMode mode) {
		int lastLendProvince = map.getLendProvinceId();
		int lendProvinceID = 0;
		if (lastLendProvince != map.getProvinces().size() -1) {
			lendProvinceID = map.getLendProvinceId();
			map.addProvinceToMesh(map.getProvince(lendProvinceID));
			this.editedProvince = null;
			map.setLendProvinceID(-1);
		}

		//PerfTimer t1 = new PerfTimer("display change");
		map.setDisplayMode(mode);
		//t1.reset();

		if (lastLendProvince != map.getProvinces().size() -1) {
			this.editedProvince = map.getProvince(lendProvinceID);
			map.takeProvinceFromMesh(this.editedProvince);
			map.setLendProvinceID(lendProvinceID);
		}

	}

	public EditorMode getMode() {
		return this.mode;
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

	public void deselectPoint() {
		this.heldPointIndex = -1;
	}

	public boolean isAnyPointSelected() {
		return heldPointIndex != -1;
	}

	@Override
	public void update(double deltaTime) {
		mode.update(deltaTime);
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

	public double getPaintSETValue() {
		return paintSETValue;
	}

	public void setPaintSETValue(final double paintSETValue) {
		this.paintSETValue = paintSETValue;
	}

	public double getPaintADDValue() {
		return paintADDValue;
	}

	public void setPaintADDValue(final double paintADDValue) {
		this.paintADDValue = paintADDValue;
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
		return mode instanceof AddProvincesMode;
	}

	public boolean isModeEditProvinces() {
		return mode instanceof EditProvincesMode;
	}

	public boolean isModeSelectProvinces() {
		return mode instanceof SelectProvincesMode;
	}

	public boolean isModePaintProvinces() {
		return mode instanceof PaintProvincesMode;
	}

	public void setMode(final EEditorMode mode) {
		this.mode.dispose();
		switch (mode) {
			case ADD_PROVINCES -> {
				if (isModeAddProvinces()) { break; }
				this.mode = new AddProvincesMode(this, map);
			}
			case EDIT_PROVINCES -> {
				if (isModeEditProvinces()) { break; }
				this.mode = new EditProvincesMode(this, map);
			}
			case SELECT_PROVINCES -> {
				if (isModeSelectProvinces()) { break; }
				this.mode = new SelectProvincesMode(this, map);
			}
			case PAINT_PROVINCES -> {
				if (isModePaintProvinces()) { break; }
				this.mode = new PaintProvincesMode(this, map);
				newProvince();
			}
		}
	}

}
