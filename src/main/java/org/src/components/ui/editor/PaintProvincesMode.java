package org.src.components.ui.editor;

import imgui.type.ImString;
import org.joml.Vector2f;
import org.src.components.map.Map;
import org.src.components.province.Province;
import org.src.core.helper.Consts;
import org.src.core.helper.Rect2D;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;

import java.sql.SQLOutput;
import java.util.ArrayList;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;
import static org.src.core.helper.Helper.isInImGuiWindow;

public final class PaintProvincesMode extends EditorMode {

	private Rect2D brushSize;
	private Mesh boxMesh;

	private float brushGrowth;

	private boolean mousePressed;

	private ImString paramName;
	private double editedValue;

	private PaintingMode mode;

	private final Map map;
	public PaintProvincesMode(final Editor editor, final Map map) {
		super(editor);
		this.map = map;

		this.brushGrowth = 0;
		this.mousePressed = false;

		this.paramName = new ImString(50);
		this.editedValue = 0;
		this.mode = PaintingMode.SET;

		this.boxMesh = new Mesh(new byte[]{2});
		this.boxMesh.indices = Consts.RECT_INDICES;
		this.brushSize = new Rect2D(editor.getAdjustedPos().x - 0.01f, editor.getAdjustedPos().y - 0.01f, 0.02f, 0.02f);
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
				editor.getAdjustedPos().x - brushSize.getWidth(), editor.getAdjustedPos().y - brushSize.getHeight(),
				editor.getAdjustedPos().x - brushSize.getWidth(), editor.getAdjustedPos().y + brushSize.getHeight(),
				editor.getAdjustedPos().x + brushSize.getWidth(), editor.getAdjustedPos().y + brushSize.getHeight(),
				editor.getAdjustedPos().x + brushSize.getWidth(), editor.getAdjustedPos().y - brushSize.getHeight(),
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

	}

	@Override
	public void update(final double deltaTime) {
		updateBrush(deltaTime);
		if (mousePressed) { paint(deltaTime); }
	}

	private void paint(final double deltaTime) {
		// due to my stupid implementation we need to split the box mesh into a grid of points
		final ArrayList<Vector2f> points = new ArrayList<>();
		// First time I ever did a for loop on a float (looks a bit cursed)
		for (float xx = brushSize.getX(); xx < brushSize.getX() + brushSize.getWidth(); xx += 0.005f) {
			for (float yy = brushSize.getY(); yy < brushSize.getY() + brushSize.getHeight(); yy += 0.005f) {
				points.add(new Vector2f(xx, yy));
			}
		}

		if (mode == PaintingMode.SET) {
			// this will not be efficient
			for (final Province province: map.getProvinces()) {
				for (final Vector2f point: points) {
					if (province.isInProvince(point)) {
						// TODO: Make it to something
						break;
					}
				}
			}
		} else if (mode == PaintingMode.ADD) {
			for (final Province province: map.getProvinces()) {
				for (final Vector2f point: points) {
					if (province.isInProvince(point)) {
						// TODO: Also make it do something
						break;
					}
				}
			}
		}
	}

	private void updateBrush(final double deltaTime) {
		if (brushGrowth != 0) {
			brushSize.setWidth((float) Math.max(brushSize.getWidth() + brushGrowth * deltaTime, 0));
			brushSize.setHeight((float) Math.max(brushSize.getHeight() + brushGrowth * deltaTime, 0));
			updateMesh();
		}
		brushSize.setX(editor.getAdjustedPos().x);
		brushSize.setY(editor.getAdjustedPos().y);
	}

	@Override
	public void draw() {
		if (isInImGuiWindow()) { return; }

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_SRC_COLOR);
		ShaderManager.get(ShaderID.EDITOR).bind();
		ShaderManager.get(ShaderID.EDITOR).setFloat3("color", 1F, 1F, 1F);
		boxMesh.draw();
		glDisable(GL_BLEND);
	}

}
