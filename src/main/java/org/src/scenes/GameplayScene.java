package org.src.scenes;

import org.src.components.Camera;
import org.src.components.ScenarioSaver;
import org.src.components.Selection;
import org.src.components.map.Map;
import org.src.components.ui.gameplay.ArmyManager;
import org.src.components.ui.gameplay.GameplayUI;
import org.src.core.helper.Scene;

public final class GameplayScene extends Scene {
  	private int gameSpeed;
	private boolean gamePaused;
	private int currentTick;
  
    private final Camera camera;
    private final Map map;
    private final Selection selection;
    private final ArmyManager armyManager;
    private final GameplayUI gameplayUI;

    public GameplayScene() {
        super();
       
		camera = new Camera();
        map = new Map();
        selection = new Selection(camera);
        armyManager = new ArmyManager(camera, selection, map);
		
        final ScenarioSaver scenarioSaver = new ScenarioSaver(map);
        scenarioSaver.loadScenario(true);
        map.bakeBorders();

        gameplayUI = new GameplayUI(map, this);
    
		componentManager.addComponent(camera, map, armyManager, gameplayUI, selection);
    }

    @Override
    public void update(final double deltaTime) {
        // The objects that only need to be updated once
        camera.update(deltaTime);
        selection.update(deltaTime);
        gameplayUI.update(deltaTime);

        if (gamePaused) { return; }
        
        int passes = 0;

        // let's say 1/10 of a tick is gonna be enough as the minimal speed
        currentTick += gameSpeed;
        System.out.println(gameSpeed);
        while (currentTick > 30) {
            passes++;
            currentTick -= 30;
        }
        
        // objects that need to be updated however fast the clock is running
        for (; passes > 0; passes--) {
            map.update(deltaTime);
            armyManager.update(deltaTime);
            gameplayUI.tickCalendar();
        }
    }

    public void incrementGameSpeed() {
        gameSpeed++;
    }

    public void decrementGameSpeed() {
        gameSpeed--;
    }

    public void togglePause() {
        gamePaused = !gamePaused;
    }

    public boolean isGamePaused() {
        return gamePaused; 
    }

}
