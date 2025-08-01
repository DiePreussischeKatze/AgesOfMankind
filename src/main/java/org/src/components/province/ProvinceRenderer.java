package org.src.components.province;

import earcut4j.Earcut;
import org.joml.Vector2f;
import org.src.core.helper.Consts;
import org.src.core.helper.Helper;
import org.src.core.helper.Rect2D;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;
import org.src.rendering.wrapper.ShaderStorage;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.src.core.helper.Helper.INT_ARR;

public final class ProvinceRenderer {

	public static final float POINT_SIZE;

	private final Mesh mesh;
	private final Mesh boxMesh;

	private final ShaderStorage shaderStorage;

	// for optimization
	private final Vector2f minPos;
	private final Vector2f maxPos;

	private final float[] color;
	private float[] pointsPoses; // I mean I could just loop over the vertices but that would complicate stuff a
					// lot

	private int vertexIndex;
	private int indicesIndex;

	static {
		POINT_SIZE = 0.0008f;
	}

	ProvinceRenderer() {
		this.pointsPoses = new float[0];
		this.color = new float[3];
		Arrays.fill(color, 0.5f);

		this.vertexIndex = indicesIndex = -1;

		this.minPos = new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
		this.maxPos = new Vector2f(Float.MIN_VALUE, Float.MIN_VALUE);

		this.shaderStorage = new ShaderStorage(1);
		this.mesh = new Mesh(new byte[] { 2, 3 });

		this.boxMesh = Helper.createPlainBoxMesh(POINT_SIZE, POINT_SIZE);
	}

	/**
	 * @return the index of the point (not the index in the array!)
	 */
	public int isInAnyPoint(final float x, final float y) {
		for (int i = 0; i < pointsPoses.length; i += Consts.POINT_POS_STRIDE) {
			final float pointX = pointsPoses[i] - POINT_SIZE;
			final float pointY = pointsPoses[i + 1] - POINT_SIZE;
			// width = height = POINT_SIZE
			if (x < pointX + POINT_SIZE * 2 && x > pointX && y < pointY + POINT_SIZE * 2 && y > pointY) {
				return i / Consts.POINT_POS_STRIDE;
			}
		}
		return -1; // the coordinates don't overlap with any point
	}

	public void addPoint(final float x, final float y) {
		if (pointExists(x, y) != -1) {
			return;
		} // we don't want duplicate points

		pointsPoses = Helper.addElementsToFloatArray(pointsPoses, new float[] { x, y });
		mesh.addVertices(new float[] { x, y, color[0], color[1], color[2] });
		refreshMesh();
		refreshMaxPoints();
	}

	/**
	 * @return the index of the point (not the index in the array!)
	 */
	private int pointExists(final float x, final float y) {
		for (int i = 0; i < pointsPoses.length; i += Consts.POINT_POS_STRIDE) {
			if (pointsPoses[i] == x && pointsPoses[i + 1] == y) {
				return i / Consts.POINT_POS_STRIDE;
			}
		}

		return -1; // the point doesn't exist at these coordinates
	}

	public void deletePoint(final int id) {
		deletePointWithoutRefresh(id);
		refreshMaxPoints();
		refreshMesh();
	}

	public void deletePointWithoutRefresh(final int id) {
		pointsPoses = Helper.deleteElementsFromFloatArray(pointsPoses, id * Consts.POINT_POS_STRIDE,
				Consts.POINT_POS_STRIDE);
		mesh.vertices = Helper.deleteElementsFromFloatArray(mesh.vertices, id * mesh.getStrideSum(),
				mesh.getStrideSum());
	}

