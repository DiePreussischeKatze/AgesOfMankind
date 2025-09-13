package org.src.scenes;

import org.src.components.Camera;
import org.src.components.ScenarioSaver;
import org.src.components.Selection;
import org.src.components.map.Map;
import org.src.components.ui.gameplay.ArmyManager;
import org.src.components.ui.gameplay.GameplayUI;
import org.src.core.helper.Scene;

public final class GameplayScene extends Scene {
    public GameplayScene() {
        super();
        final Camera camera = new Camera();
		final Map map = new Map();
		final Selection selection = new Selection(camera);
		final ArmyManager armyManager = new ArmyManager(camera, selection, map);
        final GameplayUI gameplayUI = new GameplayUI(); // TODO: Remove
        
        ScenarioSaver scenarioSaver = new ScenarioSaver(map);
        scenarioSaver.loadScenario(true);

		componentManager.addComponent(camera, map, armyManager, gameplayUI, selection);
    }

}
