package org.src.scenes;

import static org.lwjgl.glfw.GLFW.*;

import org.src.components.Camera;
import org.src.components.ScenarioSaver;
import org.src.components.Selection;
import org.src.components.map.Map;
import org.src.components.ui.gameplay.ArmyManager;
import org.src.components.ui.gameplay.GameplayUI;
import org.src.core.callbacks.KeyPressCallback;
import org.src.core.helper.Scene;
import org.src.core.managers.InputManager;

public final class GameplayScene extends Scene {
  	private int gameSpeed;
	private boolean gamePaused;
	private int currentTick;
  
    private final Camera camera;
    private final Map map;
    private final Selection selection;
    private final ArmyManager armyManager;
    private final GameplayUI gameplayUI;

    private final KeyPressCallback keyPressed = (long window, int key, int action, int mods) -> {
        switch (key) {
            case GLFW_KEY_P -> togglePause();
            case GLFW_KEY_EQUAL -> incrementGameSpeed(); // the '=+' on the keyboard
            case GLFW_KEY_MINUS -> decrementGameSpeed(); // the '-_' on the keyboard
        }
    };

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

        InputManager.addKeyPressCallback(keyPressed);
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

        while (currentTick > 30) {
            passes++;
            currentTick -= 30;
        }
        
        // objects that need to be updated however fast the clock is running
        for (; passes > 0; passes--) {
            map.update(1);
            armyManager.update(1);
            gameplayUI.tickCalendar();
        }
    }

    public void incrementGameSpeed() {
        gameSpeed++;
    }

    public void incrementGameSpeed(final int value) {
        gameSpeed += value;
    }

    public void decrementGameSpeed() {
        if (gameSpeed < 1) { return; }
        gameSpeed--;
    }

    public void decrementGameSpeed(final int value) {
        gameSpeed -= value;
        if (gameSpeed < 0) {
            gameSpeed = 0;
        }
    }

    public void togglePause() {
        gamePaused = !gamePaused;
    }

    public boolean isGamePaused() {
        return gamePaused;
    }

}
