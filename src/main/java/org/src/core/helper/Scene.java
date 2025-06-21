package org.src.core.helper;

import org.src.core.managers.ComponentManager;

public abstract class Scene {
	protected final ComponentManager componentManager;

	public Scene() {
		this.componentManager = new ComponentManager();
	}

	public void draw() {
		componentManager.draw();
	}

	public void update(final double deltaTime) {
		componentManager.update(deltaTime);
	}

	public void dispose() {
		componentManager.dispose();
	}

}
