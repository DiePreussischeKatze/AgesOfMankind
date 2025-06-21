package org.src.rendering;

import org.src.core.helper.Scene;

import static org.lwjgl.opengl.GL11.*;

public final class Renderer {

	private final Scene currentScene;
	public Renderer(final Scene currentScene) {
		this.currentScene = currentScene;
	}

	public void render() {
		clear();
		currentScene.draw();
	}

	private void clear() {
		glClearColor(0, 0, 0, 1);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

}
