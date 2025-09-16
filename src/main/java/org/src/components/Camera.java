package org.src.components;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.src.core.callbacks.*;
import org.src.core.helper.*;
import org.src.core.managers.InputManager;
import org.src.core.main.Window;
import org.src.rendering.wrapper.UniformBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL31.*;
import static org.src.core.helper.Helper.FLOAT;
import static org.src.core.helper.Helper.BOOL;
import static org.src.core.helper.Helper.isInImGuiWindow;

public final class Camera extends Component {
	private Vector3f position;
	private Vector3f finalPosition; // the position including WASD movement and dragging

	private Vector2f adjustedMousePos;

	private final float RAW_SPEED = FLOAT(Config.get("cameraMoveSpeed"));
	private final float MAX_ZOOM = FLOAT(Config.get("cameraMaxZoom"));
	private final float MIN_ZOOM = FLOAT(Config.get("cameraMinZoom"));
	private final float DECELERATION = FLOAT(Config.get("cameraDeceleration"));

	private final boolean SHOULD_BE_LIMITED = BOOL(Config.get("limitCamera"));

	public final UniformBuffer uniformBuffer;

	private final Vector3f acceleration;

	private Vector2f dragStart;
	private Vector2f dragEnd;
	private Vector2f dragDelta;
	private Vector2f draggedDistance;
	private Vector2f accumulatedDragDistance;

	private boolean wPressed;
	private boolean sPressed;
	private boolean aPressed;
	private boolean dPressed;

	private boolean shiftPressed;
	private boolean spacePressed;

	private boolean dragging;

	private final MouseRightPressCallback rightPressCallback = () -> {
		if (isInImGuiWindow()) {
			return;
		}

		dragStart.x = InputManager.getMouseX();
		dragStart.y = InputManager.getMouseY();
		dragging = true;
	};

	private final MouseRightReleaseCallback rightReleaseCallback = () -> {
		dragging = false;

		draggedDistance.add(dragDelta);

		dragEnd.x = dragEnd.y = dragStart.y = dragStart.x = dragDelta.x = dragDelta.y = 0;
	};

	private final MouseMoveCallback moveCallback = () -> {
		adjustedMousePos.x = -position.x + accumulatedDragDistance.x + (InputManager.getCenteredMouseX() / Window.getWidth()) / position.z * 2;
		adjustedMousePos.y = -position.y - accumulatedDragDistance.y - (InputManager.getCenteredMouseY() / Window.getWidth()) / position.z * 2;

		if (!dragging) {
			return;
		}

		dragEnd.x = InputManager.getMouseX();
		dragEnd.y = InputManager.getMouseY();

		dragDelta.x = (dragStart.x - dragEnd.x) / position.z / Window.getWidth() * 2;
		dragDelta.y = (dragStart.y - dragEnd.y) / position.z / Window.getWidth() * 2;

		accumulatedDragDistance.x = draggedDistance.x + dragDelta.x;
		accumulatedDragDistance.y = draggedDistance.y + dragDelta.y;

	};

	private final KeyPressCallback pressCallback = (final long window, final int key, final int action, final int mods) -> {
		if (InputManager.keyPressed(GLFW_KEY_LEFT_CONTROL) || InputManager.keyPressed(GLFW_KEY_LEFT_ALT)) {
			return;
		}

		keyHeld(key, true);
	};

	private void keyHeld(final int key, final boolean value) {
		switch (key) {
			case GLFW_KEY_W, GLFW_KEY_UP    -> wPressed     = value;
			case GLFW_KEY_S, GLFW_KEY_DOWN  -> sPressed     = value;
			case GLFW_KEY_A, GLFW_KEY_LEFT  -> aPressed     = value;
			case GLFW_KEY_D, GLFW_KEY_RIGHT -> dPressed     = value;
			case GLFW_KEY_SPACE             -> spacePressed = value;
			case GLFW_KEY_LEFT_SHIFT        -> shiftPressed = value;
		}
	}

	private final KeyReleaseCallback releaseCallback = (final long window, final int key, final int action, final int mods) -> {
		if (InputManager.keyPressed(GLFW_KEY_LEFT_CONTROL) || InputManager.keyPressed(GLFW_KEY_LEFT_ALT)) {
			return;
		}

		keyHeld(key, false);
	};

	private final MouseScrollCallback upScrollCallback = () -> {
		if (position.z >= MAX_ZOOM - 0.2f || isInImGuiWindow()) {
			return;
		}

		// This is done in order to get rid of the jitter when the camera is very close to MAX_ZOOM
		final float zoomEfficiency = 0.1f * position.z;
		final float positionZThen = position.z;

		position.z += zoomEfficiency;
		position.z = Math.min(position.z, MAX_ZOOM);

		final float zoomProduced = position.z - positionZThen;
		final float doneZoomPercentage = zoomProduced / zoomEfficiency;

		position.x -= InputManager.getCenteredMouseX() / position.z / Window.getWidth() / 5 * doneZoomPercentage;
		position.y += InputManager.getCenteredMouseY() / position.z / Window.getWidth() / 5 * doneZoomPercentage;
	};

