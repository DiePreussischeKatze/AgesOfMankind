package org.src.scenes;

import org.src.components.Camera;
import org.src.components.map.Map;
import org.src.components.Selection;
import org.src.components.ui.editor.Editor;
import org.src.core.helper.Scene;

public final class GameScene extends Scene {

	public GameScene() {
		super();
		final Camera camera = new Camera();
		final Map map = new Map();
		final Selection selection = new Selection(camera);
		final Editor editor = new Editor(camera, map, selection);

		componentManager.addComponent(camera, map, editor, selection);
	}

}
