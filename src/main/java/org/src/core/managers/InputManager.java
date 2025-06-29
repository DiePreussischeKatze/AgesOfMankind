package org.src.core.managers;

import org.src.core.callbacks.*;
import org.src.core.helper.Helper;

import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;

public final class InputManager {
	private static final boolean[] keys = new boolean[500]; // 500 is an arbitrary number

	private static final ArrayList<KeyPressCallback> keyPressCallbacks = new ArrayList<>();
	private static final ArrayList<KeyReleaseCallback> keyReleaseCallbacks = new ArrayList<>();

	private static final ArrayList<MouseMoveCallback> mouseMoveCallbacks = new ArrayList<>();

	private static final ArrayList<MouseRightReleaseCallback> mouseRightReleaseCallbacks = new ArrayList<>();
	private static final ArrayList<MouseLeftReleaseCallback> mouseLeftReleaseCallbacks = new ArrayList<>();

	private static final ArrayList<MouseLeftPressCallback> mouseLeftPressCallbacks = new ArrayList<>();
	private static final ArrayList<MouseRightPressCallback> mouseRightPressCallbacks = new ArrayList<>();

	private static final ArrayList<MouseScrollCallback> mouseUpScrollCallbacks = new ArrayList<>();
	private static final ArrayList<MouseScrollCallback> mouseDownScrollCallbacks = new ArrayList<>();

	private static boolean mouseLeftPressed = false;
	private static boolean mouseRightPressed = false;
	private static boolean mouseMiddlePressed = false;

	private static float mouseX = 0;
	private static float mouseY = 0;

	private static float centeredMouseX = 0;
	private static float centeredMouseY = 0;

	private static float mouseXSpeed = 0;
	private static float mouseYSpeed = 0;

	static {
		Arrays.fill(keys, false);
	}
	@SuppressWarnings("unused")
	public static void keyCallback(
			final long window,
			final int key,
			final int scancode,
			final int action,
			final int mods
	) {
		if (Helper.guiInputActive()) { return; }

		if (action == GLFW_PRESS) {
			keys[key] = true;

			keyPressCallbacks.forEach(callback -> callback.invoke(window, key, action, mods));
		} else if (action == GLFW_RELEASE) {
			keys[key] = false;

			keyReleaseCallbacks.forEach(callback -> callback.invoke(window, key, action, mods));
		}
	}

	public static void mouseMoveCallback(
			final long window,
			final double x,
			final double y
	) {
		mouseXSpeed = (float) (x - mouseX);
		mouseYSpeed = (float) (y - mouseY);

		mouseY = (float) y;
		mouseX = (float) x;

		int[] windowWidth = new int[1];
		int[] windowHeight = new int[1];

		glfwGetWindowSize(window, windowWidth, windowHeight);

		centeredMouseX = mouseX - ((float) windowWidth[0] / 2);
		centeredMouseY = mouseY - ((float) windowHeight[0] / 2);

		mouseMoveCallbacks.forEach(MouseMoveCallback::invoke);
	}

	public static void mousePressedCallback(
			final long window,
			final int button,
			final int action,
			final int mods
	) {
		if (action == GLFW_PRESS) {
			switch (button) {
				case GLFW_MOUSE_BUTTON_LEFT:
					mouseLeftPressed = true;
					mouseLeftPressCallbacks.forEach(MouseLeftPressCallback::invoke);
					break;
				case GLFW_MOUSE_BUTTON_RIGHT:
					mouseRightPressed = true;
					mouseRightPressCallbacks.forEach(MouseRightPressCallback::invoke);
					break;
				case GLFW_MOUSE_BUTTON_MIDDLE:
					mouseMiddlePressed = true;
					break;
			}

		} else if (action == GLFW_RELEASE) {
			switch (button) {
				case GLFW_MOUSE_BUTTON_LEFT:
					mouseLeftPressed = false;
					mouseLeftReleaseCallbacks.forEach(MouseLeftReleaseCallback::invoke);
					break;
				case GLFW_MOUSE_BUTTON_RIGHT:
					mouseRightPressed = false;
					mouseRightReleaseCallbacks.forEach(MouseRightReleaseCallback::invoke);
					break;
				case GLFW_MOUSE_BUTTON_MIDDLE:
					mouseMiddlePressed = false;
					break;
			}

		}
	}

	public static void mouseScrollCallback(
			final long window,
			final double xOffset,
			final double yOffset
	) {
		if (yOffset < 0) {
			mouseDownScrollCallbacks.forEach(MouseScrollCallback::invoke);
		} else if (yOffset > 0) {
			mouseUpScrollCallbacks.forEach(MouseScrollCallback::invoke);
		}
	}

	/**
	 * Adds the callback that's going to be directly run after the keys array has been updated
	 * The callbacks are run in the order they are added
	 * @param callbacks the callback function
	 */
	public static void addKeyPressCallback(final KeyPressCallback... callbacks) {
		keyPressCallbacks.addAll(Arrays.asList(callbacks));
	}

	public static void addMouseMoveCallback(final MouseMoveCallback... callbacks) {
		mouseMoveCallbacks.addAll(Arrays.asList(callbacks));
	}

	public static void addMouseLeftPressCallback(final MouseLeftPressCallback... callbacks) {
		mouseLeftPressCallbacks.addAll(Arrays.asList(callbacks));
	}

	public static void addMouseRightPressCallback(final MouseRightPressCallback... callbacks) {
		mouseRightPressCallbacks.addAll(Arrays.asList(callbacks));
	}

	public static void addMouseLeftReleaseCallback(final MouseLeftReleaseCallback... callbacks) {
		mouseLeftReleaseCallbacks.addAll(Arrays.asList(callbacks));
	}

	public static void addMouseRightReleaseCallback(final MouseRightReleaseCallback... callbacks) {
		mouseRightReleaseCallbacks.addAll(Arrays.asList(callbacks));
	}

	public static void addKeyReleaseCallback(final KeyReleaseCallback... callbacks) {
		keyReleaseCallbacks.addAll(Arrays.asList(callbacks));
	}

	public static void addMouseDownScrollCallback(final MouseScrollCallback... callbacks) {
		mouseDownScrollCallbacks.addAll(Arrays.asList(callbacks));
	}

	public static void addMouseUpScrollCallback(final MouseScrollCallback... callbacks) {
		mouseUpScrollCallbacks.addAll(Arrays.asList(callbacks));
	}

	public static boolean keyPressed(final int c) {
		return keys[c];
	}

	public static boolean leftPressed() { return mouseLeftPressed; }
	public static boolean rightPressed() { return mouseRightPressed; }
	public static boolean middlePressed() { return mouseMiddlePressed; }

	public static float getMouseX() { return mouseX; }
	public static float getMouseY() { return mouseY; }

	public static float getCenteredMouseX() { return centeredMouseX; }
	public static float getCenteredMouseY() { return centeredMouseY; }

	public static float getMouseXSpeed() { return mouseXSpeed; }
	public static float getMouseYSpeed() { return mouseYSpeed; }

}
