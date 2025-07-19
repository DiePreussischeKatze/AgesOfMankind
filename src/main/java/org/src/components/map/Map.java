package org.src.components.map;

import org.joml.Vector2f;
import org.src.components.province.Province;
import org.src.core.helper.Component;

import java.util.ArrayList;

public final class Map extends Component {

	private final MapRenderer renderer;

	private final ArrayList<Province> provinces;

	private int lendProvince;

	private int maxPopulation;
	private int maxElevation;

	public Map() {
		this.renderer = new MapRenderer(this);

		this.provinces = new ArrayList<>();

		this.lendProvince = 0;
		this.maxPopulation = -1;
	}

	public Province createProvince() {
		// check if there aren't any empty provinces
		for (int i = 0; i < provinces.size(); i++) {
			if (provinces.get(i).getIndices().length == 0) {
				lendProvince = i;
				return provinces.get(i);
			}
		}

		final Province newProvince = new Province();
		provinces.add(newProvince);
		lendProvince = provinces.size() - 1;
		return newProvince;
	}

	public void addProvinceToMesh(final Province province) {
		renderer.addProvinceToMesh(province);
	}

	public void takeProvinceFromMesh(final Province province) {
		renderer.takeProvinceFromMesh(province);
	}

	public Province findProvinceUnderPoint(final Vector2f point) {
		for (final Province province: provinces) {
			if (province.isInProvince(point)) { return province; }
		}
		return null;
	}

	public int findProvinceIndexUnderPoint(final Vector2f point) {
		for (int i = 0; i < provinces.size(); i++) {
			if (provinces.get(i).isInProvince(point)) { return i; }
		}
		return -1;
	}

	@Override
	public void draw() {
		renderer.draw();
	}

	@Override
	public void dispose() {
		renderer.dispose();
	}

	public void setProvinceColor(final Province hwich, final float[] color) {
		renderer.setProvinceColor(hwich, color);
	}

	public void setDisplayMode(final DisplayMode mode) {
		renderer.setDisplayMode(mode);
	}

	public void setDrawProvinceFillings(boolean drawProvinceFillings) {
		renderer.setDrawProvinceFillings(drawProvinceFillings);
	}

	public void setLendProvinceID(int lendProvince) {
		this.lendProvince = lendProvince;
	}

	public boolean getDrawProvinceFillings() {
		return renderer.getDrawProvinceFillings();
	}

	public boolean getDrawProvincePoints() {
		return renderer.getDrawProvincePoints();
	}

	public Province getProvince(final int index) {
		return provinces.get(index);
	}

	public int getAmountOfProvinces() {
		return provinces.size();
	}

	public int getLendProvinceId() {
		return this.lendProvince;
	}

	public ArrayList<Province> getProvinces() {
		return this.provinces;
	}

	public void toggleDrawProvincePoints() {
		renderer.setDrawProvincePoints(!renderer.getDrawProvincePoints());
	}

	public void toggleDrawProvinceFillings() {
		renderer.setDrawProvinceFillings(!renderer.getDrawProvinceFillings());
	}

	public void findMaxParams() {
		maxPopulation = -1;
		maxElevation = -1;

		for (final Province province: provinces) {
			maxPopulation = Math.max(maxPopulation, province.populationCount);
			maxElevation = Math.max(maxElevation, province.elevation);
		}
	}

	public int getMaxPopulation() {
		return maxPopulation;
	}

	public int getMaxElevation() {
		return maxElevation;
	}

	public DisplayMode getDisplayMode() {
		return renderer.getDisplayMode();
	}

	@Override
	public void update(double deltaTime){}

}
