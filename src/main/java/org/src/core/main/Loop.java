package org.src.core.main;

import org.src.core.helper.Scene;

public final class Loop {

	private final Scene currentScene;
	Loop(final Scene currentScene) {
		this.currentScene = currentScene;
	}

	public void doLogic(final double deltaTime) {
		currentScene.update(deltaTime);
	}

}
