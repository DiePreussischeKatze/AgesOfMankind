package org.src.components.province;

import earcut4j.Earcut;
import org.joml.Vector2f;
import org.src.core.helper.Consts;
import org.src.core.helper.Helper;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;
import org.src.rendering.wrapper.ShaderStorage;

import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;

public final class Province {
	// TODO: make some of them not private
	public static final float POINT_SIZE;

	private final Mesh mesh;
	private final Mesh boxMesh;

	private final ShaderStorage shaderStorage;

	// for optimization
	// TODO: use for mouse selection optimization
	private final Vector2f minPos;
	private final Vector2f maxPos;

	private final float[] color;
	private float[] pointsPositions;

	private boolean drawFill;
	private boolean drawPoints;

	static {
		POINT_SIZE = 0.0008f;
	}

	public Province() {
		drawFill = drawPoints = true;

		pointsPositions = new float[0];

		color = new float[3];
		Arrays.fill(color, 0.5f);

		minPos = new Vector2f();
		maxPos = new Vector2f();

		shaderStorage = new ShaderStorage(1);
		mesh = new Mesh(new byte[] {2, 3});

		boxMesh = Helper.createPlainBoxMesh(POINT_SIZE, POINT_SIZE);
	}

	/**
	 * @return the index of the point (not the index in the array!)
	 */
	public int isInAnyPoint(final float x, final float y) {
		for (int i = 0; i < pointsPositions.length; i += Consts.POINT_POSITION_STRIDE) {
			final float pointX = pointsPositions[i] - POINT_SIZE;
			final float pointY = pointsPositions[i + 1] - POINT_SIZE;
			// width = height = POINT_SIZE
			if (x < pointX + POINT_SIZE * 2 && x > pointX && y < pointY + POINT_SIZE * 2 && y > pointY) {
				return i / Consts.POINT_POSITION_STRIDE;
			}
		}
		return -1; // the coordinates don't overlap with any point
	}

	public void addPoint(final float x, final float y) {
		if (pointAlreadyExists(x, y) != -1) { return; } // we don't want duplicate points

		pointsPositions = Helper.addElementsToFloatArray(pointsPositions, new float[] {x, y});
		mesh.setVertices(Helper.addElementsToFloatArray(mesh.getVertices(), new float[] {
				x, y, color[0], color[1], color[2]
		}));
		refreshMesh();
		refreshMaxPoints();
	}

	/**
	 * @return the index of the point (not the index in the array!)
	 */
	private int pointAlreadyExists(final float x, final float y) {
		for (int i = 0; i < pointsPositions.length; i += Consts.POINT_POSITION_STRIDE) {
			if (pointsPositions[i] == x && pointsPositions[i + 1] == y) { return i / Consts.POINT_POSITION_STRIDE; }
		}

		return -1; // the point doesn't exist at these coordinates
	}

	public void deletePoint(final int index) {
		pointsPositions = Helper.deleteElementsFromFloatArray(pointsPositions, index * Consts.POINT_POSITION_STRIDE, Consts.POINT_POSITION_STRIDE);
		mesh.setVertices(Helper.deleteElementsFromFloatArray(mesh.getVertices(), index * mesh.getStrideSum(), mesh.getStrideSum()));
		refreshMaxPoints();
		refreshMesh();
	}

	public boolean pointInProvince(final Vector2f point) {
		for (int i = 0; i < mesh.getIndices().length; i += 3) {
			if (Helper.pointTriangleIntersection(point,
					new Vector2f(pointsPositions[mesh.getIndices()[i] * 2],     pointsPositions[mesh.getIndices()[i] * 2 + 1]),
					new Vector2f(pointsPositions[mesh.getIndices()[i + 1] * 2], pointsPositions[mesh.getIndices()[i + 1] * 2 + 1]),
					new Vector2f(pointsPositions[mesh.getIndices()[i + 2] * 2], pointsPositions[mesh.getIndices()[i + 2] * 2 + 1])
			)) {
				return true;
			}
		}
		return false;
	}

