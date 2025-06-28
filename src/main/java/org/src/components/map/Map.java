package org.src.components.map;

import org.joml.Vector2f;
import org.src.components.province.Province;
import org.src.core.helper.Component;

import java.util.ArrayList;

public final class Map extends Component {

	private final MapRenderer renderer;

	private final ArrayList<Province> provinces;

	private int lendProvince;

	public Map() {
		this.renderer = new MapRenderer(this);

		provinces = new ArrayList<>();

		lendProvince = 0;
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
	public void update(double deltaTime) {
		provinces.forEach(Province::update);
	}

	@Override
	public void dispose() {
		renderer.dispose();
	}

	public boolean getDrawProvinceFillings() {
		return renderer.getDrawProvinceFillings();
	}

	public void setDrawProvinceFillings(boolean drawProvinceFillings) {
		renderer.setDrawProvinceFillings(drawProvinceFillings);
	}

	public boolean getDrawProvincePoints() {
		return renderer.getDrawProvincePoints();
	}

	public void setDrawProvincePoints(boolean drawProvincePoints) {
		renderer.setDrawProvincePoints(drawProvincePoints);
	}

	public void toggleDrawProvincePoints() {
		renderer.setDrawProvincePoints(!renderer.getDrawProvincePoints());
	}

	public void toggleDrawProvinceFillings() {
		renderer.setDrawProvinceFillings(!renderer.getDrawProvinceFillings());
	}

	public Province getProvince(final int index) {
		return provinces.get(index);
	}

	public int getAmountOfProvinces() {
		return provinces.size();
	}

	public int getLendProvinceId() {
		return lendProvince;
	}

	public void setLendProvince(int lendProvince) {
		this.lendProvince = lendProvince;
	}

	public ArrayList<Province> getProvinces() {
		return provinces;
	}

}
