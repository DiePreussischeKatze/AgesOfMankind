package org.src.components.ui.gameplay;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2f;
import org.src.components.Camera;
import org.src.components.Selection;
import org.src.components.map.Map;
import org.src.core.callbacks.KeyPressCallback;
import org.src.core.callbacks.MouseLeftPressCallback;
import org.src.core.callbacks.MouseLeftReleaseCallback;
import org.src.core.callbacks.MouseMoveCallback;
import org.src.core.callbacks.MouseRightPressCallback;
import org.src.core.callbacks.MouseRightReleaseCallback;
import org.src.core.helper.Component;
import org.src.core.helper.Helper;
import org.src.core.helper.Rect2D;
import org.src.core.helper.ShaderID;
import org.src.core.managers.InputManager;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;
import org.src.rendering.wrapper.ShaderStorage;

public final class ArmyManager extends Component {
    
    // Template meshes that will be instance-rendered for every Army
    private final Mesh boxMesh; // for now we just wanna render a red box that I can control

    private Camera camera;
    private Selection selection;
    private Map map;

    private final ShaderStorage shaderStorage;

    private ArrayList<Army> armies;
    private ArrayList<Army> selectedArmies;

    private boolean movedMouseWhilePressing;

    private Vector2f lastMousePos;

    private final KeyPressCallback keyPressed = (long window, int key, int action, int mods) -> {
        switch (key) {
            case GLFW_KEY_R -> addArmy(new Army(camera.getAdjustedMousePos(), map.getStates().get(0))); 
            case GLFW_KEY_X -> clearSelectedArmies();
        }
    };

    private final MouseMoveCallback moveCallback = () -> {
        if (InputManager.leftPressed()) {
            movedMouseWhilePressing = true;
        }
    };

    private final MouseLeftPressCallback mouseLeftPress = () -> {
        clearSelectedArmies();
    
        // If an army is under the cursor, we'll select it
        for (final Army army: armies) {
            final Rect2D armyRect = new Rect2D(army.getPos().x - 0.002f, army.getPos().y - 0.002f, 0.004f, 0.004f);
            final Rect2D cursorRect = new Rect2D(camera.getAdjustedMousePos().x, camera.getAdjustedMousePos().y, 0.00001f, 0.00001f);
            if (armyRect.intersects(cursorRect)) {
                army.setSelected(true);
                selectedArmies.add(army);
                break;
            }
        }
    };

    private final MouseLeftReleaseCallback mouseLeftRelease = () -> {
        if (movedMouseWhilePressing) {
            for (final Army army: armies) {
                final Rect2D armyRect = new Rect2D(army.getPos().x - 0.002f, army.getPos().y - 0.002f, 0.004f, 0.004f);
                if (armyRect.intersects(selection.get()) && !selectedArmies.contains(army)) {
                    army.setSelected(true);
                    selectedArmies.add(army);
                }
            }
        }

        movedMouseWhilePressing = false;
    };

    private final MouseRightPressCallback mouseRightPress = () -> {
        lastMousePos.x = InputManager.getMouseX();
        lastMousePos.y = InputManager.getMouseY();
    };

    private final MouseRightReleaseCallback mouseRightRelease = () -> {
        if (lastMousePos.x != InputManager.getMouseX() || lastMousePos.y != InputManager.getMouseY()) { return; }

        for (final Army army: selectedArmies) {
            army.setOrderedPosition(camera.getAdjustedMousePos());
        }
    };

    public ArmyManager(final Camera camera, final Selection selection, final Map map) {
        this.camera = camera;
        this.selection = selection;
        this.map = map;

        this.lastMousePos = new Vector2f();

        selection.setEnabled(true);

        this.boxMesh = Helper.createPlainBoxMesh(0.002f, 0.002f);
    
        this.armies = new ArrayList<>();
        this.selectedArmies = new ArrayList<>();

        this.shaderStorage = new ShaderStorage(2);

        InputManager.addMouseMoveCallback(moveCallback);
        InputManager.addKeyPressCallback(keyPressed);
        InputManager.addMouseLeftPressCallback(mouseLeftPress);
        InputManager.addMouseLeftReleaseCallback(mouseLeftRelease);
        InputManager.addMouseRightPressCallback(mouseRightPress);
        InputManager.addMouseRightReleaseCallback(mouseRightRelease);
    }

    private void clearSelectedArmies() {
        for (final Army army: armies) {
            army.setSelected(false);
        }
        selectedArmies.clear();
    }

    public void addArmy(final Army... army) {
        this.armies.addAll(List.of(army));
    }

    @Override
    public void draw() {
        final int OFFSET = /*XY positions:*/ 2 + /*Is selected*/ 1; // we'll add more

        final float[] data = new float[armies.size() * OFFSET];

        int i = 0;

        for (final Army army: armies) {
            data[i    ] = army.getPos().x;
            data[i + 1] = army.getPos().y;
            data[i + 2] = army.isSelected() ? 1 : 0;

            i += OFFSET; // TODO: remember to change the offset
        }

        shaderStorage.regenerate(data);
        
        shaderStorage.bind();
        // render
        ShaderManager.get(ShaderID.ARMY).bind();
        boxMesh.bind();
        glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0, armies.size());
    }

    @Override
    public void update(final double deltaTime) {
        // move all the armies accordingly to their orders
        for (final Army army: armies) {
            army.update(deltaTime);
        }
    }

    @Override
    public void dispose() {
        boxMesh.dispose();
        shaderStorage.dispose();
    }
    
}