	public boolean isInProvince(final Vector2f point) {
		// check if it even makes sense to do the other checks
		if (isInMaxPoints(point)) {
			for (int i = 0; i < mesh.indices.length; i += 3) {
				// calculate the bounds of the triangle (fix and also an optimization)
				final Vector2f triangleMax = new Vector2f(
					Helper.max(pointsPoses[mesh.indices[i] * Consts.POINT_POS_STRIDE],
							pointsPoses[mesh.indices[i + 1] * Consts.POINT_POS_STRIDE],
							pointsPoses[mesh.indices[i + 2] * Consts.POINT_POS_STRIDE]),
					Helper.max(pointsPoses[mesh.indices[i] * Consts.POINT_POS_STRIDE + 1],
							pointsPoses[mesh.indices[i + 1] * Consts.POINT_POS_STRIDE + 1],
							pointsPoses[mesh.indices[i + 2] * Consts.POINT_POS_STRIDE + 1])
				);
				final Vector2f triangleMin = new Vector2f(
					Helper.min(pointsPoses[mesh.indices[i] * Consts.POINT_POS_STRIDE],
						pointsPoses[mesh.indices[i + 1] * Consts.POINT_POS_STRIDE],
						pointsPoses[mesh.indices[i + 2] * Consts.POINT_POS_STRIDE]),
					Helper.min(pointsPoses[mesh.indices[i] * Consts.POINT_POS_STRIDE + 1],
						pointsPoses[mesh.indices[i + 1] * Consts.POINT_POS_STRIDE + 1],
						pointsPoses[mesh.indices[i + 2] * Consts.POINT_POS_STRIDE + 1])
				);

				if (point.x > triangleMin.x && point.x < triangleMax.x && point.y > triangleMin.y && point.y < triangleMax.y) {
					if (Helper.pointTriangleIntersection(point,
							new Vector2f(pointsPoses[mesh.indices[i] * Consts.POINT_POS_STRIDE], pointsPoses[mesh.indices[i] * Consts.POINT_POS_STRIDE + 1]),
							new Vector2f(pointsPoses[mesh.indices[i + 1] * Consts.POINT_POS_STRIDE], pointsPoses[mesh.indices[i + 1] * Consts.POINT_POS_STRIDE + 1]),
							new Vector2f(pointsPoses[mesh.indices[i + 2] * Consts.POINT_POS_STRIDE], pointsPoses[mesh.indices[i + 2] * Consts.POINT_POS_STRIDE + 1]))
					) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void refreshMaxPoints() {
		for (int i = 0; i < pointsPoses.length; i += Consts.POINT_POS_STRIDE) {
			// i is x, i + 1 i y
			if (pointsPoses[i] > maxPos.x) {
				maxPos.x = pointsPoses[i];
			} else if (pointsPoses[i] < minPos.x) {
				minPos.x = pointsPoses[i];
			}

			if (pointsPoses[i + 1] > maxPos.y) {
				maxPos.y = pointsPoses[i + 1];
			} else if (pointsPoses[i + 1] < minPos.y) {
				minPos.y = pointsPoses[i + 1];
			}
		}
	}

	/**
	 * Places a point in between the point a the specified index (not the index in
	 * the array!) and the point at index - 1 (again, not the array index)
	 */
	public void insertPointBackwards(final int index) {
		if (index * Consts.POINT_POS_STRIDE - Consts.POINT_POS_STRIDE < 0) {
			return;
		}

		final Vector2f averagePosition = new Vector2f(
				(pointsPoses[index * Consts.POINT_POS_STRIDE] + pointsPoses[index * Consts.POINT_POS_STRIDE - Consts.POINT_POS_STRIDE]) / 2,
				(pointsPoses[index * Consts.POINT_POS_STRIDE + 1] + pointsPoses[index * Consts.POINT_POS_STRIDE - Consts.POINT_POS_STRIDE + 1]) / 2
		);

		pointsPoses = Helper.insertElementsToFloatArray(pointsPoses, index * Consts.POINT_POS_STRIDE,
				new float[] { averagePosition.x, averagePosition.y });
		mesh.vertices = Helper.insertElementsToFloatArray(mesh.vertices, index * mesh.getStrideSum(),
				new float[] { averagePosition.x, averagePosition.y, color[0], color[1], color[2] });
		refreshMesh();
		// there's no need for refreshMaxPoints()
	}

	public void deleteLastPoint() {
		pointsPoses = Helper.truncateFloatArray(pointsPoses, pointsPoses.length - Consts.POINT_POS_STRIDE);
		mesh.vertices = Helper.truncateFloatArray(mesh.vertices, mesh.vertices.length - mesh.getStrideSum());
		refreshMesh();
		refreshMaxPoints();
	}

	public void refreshMesh() {
		final List<Integer> newIndices = Earcut.earcut(Helper.toDoubleArray(pointsPoses));
		mesh.indices = newIndices.stream().mapToInt(Integer::intValue).toArray();
		shaderStorage.regenerate(pointsPoses);
		mesh.regenerate();
	}

	public void clearProvincePoints() {
		pointsPoses = new float[0];
		shaderStorage.regenerate(pointsPoses);
		mesh.clear();
		mesh.regenerate();
	}

	public void drawAlone(final boolean drawPoints, final boolean drawFill) {
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
		for (int i = 0; i < mesh.vertices.length; i += mesh.getStrideSum()) {
			// i + 2 for the first 2 indices are reserved for position
			mesh.vertices[i + 2] = color[0];
			mesh.vertices[i + 3] = color[1];
			mesh.vertices[i + 4] = color[2];
		}
		mesh.regenerateGeometry();
	}

	public void shallowSetColor(final float[] color) {
		for (int i = 0; i < mesh.vertices.length; i += mesh.getStrideSum()) {
			// i + 2 for the first 2 indices are reserved for position
			mesh.vertices[i + 2] = color[0];
			mesh.vertices[i + 3] = color[1];
			mesh.vertices[i + 4] = color[2];
		}
		mesh.regenerate();
	}

	public int[] getIntersectedPointIndices(final Rect2D what) {
		final ArrayList<Integer> points = new ArrayList<>();

		for (int i = 0; i < pointsPoses.length; i += Consts.POINT_POS_STRIDE) {

			if (what.intersects(new Rect2D(
					pointsPoses[i],
					pointsPoses[i + 1],
					0.000001f,
					0.000001f))) {
				points.add(i);
				points.add(i + 1);
			}
		}

		return INT_ARR(points);
	}

	/*
	 * @param hwat the rectangle to check collisions for
	 * 
	 * @return the index in the pointPoses array of the x value of the point (-1 if
	 * we don't hit anything)
	 */
	public int getFirstIntersectedPointIndex(final Rect2D hwat) {
		if (!hwat.intersects(minPos.x, minPos.y, maxPos.x - minPos.x, maxPos.y - minPos.y)) {
			return -1;
		}

		for (int i = 0; i < pointsPoses.length; i += Consts.POINT_POS_STRIDE) {
			if (hwat.intersects(
					pointsPoses[i],
					pointsPoses[i + 1],
					0.000001f,
					0.000001f)) {
				return i;
			}
		}

		return -1;
	}

	public boolean intersectsMaxRectangle(final Rect2D hwat) {
		return hwat.intersects(minPos.x, minPos.y, maxPos.x - minPos.x, maxPos.y - minPos.y);
	}

	private boolean isInMaxPoints(final float x, final float y) {
		return x > minPos.x && x < maxPos.x && y > minPos.y && y < maxPos.y;
	}

	private boolean isInMaxPoints(final Vector2f point) {
		return point.x > minPos.x && point.x < maxPos.x && point.y > minPos.y && point.y < maxPos.y;
	}

	public void dispose() {
		boxMesh.dispose();
		shaderStorage.dispose();
	}

	public void setColor(final float[] color) {
		this.color[0] = color[0];
		this.color[1] = color[1];
		this.color[2] = color[2];

		updateColor();
	}

	/**
	 * The function only sets the data without regenerating the mesh
	 */
	public void setVertices(final float[] data) {
		mesh.vertices = data;
	}

	public void setPointsPoses(final float[] data) {
		pointsPoses = data;
	}

	public void setIndicesIndex(int indicesIndex) {
		this.indicesIndex = indicesIndex;
	}

	public void setVertexIndex(int vertexIndex) {
		this.vertexIndex = vertexIndex;
	}

	public int getVertexIndex() {
		return vertexIndex;
	}

	public int getIndicesIndex() {
		return indicesIndex;
	}

	public int getPivotAmount() {
		return pointsPoses.length / Consts.POINT_POS_STRIDE;
	}

	public int[] getIndices() {
		return mesh.indices;
	}

	public int getMeshStride() {
		return mesh.getStrideSum();
	}

	public float[] getVertices() {
		return mesh.vertices;
	}

	public float[] getColor() {
		return color;
	}

	public float[] getPointsPoses() {
		return pointsPoses;
	}

	public Rect2D getMaxPoints() {
		return new Rect2D(minPos.x, minPos.y, maxPos.x - minPos.x, maxPos.y - minPos.y);
	}

	public void setColorValue(final float[] color) {
		this.color[0] = color[0];
		this.color[1] = color[1];
		this.color[2] = color[2];
	}

}
