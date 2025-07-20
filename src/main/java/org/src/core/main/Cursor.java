package org.src.core.main;

import org.lwjgl.glfw.GLFWImage;
import org.src.core.managers.InputManager;

import java.nio.ByteBuffer;

import static org.lwjgl.glfw.GLFW.glfwCreateCursor;
import static org.lwjgl.glfw.GLFW.glfwSetCursor;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;

public final class Cursor {
	// TODO: Figure out hwy this isn't working
	private long unactiveCursor;
	private long activeCursor;

	public Cursor() {
		final int[] width = new int[1];
		final int[] height = new int[1];
		final int[] channels = new int[1];
		stbi_set_flip_vertically_on_load(false);
		final ByteBuffer unactivePixels = stbi_load("res/cursors/active.png", width, height, channels, 4);
		final ByteBuffer activePixels = stbi_load("res/cursors/unactive.png", width, height, channels, 4);
		stbi_set_flip_vertically_on_load(true);

		final GLFWImage unactive = GLFWImage.create();
		final GLFWImage active = GLFWImage.create();

		assert activePixels != null; // Shut up, IDEA
		assert unactivePixels != null;

		active.set(32, 32, activePixels);
		unactive.set(32, 32, unactivePixels);

		unactiveCursor = glfwCreateCursor(unactive, 4, 5);
		activeCursor = glfwCreateCursor(active, 4, 5);
	}

	public void updateCursor() {
		// IDK hwy this must be in a loop
		if (InputManager.leftPressed()) {
			glfwSetCursor(Window.getId(), activeCursor);
		} else {
			glfwSetCursor(Window.getId(), unactiveCursor);
		}
	}

}
