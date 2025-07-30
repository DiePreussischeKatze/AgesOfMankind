package org.src.components.ui.editor;

import imgui.ImGui;
import imgui.type.ImDouble;
import imgui.type.ImInt;
import org.joml.Vector2f;
import org.src.components.map.DisplayMode;
import org.src.components.map.Map;
import org.src.components.province.Province;
import org.src.components.province.ProvinceType;
import org.src.core.helper.Consts;
import org.src.core.helper.Rect2D;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;

import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;

import static org.src.components.ui.editor.EditorWindow.randomizeValue;
import static org.src.core.helper.Helper.isInImGuiWindow;

public final class PaintProvincesMode extends EditorMode {

	private final Rect2D brushSize;
	private final Mesh boxMesh;

	private float brushGrowth;

	private boolean mousePressed;

	private ProvinceType provinceType;

	private PaintingMode mode;

	private ImInt editedStateID;

	private static final int NO_STATE_EDITED = -1;

	private final Map map;
	public PaintProvincesMode(final Editor editor, final Map map) {
		super(editor);
		this.map = map;

		editor.deselectPoint();

		this.editedStateID = new ImInt(NO_STATE_EDITED);

		this.brushGrowth = 0;
		this.mousePressed = false;

		this.mode = PaintingMode.ADD;
		this.provinceType = ProvinceType.DEEP_SEA;

		this.boxMesh = new Mesh(new byte[] { 2 });
		this.boxMesh.indices = Consts.RECT_INDICES;
		this.brushSize = new Rect2D(editor.getAdjustedPos().x - 0.01f, editor.getAdjustedPos().y - 0.01f, 0.02f, 0.02f);
		updateMesh();

		editor.getSelection().setEnabled(false);
	}

	@Override
	public void keyPressAction(int key) {
		keyHeld(key, true);
	}

	@Override
	public void keyReleaseAction(int key) {
		keyHeld(key, false);
	}

	private void keyHeld(final int key, final boolean value) {
		switch (key) {
			case GLFW_KEY_MINUS:
				brushGrowth = value ? -0.001f : 0;
				break;
			case GLFW_KEY_EQUAL:
				brushGrowth = value ? 0.001f : 0;
				break;
		}
	}

	private void updateMesh() {
		boxMesh.vertices = new float[] {
				brushSize.getX() - brushSize.getWidth(), brushSize.getY() - brushSize.getHeight(),
				brushSize.getX() - brushSize.getWidth(), brushSize.getY() + brushSize.getHeight(),
				brushSize.getX() + brushSize.getWidth(), brushSize.getY() + brushSize.getHeight(),
				brushSize.getX() + brushSize.getWidth(), brushSize.getY() - brushSize.getHeight(),
		};
		boxMesh.regenerate();
	}

	@Override
	public void mouseMovedAction() {
		updateMesh();
	}

	@Override
	public void mouseLeftPressedAction() {
		this.mousePressed = true;
	}

	@Override
	public void mouseLeftReleasedAction() {
		this.mousePressed = false;
	}

	@Override
	public void mouseRightPressedAction() {
	}

	@Override
	public void mouseRightReleasedAction() {

	}

	@Override
	public void renderGui() {
		if (ImGui.checkbox("ADD painting mode", mode == PaintingMode.ADD)) {
			mode = PaintingMode.ADD;
		}

		if (ImGui.checkbox("SET painting mode", mode == PaintingMode.SET)) {
			mode = PaintingMode.SET;
		}

		ImGui.separator();

		if (ImGui.button("Randomize values")) {
			randomizeValues();
		}

		editor.setPaintADDValue(inputDouble("ADD value", editor.getPaintADDValue()));
		editor.setPaintSETValue(inputDouble("SET value", editor.getPaintSETValue()));

		ImGui.separator();

		if (map.getDisplayMode() == DisplayMode.TERRAIN) {
			final ImInt terrainType = new ImInt(this.provinceType.ordinal());

			if (ImGui.combo("Terrain type", terrainType, Consts.PROVINCE_TYPE_STRINGS)) {
				provinceType = ProvinceType.values()[terrainType.get()];
			}
		}

		if (map.getDisplayMode() == DisplayMode.POLITICAL) {
			final String[] stateNames = new String[map.getStates().size()];

			for (int i = 0; i < stateNames.length; i++) {
				// OOP in its full glory
				stateNames[i] = map.getStates().get(i).getName().get();
			}

			ImGui.combo("States", editedStateID, stateNames);

			if (editedStateID.get() != NO_STATE_EDITED && ImGui.inputText("State name", map.getStates().get(editedStateID.get()).getName())) {

			}

			if (ImGui.button("Add state")) {
				map.addState();
			}

			if (editedStateID.get() != NO_STATE_EDITED && ImGui.colorPicker3("State color", map.getStates().get(editedStateID.get()).getColor())) {
				map.getStates().get(editedStateID.get()).updateColor();
				// update the color of the provinces in the mesh
				for (final Province province: map.getStates().get(editedStateID.get()).getOwnedProvinces()) {
					map.setProvinceInMeshColor(province, province.getColor());
				}
				map.updateMesh();
			}

		}

	}

