package org.src.components;

import org.joml.Vector2f;
import org.src.core.callbacks.MouseLeftPressCallback;
import org.src.core.callbacks.MouseLeftReleaseCallback;
import org.src.core.callbacks.MouseMoveCallback;
import org.src.core.helper.Component;
import org.src.core.helper.Rect2D;
import org.src.core.helper.ShaderID;
import org.src.core.main.Window;
import org.src.core.managers.InputManager;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;

import static org.lwjgl.opengl.GL11.*;
import static org.src.core.helper.Consts.RECT_INDICES;

public final class Selection extends Component {
	private Mesh box;

	private Vector2f start;
	private Vector2f end;

	private Vector2f cameraAdjustedStart;
	private Vector2f cameraAdjustedEnd;

	private Vector2f screenAdjustedStart;
	private Vector2f screenAdjustedEnd;

	private boolean enabled;
	private boolean active;

	private final Camera camera;

	private final MouseMoveCallback moveCallback = () -> {
		if (!enabled || !active) { return; }

		end.x = InputManager.getMouseX();
		end.y = -InputManager.getMouseY();

		screenAdjustedEnd.x = InputManager.getCenteredMouseX() / Window.getWidth() * 2;
		screenAdjustedEnd.y = -InputManager.getCenteredMouseY() / Window.getHeight() * 2;

		// maybe this isn't exactly the most efficient
		box.vertices = new float[]
				{
						screenAdjustedStart.x, screenAdjustedStart.y,
						screenAdjustedStart.x, screenAdjustedEnd.y,
						screenAdjustedEnd.x, screenAdjustedEnd.y,
						screenAdjustedEnd.x, screenAdjustedStart.y,
				};
		box.regenerate();
	};

	private final MouseLeftPressCallback leftPressCallback = () -> {
		if (!enabled) { return; }
		active = true;

		start.x = InputManager.getMouseX();
		start.y = -InputManager.getMouseY();

		screenAdjustedStart.x = InputManager.getCenteredMouseX() / Window.getWidth() * 2;
		screenAdjustedStart.y = -InputManager.getCenteredMouseY() / Window.getHeight() * 2;

		box.vertices = new float[1];
		box.regenerate();
	};

	private final MouseLeftReleaseCallback leftReleaseCallback = () -> {
		if (!enabled) { return; }

		clear();
	};

	public Selection(final Camera camera) {
		this.camera = camera;

		box = new Mesh(RECT_INDICES, new byte[]{2});

		start = new Vector2f();
		end = new Vector2f();

		cameraAdjustedStart = new Vector2f();
		cameraAdjustedEnd = new Vector2f();

		screenAdjustedStart = new Vector2f();
		screenAdjustedEnd = new Vector2f();

		InputManager.addMouseMoveCallback(moveCallback);
		InputManager.addMouseLeftPressCallback(leftPressCallback);
		InputManager.addMouseLeftReleaseCallback(leftReleaseCallback);
	}

	@Override
	public void draw() {
		if (!active) { return; }

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_SRC_COLOR);
		ShaderManager.get(ShaderID.SELECTION).bind();
		box.draw();
		glDisable(GL_BLEND);
	}

	public void clear() {
		active = false;

		start.x = start.y = end.x = end.y = screenAdjustedEnd.x = screenAdjustedEnd.y = screenAdjustedStart.x = screenAdjustedStart.y = 0;

		box.vertices = new float[1];
		box.regenerate();
	}

	@Override
	public void dispose() {
		box.dispose();
	}

	public Rect2D get() {
		return new Rect2D(start.x, start.y, end.x - start.x, end.y - start.y);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override public void update(final double deltaTime){}

}