	private void refreshMaxPoints() {
		for (int i = 0; i < pointsPositions.length; i += Consts.POINT_POSITION_STRIDE) {
			// i is x, i + 1 i y
			if (pointsPositions[i] > maxPos.x) {
				maxPos.x = pointsPositions[i];
			} else if (pointsPositions[i] < minPos.x) {
				minPos.x = pointsPositions[i];
			}

			if (pointsPositions[i + 1] > maxPos.y) {
				maxPos.y = pointsPositions[i + 1];
			} else if (pointsPositions[i + 1] < minPos.y) {
				minPos.y = pointsPositions[i + 1];
			}
		}
	}

	/**
	 * Places a point in between the point a the specified index (not the index in the array!) and the point at index - 1 (again, not the array index)
	 */
	public void insertPointBackwards(final int index) {
		if (index * Consts.POINT_POSITION_STRIDE - Consts.POINT_POSITION_STRIDE < 0) { return; }

		final Vector2f averagePosition = new Vector2f(
				(pointsPositions[index * Consts.POINT_POSITION_STRIDE] + pointsPositions[index * Consts.POINT_POSITION_STRIDE - Consts.POINT_POSITION_STRIDE]) / 2,
				(pointsPositions[index * Consts.POINT_POSITION_STRIDE + 1] + pointsPositions[index * Consts.POINT_POSITION_STRIDE - Consts.POINT_POSITION_STRIDE + 1]) / 2
		);

		// FIXME: this works bizarre
		pointsPositions = Helper.insertElementsToFloatArray(pointsPositions, index * Consts.POINT_POSITION_STRIDE, new float[] {averagePosition.x, averagePosition.y});
		mesh.setVertices(Helper.insertElementsToFloatArray(mesh.getVertices(), index * mesh.getStrideSum(), new float[] {averagePosition.x, averagePosition.y, color[0], color[1], color[2]}));
		refreshMesh();
		// there's no need for refreshMaxPoints()
	}

	public void deleteLastPoint() {
		pointsPositions = Helper.truncateFloatArray(pointsPositions, pointsPositions.length - Consts.POINT_POSITION_STRIDE);
		mesh.setVertices(Helper.truncateFloatArray(mesh.getVertices(), mesh.getVertices().length - mesh.getStrideSum()));
		refreshMesh();
		refreshMaxPoints();
	}

	public void refreshMesh() {
		final List<Integer> newIndices = Earcut.earcut(Helper.toDoubleArray(pointsPositions));
		mesh.setIndices(newIndices.stream().mapToInt(Integer::intValue).toArray());
		shaderStorage.regenerate(pointsPositions);
		mesh.regenerate();
	}

	public void clearProvincePoints() {
		pointsPositions = new float[0];
		mesh.clear();
		mesh.regenerate();
	}

	public void drawAlone() {
		if (drawPoints) {
			ShaderManager.get(ShaderID.PIVOT).bind();
			shaderStorage.bind();
			boxMesh.bind();
			glDrawElementsInstanced(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0, getPivotAmount());
		}

		if (drawFill) {
			glEnable(GL_BLEND);
			glBlendFunc(GL_SRC_ALPHA, GL_SRC_COLOR);
			ShaderManager.get(ShaderID.POLYGON).bind();
			mesh.draw();
			glDisable(GL_BLEND);
		}
	}

	public void updateColor() {
		for (int i = 0; i < mesh.getVertices().length; i += mesh.getStrideSum()) {
			// i + 2 for the first 2 indices are reserved for position
			mesh.getVertices()[i + 2] = color[0];
			mesh.getVertices()[i + 3] = color[1];
			mesh.getVertices()[i + 4] = color[2];
		}
		mesh.regenerate();
	}

	public void update() {

	}

	public void dispose() {
		boxMesh.dispose();
		shaderStorage.dispose();
	}

	public int getPivotAmount() {
		return pointsPositions.length / Consts.POINT_POSITION_STRIDE;
	}

	public int[] getIndices() {
		return mesh.getIndices();
	}

	public int getMeshStride() {
		return mesh.getStrideSum();
	}

	public float[] getVertices() {
		return mesh.getVertices();
	}

	public float[] getColor() {
		return color;
	}

	public float[] getPointsPositions() {
		return pointsPositions;
	}

	public boolean getDrawFill() {
		return drawFill;
	}

	public void setDrawFill(boolean drawFill) {
		this.drawFill = drawFill;
	}

	public boolean getDrawPoints() {
		return drawPoints;
	}

	public void setDrawPoints(boolean drawPoints) {
		this.drawPoints = drawPoints;
	}

}