	private double inputDouble(final String label, final double value) {
		final ImDouble d = new ImDouble(value);
		if (ImGui.inputDouble(label, d, 0)) {
			return d.get();
		}
		return value;
	}

	@Override
	public void update(final double deltaTime) {
		updateBrush(deltaTime);
		if (mousePressed) { paint(deltaTime); }
	}

	private void randomizeValues() {
		for (final Province province: map.getProvinces()) {
			province.populationCount = randomizeValue(province.populationCount);
		}
	}

	private boolean isStateEdited() {
		return editedStateID.get() != NO_STATE_EDITED;
	}

	private void unselectEditedState() {
		editedStateID.set(NO_STATE_EDITED);
	}

	private void paint(final double deltaTime) {
		// due to my stupid implementation we need to split the box mesh into a grid of points
		final ArrayList<Vector2f> points = new ArrayList<>();
		// First time I ever did a for loop on a float (looks a bit cursed)
		for (float xx = brushSize.getX() - brushSize.getWidth(); xx < brushSize.getX() + brushSize.getWidth(); xx += 0.002f) {
			for (float yy = brushSize.getY() - brushSize.getHeight(); yy < brushSize.getY() + brushSize.getHeight(); yy += 0.002f) {
				points.add(new Vector2f(xx, yy));
			}
		}
		//PerfTimer t1 = new PerfTimer("initial display update");
		// more like updating the display
		editor.changeMapDisplay(map.getDisplayMode());
		//t1.reset();

		//PerfTimer t2 = new PerfTimer("painting");
		if (mode == PaintingMode.SET) {
			// this will not be efficient
			for (final Province province: map.getProvinces()) {
				if (!province.intersectsMaxRectangle(brushSize)) { continue; } // some optimization

				for (final Vector2f point: points) {
					if (province.isInProvince(point)) {
						SETEditParam(province);
						break;
					}
				}
			}
		} else if (mode == PaintingMode.ADD) {

			for (final Province province: map.getProvinces()) {
				if (!province.intersectsMaxRectangle(brushSize)) { continue; }

				for (final Vector2f point: points) {
					if (province.isInProvince(point)) {
						ADDEditParam(province, deltaTime);
						break;
					}
				}
			}
		}
	//	t2.reset();
		map.findMaxParams();
	}

	private void updateBrush(final double deltaTime) {
		if (brushGrowth != 0) {
			brushSize.setWidth((float) Math.max(brushSize.getWidth() + brushGrowth * deltaTime, 0.01));
			brushSize.setHeight((float) Math.max(brushSize.getHeight() + brushGrowth * deltaTime, 0.01));
		}
		brushSize.setX(editor.getAdjustedPos().x);
		brushSize.setY(editor.getAdjustedPos().y);
		updateMesh();
	}

	@Override
	public void draw() {
		if (isInImGuiWindow()) { return; }

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_SRC_COLOR);
		ShaderManager.get(ShaderID.BRUSH).bind();
		ShaderManager.get(ShaderID.BRUSH).setFloat4("color", 1F, 1F, 1F, 0.5f);
		boxMesh.draw();
		glDisable(GL_BLEND);
	}

	@Override
	public void dispose() {
		boxMesh.dispose();
	}

	// It sucks java doesn't have references
	// TODO: Figure out how to put 'em into a single switch
	private void ADDEditParam(final Province province, final double deltaTime) {
		final double ADDValue = editor.getPaintADDValue() * deltaTime;
		switch (map.getDisplayMode()) {
			case POPULATION -> {
				if (Province.isSeaType(province)) { break; }
				province.populationCount += (int) ADDValue;
			}
			case ELEVATION -> province.elevation += (int) ADDValue;
			// TODO: REMEMBER TO CHANGE THE OTHER SWITCH!!!!!!!!!!!!!!!!!!!
			// TODO: REMEMBER TO CHANGE THE OTHER SWITCH!!!!!!!!!!!!!!!!!!!
			// TODO: REMEMBER TO CHANGE THE OTHER SWITCH!!!!!!!!!!!!!!!!!!!
		}
	}

	private void SETEditParam(final Province province) {
		final double SETValue = editor.getPaintSETValue();
		switch (map.getDisplayMode()) {
			case POPULATION -> {
				if (Province.isSeaType(province)) { break; }

				province.populationCount = (int) SETValue;
			}
			case ELEVATION -> province.elevation = (int) SETValue;
			case TERRAIN -> province.setType(provinceType);
			case POLITICAL -> {
				if (editedStateID.get() != NO_STATE_EDITED) {
					map.getStates().get(editedStateID.get()).addProvince(province);
					// update the province in the mesh (we know no province is selected rn)
					map.setProvinceInMeshColor(province, province.getColor());
					map.updateMesh();
				}
			}
			// TODO: REMEMBER TO CHANGE THE OTHER SWITCH!!!!!!!!!!!!!!!!!!!
			// TODO: REMEMBER TO CHANGE THE OTHER SWITCH!!!!!!!!!!!!!!!!!!!
			// TODO: REMEMBER TO CHANGE THE OTHER SWITCH!!!!!!!!!!!!!!!!!!!
		}
	}

}
