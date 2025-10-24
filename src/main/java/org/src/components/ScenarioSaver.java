package org.src.components;

import org.src.components.civilisation.State;
import org.src.components.map.DisplayMode;
import org.src.components.map.Map;
import org.src.components.province.Province;
import org.src.core.helper.Consts;
import org.src.core.helper.Helper;

import java.util.ArrayList;
import java.util.Arrays;

import static org.src.core.helper.Helper.INT;

public final class ScenarioSaver {

	private static final String NO_OWNER = "$NOWNR";

	private final ArrayList<Integer> semicolonIndices;
	private String provinceData;

	private Province usedProvince;

	private final Map map;
	public ScenarioSaver(final Map map) {
		this.map = map;

		this.semicolonIndices = new ArrayList<>();
		this.provinceData = "";
	}

	public void saveScenario() {
		DisplayMode previous = map.getDisplayMode();
		map.setDisplayMode(DisplayMode.POLITICAL); // needed for some stuff

		final StringBuilder saveFile = new StringBuilder();
		// TODO: remember when loading to not loop over the last province as it will be empty
		for (int i = 0; i < map.getAmountOfProvinces(); i++) {
			final Province province = map.getProvince(i);
			
			saveFile
					.append(";v:")
					.append(Arrays.toString(province.getVertices()).replace(" ", ""))
					.append(";n:")
					.append(province.name)
					.append(";p:")
					.append(province.populationCount)
					.append(";t:")
					.append(province.getTypeString())
					.append(";e:")
					.append(province.elevation)
					.append(";o:")
					.append(province.getOwner() != null ? province.getOwner().getName() : NO_OWNER)
					.append(";\n");
		}

		Helper.writeToFile("res/saves/save1.txt", saveFile.toString());

		map.setDisplayMode(previous);
	}

	public void loadScenario(final boolean loadingAsGameplayMode) {
		// TODO: Organize into a bunch of smaller methods
		final String[] lines = Helper.loadFileAsString("res/saves/save1.txt").split("\n");

		for (int i = 0; i < lines.length - 1; i++) {
			provinceData = lines[i];

			usedProvince = map.createProvince();

			// get all the indices of the semicolons
			int currentSemicolonIndex = provinceData.indexOf(';');
			while (currentSemicolonIndex >= 0) {
				semicolonIndices.add(currentSemicolonIndex);
				currentSemicolonIndex = provinceData.indexOf(';', currentSemicolonIndex + 1);
			}

			loadProvinceMeshData();
			loadProvinceName();
			loadProvinceElevation();
			loadProvinceType();
			loadProvincePopCount();
			loadOwnership();

			usedProvince.refreshMesh();
			usedProvince.refreshMaxPoints();

			provinceData = "";
			semicolonIndices.clear();

			map.findMaxParams();
			usedProvince.calculateParams();

			if (i == 0 && !loadingAsGameplayMode) { continue; }
			map.addProvinceToMesh(usedProvince);
			usedProvince = null;
			// NO OTHER CODE HERE!!!
		}

		for (Province province: map.getProvinces()) {
			map.lookForNeighbors(province);
		}
	}

	private void loadOwnership() {
		final String ownerName = getProperty(";o:");

		if (ownerName.isEmpty() || ownerName.equals(NO_OWNER)) {
			return; // old save or no owner
		}

		State foundOwner = findOwner(ownerName);

		// So an owner was found
		if (foundOwner != null) {
		} else {
			map.addState(ownerName);
			foundOwner = map.getStates().getLast();
		}

		foundOwner.changeColor(usedProvince.getColor()); // need to watch out for the order of initialization
		foundOwner.addProvince(usedProvince);

		// Get the correct color (I'll need to update this when I stop saving the color each time for every vertex


	}

	private State findOwner(final String name) {
		for (final State owner: map.getStates()) {
			if (owner.getName().get().equals(name)) {
				return owner;
			}
		}

		return null;
	}

	private void loadProvinceMeshData() {
		final float[] vertices = Helper.FLOAT_ARR(getProperty(";v:"));

		final float[] pointPositions = new float[vertices.length / usedProvince.getVertexStride() * Consts.POINT_POS_STRIDE];
		int j = 0;
		for (int k = 0; k < vertices.length; k += usedProvince.getVertexStride()) {
			pointPositions[j] = vertices[k];
			pointPositions[j + 1] = vertices[k + 1];
			j += Consts.POINT_POS_STRIDE;
		}

		// TODO: Keep an eye on this code
		final float[] color = {
			vertices[Consts.POINT_POS_STRIDE],
			vertices[Consts.POINT_POS_STRIDE + 1],
			vertices[Consts.POINT_POS_STRIDE + 2],
		};

		usedProvince.setVertices(vertices);
		usedProvince.setColor(color);
		usedProvince.setPointsPoses(pointPositions);
	}

	private void loadProvinceName() {
		usedProvince.name.set(getProperty(";n:"), true);
	}

	private void loadProvinceType() {
		usedProvince.setType(getProperty(";t:"));
	}

	private void loadProvincePopCount() {
		final String pop = getProperty(";p:");
		if (!pop.isEmpty()) {
			usedProvince.populationCount = INT(pop);
		}
	}

	private void loadProvinceElevation() {
		final String elevation = getProperty(";e:");
		if (!elevation.isEmpty()) {
			usedProvince.elevation = INT(elevation);
		}
	}

	private String getProperty(final String label) {
		int startIndex = provinceData.indexOf(label);
		if (startIndex == -1) { // no such label is found
			System.err.println("No property named: " + label + " was found!");
			return "";
		}

		startIndex += label.length();
		for (final int semicolon : semicolonIndices) {
			if (startIndex <= semicolon) {
				return provinceData.substring(startIndex, semicolon);
			}
		}

		System.err.println("No property named: " + label + " was found!");
		return "";
	}

}
