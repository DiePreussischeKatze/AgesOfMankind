package org.src.components.ui.editor;

import org.src.components.Camera;
import org.src.components.Map;
import org.src.components.province.Province;
import org.src.core.callbacks.KeyPressCallback;
import org.src.core.callbacks.MouseMoveCallback;
import org.src.core.helper.Component;
import org.src.core.main.Window;
import org.src.core.managers.InputManager;
import org.src.core.callbacks.MouseLeftPressCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.src.core.helper.Helper.isInImGuiWindow;

public final class Editor extends Component {

	private final EditorWindow editorWindow;
	private EditorCursor editorCursor;

	private EditorMode mode;
	private Province currentProvince;

	private Camera camera;
	private Map map;

	private final MouseMoveCallback moveCallback = () -> {
			editorCursor.updatePosition((float) Math.floor((-camera.getPosition().x + camera.getAccumulatedDragDistance().x + (InputManager.getCenteredMouseX() / Window.getWidth()) / camera.getPosition().z * 2) * 1000) / 1000 + 0.0005f,
					(float) Math.floor((-camera.getPosition().y - camera.getAccumulatedDragDistance().y - (InputManager.getCenteredMouseY() / Window.getWidth()) / camera.getPosition().z * 2) * 1000) / 1000 + 0.0005f);
	};

	private final MouseLeftPressCallback leftCallback = () -> {
		if (isInImGuiWindow() || mode != EditorMode.ADD_PROVINCES) { return; }

		currentProvince.addPoint(
				(float) Math.floor((-camera.getPosition().x + camera.getAccumulatedDragDistance().x + (InputManager.getCenteredMouseX() / Window.getWidth()) / camera.getPosition().z * 2) * 1000) / 1000 + 0.0005f,
				(float) Math.floor((-camera.getPosition().y - camera.getAccumulatedDragDistance().y - (InputManager.getCenteredMouseY() / Window.getWidth()) / camera.getPosition().z * 2) * 1000) / 1000 + 0.0005f
		);
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
		}
	};

	public Editor(final Camera camera, final Map map) {
		this.camera = camera;
		this.map = map;

		currentProvince = map.createProvince();

		mode = EditorMode.ADD_PROVINCES;

		this.editorWindow = new EditorWindow(this);
		this.editorCursor = new EditorCursor();

		InputManager.addMouseLeftPressCallback(leftCallback);
		InputManager.addMouseMoveCallback(moveCallback);
		InputManager.addKeyPressCallback(pressCallback);
	}

	public void passProvince() {
		map.addProvinceToMesh(currentProvince);
		currentProvince = map.createProvince();
	}

	@Override
	public void draw() {
		currentProvince.drawAlone();
		editorCursor.draw();
		editorWindow.draw();
	}

	public EditorMode getMode() {
		return mode;
	}

	public Province getCurrentProvince() {
		return currentProvince;
	}

	public void setMode(EditorMode mode) {
		this.mode = mode;
	}

	@Override
	public void update(double deltaTime) {

	}

	@Override
	public void dispose() {}

}