	private final MouseScrollCallback downScrollCallback = () -> {
		if (position.z <= MIN_ZOOM + 0.002f || isInImGuiWindow()) {
			return;
		}

		final float zoomEfficiency = 0.1f * position.z;
		final float positionZThen = position.z;

		position.z -= zoomEfficiency;
		position.z = Math.max(position.z, MIN_ZOOM);

		final float zoomProduced = positionZThen - position.z;
		final float doneZoomPercentage = zoomProduced / zoomEfficiency;

		position.x += InputManager.getCenteredMouseX() / position.z / Window.getWidth() / 5 * doneZoomPercentage;
		position.y -= InputManager.getCenteredMouseY() / position.z / Window.getWidth() / 5 * doneZoomPercentage;
	};

	public Camera() {
		
		this.sPressed = this.wPressed = this.aPressed = this.dPressed = false;
		this.dragging = false;
		
		this.uniformBuffer = new UniformBuffer(16, 0, GL_DYNAMIC_DRAW);
		
		// position - (x - y), (y - y) (z - scaling factor)
		this.position = new Vector3f(-0.4f, -3.0f, 5.0f);
		this.acceleration = new Vector3f(0.0f, 0.0f, 0.0f);
		
		this.adjustedMousePos = new Vector2f();
		this.dragStart = new Vector2f();
		this.draggedDistance = new Vector2f();
		this.dragDelta = new Vector2f();
		this.dragEnd = new Vector2f();
		this.accumulatedDragDistance = new Vector2f();
		this.finalPosition = new Vector3f();

		InputManager.addKeyPressCallback(pressCallback);
		InputManager.addKeyReleaseCallback(releaseCallback);
		InputManager.addMouseRightPressCallback(rightPressCallback);
		InputManager.addMouseRightReleaseCallback(rightReleaseCallback);
		InputManager.addMouseMoveCallback(moveCallback);
		InputManager.addMouseUpScrollCallback(upScrollCallback);
		InputManager.addMouseDownScrollCallback(downScrollCallback);
	}

	@Override
	public void draw() {
		uniformBuffer.bind();
		uniformBuffer.regenerate(new float[]{
			finalPosition.x,
			finalPosition.y,
			finalPosition.z,
			Window.getAspectRatio(),
		});
	}

	@Override
	public void update(final double deltaTime) {
		move(deltaTime);
		if (SHOULD_BE_LIMITED) { limit(); }
	}

	public Vector3f getPos() {
		return position;
	}

	public Vector2f getAccumulatedDragDist() {
		return accumulatedDragDistance;
	}

	@Override
	public void dispose() {
		uniformBuffer.dispose();
	}

	private void move(final double deltaTime) {
		final float realSpeed = (float) (RAW_SPEED / position.z * deltaTime);
		final float readLiftSpeed = (float) (RAW_SPEED * position.z * deltaTime);
		if (aPressed) {
			acceleration.x = realSpeed;
		}
		if (dPressed) {
			acceleration.x = -realSpeed;
		}
		if (wPressed) {
			acceleration.y = -realSpeed;
		}
		if (sPressed) {
			acceleration.y = realSpeed;
		}
		if (shiftPressed) {
			acceleration.z = readLiftSpeed;
		}
		if (spacePressed) {
			acceleration.z = -readLiftSpeed;
		}

		final float realDeceleration = (float) (DECELERATION / position.z * deltaTime);

		if (acceleration.x > 0) {
			acceleration.x -= realDeceleration;
		} else if (acceleration.x < 0) {
			acceleration.x += realDeceleration;
		}

		if (acceleration.y > 0) {
			acceleration.y -= realDeceleration;
		} else if (acceleration.y < 0) {
			acceleration.y += realDeceleration;
		}

		if (acceleration.z > 0) {
			acceleration.z -= (float) (position.z / 1000 * deltaTime);
		} else if (acceleration.z < 0) {
			acceleration.z += (float) (position.z / 1000 * deltaTime);
		}

		// if the acceleration is very small, we don't want it to make the screen slowly shift (or jitter), so we make it 0
		if (Helper.insideRange(acceleration.x, (float) (0.0001F * deltaTime), (float) (-0.0001F * deltaTime))) {
			acceleration.x = 0;
		}

		if (Helper.insideRange(acceleration.y, (float) (0.0001F * deltaTime), (float) (-0.0001F * deltaTime))) {
			acceleration.y = 0;
		}
		// I'm thinking about getting rid of the WASD controls
		if (Helper.insideRange(acceleration.z, (float) (0.008F * deltaTime * position.z), (float) (-0.008F * deltaTime * position.z))) {
			acceleration.z = 0;
		}

		if (position.z > MAX_ZOOM) {
			position.z = MAX_ZOOM;
			acceleration.z = 0;
		} else if (position.z < MIN_ZOOM) {
			position.z = MIN_ZOOM;
			acceleration.z = 0;
		}

		position.add(acceleration);

		finalPosition.set(position.x - accumulatedDragDistance.x, position.y + accumulatedDragDistance.y, position.z);
	}

	public Vector2f getAdjustedMousePos() {
		return new Vector2f(adjustedMousePos);
	}
	// TODO: Implement a better way of doing it
	private void limit() {
		finalPosition.x = Math.clamp(finalPosition.x, -4f, 4f);
		finalPosition.y = Math.clamp(finalPosition.y, -4f, 4f);
	}

}