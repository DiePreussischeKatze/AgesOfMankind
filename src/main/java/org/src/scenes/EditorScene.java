package org.src.scenes;

import org.src.components.Camera;
import org.src.components.map.Map;
import org.src.components.Selection;
import org.src.components.ui.editor.Editor;
import org.src.components.ui.gameplay.ArmyManager;
import org.src.components.ui.gameplay.GameplayUI;
import org.src.core.helper.Scene;

public final class EditorScene extends Scene {

	public EditorScene() {
		super();
		final Camera camera = new Camera();
		final Map map = new Map();
		final Selection selection = new Selection(camera);
		final ArmyManager armyManager = new ArmyManager(camera, selection, map);
		final Editor editor = new Editor(camera, map, selection);

		componentManager.addComponent(camera, map, editor, armyManager, selection);
	}

}
