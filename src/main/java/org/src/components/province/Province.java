package org.src.components.province;

import imgui.type.ImString;
import org.joml.Vector2f;
import org.src.components.civilisation.State;
import org.src.core.helper.Helper;
import org.src.core.helper.Rect2D;

import java.util.HashSet;

import static org.src.core.helper.Consts.*;

public final class Province {
	private State owner;

	private final ProvinceRenderer renderer;

	private final HashSet<Province> neighbors;

	public ImString name;

	public Climate climate;
	public Minerals minerals;
	public Soil soil;

	public float forestCoverage; // 0.0 means no forest; 1.0 means everything is forest

	public float distanceFromSea; // this will have to be decided programmatically
	public boolean islandProvince;

	public float soilQuality; // 0.0 means totally unusable (for instance in the Death Valley); 1.0 means perfect (for instance ukraine)

	public ProvinceType type;

	public int populationCount;
	public int elevation;

	public Province() {
		renderer = new ProvinceRenderer();
		neighbors = new HashSet<>();

		name = new ImString(50);

		this.owner = null;
		this.populationCount = 0;

		this.type = ProvinceType.DEEP_SEA;

		setColorToType();
	}

	public void setType(final int intType) {
		if (ProvinceType.values()[intType] == this.type) { return; }

		switch (intType) {
			case 0 -> type = ProvinceType.DEEP_SEA;
			case 1 -> type = ProvinceType.SHALLOW_SEA;
			case 2 -> type = ProvinceType.COSTAL_SEA;
			case 3 -> type = ProvinceType.BOG;
			case 4 -> type = ProvinceType.LOWLANDS;
			case 5 -> type = ProvinceType.HIGHLANDS;
			case 6 -> type = ProvinceType.MOUNTAINS;
		}

		setColorToType();
	}

	public void setType(final ProvinceType type) {
		if (this.type == type) { return; }

		this.type = type;
		setColorToType();
	}

	public static boolean isSeaType(final ProvinceType type) {
		return type == ProvinceType.DEEP_SEA || type == ProvinceType.SHALLOW_SEA || type == ProvinceType.COSTAL_SEA;
	}

	public static boolean isSeaType(final Province province) {
		return province.type == ProvinceType.DEEP_SEA || province.type == ProvinceType.SHALLOW_SEA || province.type == ProvinceType.COSTAL_SEA;
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

	public boolean intersectsMaxRectangle(final Rect2D hwat) {
		return renderer.intersectsMaxRectangle(hwat);
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
	 * @param hwat the rectangle to check collisions for
	 * @return the index in the pointPoses array of the x value of the point (-1 if we don't hit anything)
	 */
	public int getFirstIntersectedPointIndex(final Rect2D hwat) {
		return renderer.getFirstIntersectedPointIndex(hwat);
	}

	public void dispose() {
		renderer.dispose();
	}

	public void setColor(final float[] color) {
		renderer.setColor(color);
	}

	public void setColorValue(final float[] color) {
		renderer.setColorValue(color);
	}

	public void setColorToType() {
		float[] color = new float[3];

		switch (type) {
			case DEEP_SEA    -> color = Helper.deepCopy(DEEP_SEA_COLORS);
			case SHALLOW_SEA -> color = Helper.deepCopy(SHALLOW_SEA_COLORS);
			case COSTAL_SEA  -> color = Helper.deepCopy(COSTAL_SEA_COLORS);
			case BOG         -> color = Helper.deepCopy(BOG_COLORS);
			case LOWLANDS    -> color = Helper.deepCopy(LOWLANDS_COLORS);
			case HIGHLANDS   -> color = Helper.deepCopy(HIGHLANDS_COLORS);
			case MOUNTAINS   -> color = Helper.deepCopy(MOUNTAINS_COLORS);
		}

		//FIXME: Figure out why this is doing a party when painting other provinces using PaintProvincesMode.java
		//color[0] += (float) Helper.rand(-0.005, 0.005); // red isn't very prevalent
		//color[1] += (float) Helper.rand(-0.01, 0.01);
		//color[2] += (float) Helper.rand(-0.01, 0.01);

		setColor(color);
	}

	public void shallowSetColor(final float[] color) {
		renderer.shallowSetColor(color);
	}

	public String getTypeString() {
		return switch (type) {
			case MOUNTAINS -> "Mountains";
			case DEEP_SEA -> "Deep sea";
			case SHALLOW_SEA -> "Shallow sea";
			case COSTAL_SEA -> "Costal sea";
			case BOG -> "Bog";
			case LOWLANDS -> "Lowlands";
			case HIGHLANDS -> "Highlands";
		};
	}

	public void setType(final String type) {
		switch (type) {
			case "Mountains"   -> this.type = ProvinceType.MOUNTAINS;
			case "Deep sea"    -> this.type = ProvinceType.DEEP_SEA;
			case "Shallow sea" -> this.type = ProvinceType.SHALLOW_SEA;
			case "Costal sea"  -> this.type = ProvinceType.COSTAL_SEA;
			case "Bog"         -> this.type = ProvinceType.BOG;
			case "Lowlands"    -> this.type = ProvinceType.LOWLANDS;
			case "Highlands"   -> this.type = ProvinceType.HIGHLANDS;
		}
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

	public int getVertexStride() {
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

	public HashSet<Province> getNeighbors() {
		return neighbors;
	}

	public void clearNeighbors() {
		neighbors.clear();
	}

	public State getOwner() {
		return this.owner;
	}

	// this is expected to be only called within State.java so they do not take care of changing the color
	public void setOwner(final State state) {
		this.owner = state;
	}

}
