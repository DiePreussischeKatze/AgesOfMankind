package org.src.components.province;

import imgui.type.ImString;
import org.joml.Vector2f;
import org.src.core.helper.Rect2D;

import java.util.HashSet;

public final class Province {
	private final ProvinceRenderer renderer;

	private final HashSet<Province> neighbors;

	public ImString name;

	public float averageTemperature;
	public float maxTemperature;
	public float minTemperature;
	public float climateCharacter; // 0.0 means totally continental, 1.0 means totally marine climate

	public int populationCount;
	public int elevation;

	public Province() {
		renderer = new ProvinceRenderer();
		neighbors = new HashSet<>();

		name = new ImString(50);

		this.populationCount = 0;
	}

	/**
	 * @return the index of the point (not the index in the array!)
	 */
	public int isInAnyPoint(final float x, final float y) {
		return renderer.isInAnyPoint(x, y);
	}

	public void addPoint(final float x, final float y) {
		renderer.addPoint(x, y);
	}

	public void deletePoint(final int id) {
		renderer.deletePoint(id);
	}

	public void deletePointWithoutRefresh(final int id) {
		renderer.deletePointWithoutRefresh(id);
	}

	public boolean isInProvince(final Vector2f point) {
		return renderer.isInProvince(point);
	}

	/**
	 * Places a point in between the point a the specified index (not the index in the array!) and the point at index - 1 (again, not the array index)
	 */
	public void insertPointBackwards(final int index) {
		renderer.insertPointBackwards(index);
	}

	public void deleteLastPoint() {
		renderer.deleteLastPoint();
	}

	public void refreshMesh() {
		renderer.refreshMesh();
	}

	public void refreshMaxPoints() {
		renderer.refreshMaxPoints();
	}

	public void clearProvincePoints() {
		renderer.clearProvincePoints();
	}

	public void drawAlone(final boolean doDrawPoints, final boolean doDrawFill) {
		renderer.drawAlone(doDrawPoints, doDrawFill);
	}

	public void updateColor() {
		renderer.updateColor();
	}

	public int[] getIntersectedPointIndices(final Rect2D what) {
		return renderer.getIntersectedPointIndices(what);
	}

	/**
	 * @param what the rectangle to check collisions for
	 * @return the index in the pointPoses array of the x value of the point (-1 if we don't hit anything)
	 */
	public int getFirstIntersectedPointIndex(final Rect2D what) {
		return renderer.getFirstIntersectedPointIndex(what);
	}

	public void dispose() {
		renderer.dispose();
	}

	public void setColor(final float[] color) {
		renderer.setColor(color);
	}

	/**
	 * The function only sets the data without regenerating the mesh
	 */
	public void setVertices(final float[] data) {
		renderer.setVertices(data);
	}

	public void setPointsPoses(final float[] data) {
		renderer.setPointsPoses(data);
	}

	public void setVertexIndex(int vertexIndex) {
		renderer.setVertexIndex(vertexIndex);
	}

	public void setIndicesIndex(int indicesIndex) {
		renderer.setIndicesIndex(indicesIndex);
	}

	public int getVertexIndex() {
		return renderer.getVertexIndex();
	}

	public int getIndicesIndex() {
		return renderer.getIndicesIndex();
	}

	public int getPivotAmount() {
		return renderer.getPivotAmount();
	}

	public int[] getIndices() {
		return renderer.getIndices();
	}

	public int getMeshStride() {
		return renderer.getMeshStride();
	}

	public float[] getVertices() {
		return renderer.getVertices();
	}

	public float[] getColor() {
		return renderer.getColor();
	}

	public float[] getPointsPoses() {
		return renderer.getPointsPoses();
	}

	public void addNeighbor(final Province province) {
		neighbors.add(province);
	}

	public boolean hasNeighbor(final Province province) {
		return neighbors.contains(province);
	}

	public Rect2D getMaxPoints() {
		return renderer.getMaxPoints();
	}

	public HashSet<Province> getNeighbors() { return neighbors; }

	public void clearNeighbors() { neighbors.clear(); }

}
