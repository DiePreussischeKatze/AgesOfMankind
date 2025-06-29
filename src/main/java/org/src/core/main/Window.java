package org.src.core.main;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.*;
import org.src.core.helper.Config;
import org.src.core.helper.Scene;
import org.src.core.managers.InputManager;
import org.src.core.managers.ShaderManager;
import org.src.rendering.Renderer;
import org.src.scenes.GameScene;

import java.nio.*;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import static org.src.core.helper.Consts.FRAMES_PER_SECOND;
import static org.src.core.helper.Consts.UPS_PER_SECOND;
import static org.src.core.helper.Helper.*;

public final class Window {
	private static long id;

	private static float aspectRatio;
	private static int width;
	private static int height;

	private final Loop loop;
	private final Renderer renderer;
	private Scene currentScene;

	Window() {
		// flip the loaded textures vertically so they agree with opengl's texture coordinates
		stbi_set_flip_vertically_on_load(true);

		// Create error callbacks
		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit()) {
			throw new IllegalStateException("Unable initialize GLFW");
		}

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 4);

		if (BOOL(Objects.requireNonNull(Config.get("antialiasingSamples")))) {
			glfwWindowHint(GLFW_SAMPLES, INT(Config.get("antialiasingSamples")));
		}

		final GLFWVidMode videoMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

		if (BOOL(Objects.requireNonNull(Config.get("fullscreen")))) {
			assert videoMode != null; // Intellij, SHUT UP!
			id = glfwCreateWindow(videoMode.width(), videoMode.height(), "Ages of Mankind (indev) | FPS: 0", glfwGetPrimaryMonitor(), NULL);
			if (id == NULL) {
				throw new RuntimeException("Unable to create the window");
			}

			glfwMakeContextCurrent(id);
			GL.createCapabilities();

			glViewport(0, 0, videoMode.width(), videoMode.height());
			aspectRatio = (float) videoMode.width() / (float) videoMode.height();
		} else {
			id = glfwCreateWindow(INT(Config.get("startWinWidth")), INT(Config.get("startWinHeight")), "Ages of Mankind (indev) | FPS: 0", NULL, NULL);
			if (id == NULL) {
				throw new RuntimeException("Unable to create the window");
			}

			glfwMakeContextCurrent(id);
			GL.createCapabilities();

			// center the window
			try (MemoryStack stack = stackPush()) {
				final IntBuffer pWidth = stack.mallocInt(1); // int*
				final IntBuffer pHeight = stack.mallocInt(1); // int*

				glfwGetWindowSize(id, pWidth, pHeight);

				assert videoMode != null; // Dear Intellij, FUCK OFF
				glfwSetWindowPos(
						id,
						(videoMode.width() - pWidth.get(0)) / 2,
						(videoMode.height() - pHeight.get(0)) / 2
				);
			}

			glViewport(0, 0, INT(Config.get("startWinWidth")), INT(Config.get("startWinHeight")));
			aspectRatio = FLOAT(Config.get("startWinWidth")) / FLOAT(Config.get("startWinHeight"));
		}

		glfwSetKeyCallback(id, InputManager::keyCallback);
		glfwSetMouseButtonCallback(id, InputManager::mousePressedCallback);
		glfwSetScrollCallback(id, InputManager::mouseScrollCallback);
		glfwSetCursorPosCallback(id, InputManager::mouseMoveCallback);
		glfwSetWindowSizeCallback(id, Window::resizeCallback);

		glfwSwapInterval(INT(BOOL(Objects.requireNonNull(Config.get("vsyncEnabled")))));
		glfwShowWindow(id);

		if (BOOL(Objects.requireNonNull(Config.get("antialiasingSamples")))) {
			glEnable(GL_MULTISAMPLE);
		}

		glEnable(GL_DEPTH_TEST);

		int[] winWidth = new int[1];
		int[] winHeight = new int[1];

		glfwGetWindowSize(id, winWidth, winHeight);

		width = winWidth[0];
		height = winHeight[0];

		ShaderManager.loadShaders("res/shaders/");

		this.currentScene = new GameScene();

		this.loop = new Loop(currentScene);
		this.renderer = new Renderer(currentScene);

		loop();
		die();
	}

	private static void resizeCallback(long window, int width, int height) {
		glViewport(0, 0, width, height);
		aspectRatio = (float) width / (float) height;
		Window.width = width;
		Window.height = height;
	}

	private void die() {
		currentScene.dispose();
		Callbacks.glfwFreeCallbacks(Window.getId());
		glfwDestroyWindow(id);
		glfwTerminate();
	}

	private void loop() {
		double then = glfwGetTime();
		double thenFps = glfwGetTime();
		double now;

		int fps = 0;
		double thenFpsCounter = glfwGetTime();

		while (!glfwWindowShouldClose(id)) {
			now = glfwGetTime();

			if (now - then > UPS_PER_SECOND) {
				glfwPollEvents();
				loop.doLogic((now - then) / UPS_PER_SECOND);
				then = now;
			}

			if (now - thenFps > FRAMES_PER_SECOND) {
				renderer.render();
				glfwSwapBuffers(id);
				fps++;
				thenFps = now;
			}

			if (now - thenFpsCounter > 1) {
				glfwSetWindowTitle(id, "Ages of Mankind (indev) | FPS: " + fps);
				fps = 0;
				thenFpsCounter = now;
			}
		}
	}

	public void changeScene(final Scene newScene) {
		currentScene.dispose();
		currentScene = newScene;
	}

	public static float getAspectRatio() {
		return aspectRatio;
	}

	public static int getWidth() {
		return width;
	}

	public static int getHeight() {
		return height;
	}

	public static long getId() {
		return id;
	}

}
