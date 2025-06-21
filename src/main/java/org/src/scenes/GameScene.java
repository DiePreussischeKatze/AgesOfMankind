package org.src.scenes;

import org.src.components.Camera;
import org.src.components.Map;
import org.src.components.ui.editor.Editor;
import org.src.core.helper.Scene;

public final class GameScene extends Scene {

	public GameScene() {
		super();
		final Camera camera = new Camera();
		final Map map = new Map();
		final Editor editor = new Editor(camera, map);

		componentManager.addComponent(camera, map, editor);
	}

}
