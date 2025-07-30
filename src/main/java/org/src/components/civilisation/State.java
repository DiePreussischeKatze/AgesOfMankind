package org.src.components.civilisation;

import imgui.type.ImString;
import org.src.components.map.Map;
import org.src.components.province.Province;
import org.src.core.helper.Helper;

import java.util.ArrayList;

public final class State {
	private final float[] color;

	private ImString name;

	private final ArrayList<Province> ownedProvinces;

	private final Map map;
	public State(final Map map, final String name) {
		this.map = map;

		this.name = new ImString(name, 40);

		this.ownedProvinces = new ArrayList<>();

		color = new float[3];
		color[0] = Helper.rand(0.0, 1.0);
		color[1] = Helper.rand(0.0, 1.0);
		color[2] = Helper.rand(0.0, 1.0);
	}

	public void addProvince(final Province province) {
		if (this.ownedProvinces.contains(province) || Province.isSeaType(province)) {
			return;
		}

		if (province.getOwner() != null) {
			province.getOwner().removeProvince(province);
		}

		this.ownedProvinces.add(province);
		province.setOwner(this);
		province.setColor(color);
	}

	// does not remove the memory associated with the province only the pointer in the array
	public void removeProvince(final Province province) {
		this.ownedProvinces.remove(province);
		province.setOwner(null);
	}

	public void changeColor(final float[] color) {
		if (this.color.length != color.length) {
			System.err.println("Invalid color in State.java");
		}

		this.color[0] = color[0];
		this.color[1] = color[1];
		this.color[2] = color[2];

		updateColor();
	}

	public void updateColor() {
		for (final Province province: ownedProvinces) {
			province.setColor(color);
		}
	}

	public ImString getName() {
		return name;
	}

	public void setName(final ImString name) {
		this.name = name;
	}

	public float[] getColor() {
		return color;
	}

	public ArrayList<Province> getOwnedProvinces() {
		return ownedProvinces;
	}

}
