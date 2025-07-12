package org.src.components;

import org.src.components.map.Map;
import org.src.components.province.Province;
import org.src.core.helper.Consts;
import org.src.core.helper.Helper;

import java.util.ArrayList;
import java.util.Arrays;

public final class ScenarioSaver {

	private final ArrayList<Integer> semicolonIndices;
	private String provinceData;

	private final Map map;
	public ScenarioSaver(final Map map) {
		this.map = map;

		this.semicolonIndices = new ArrayList<>();
		this.provinceData = "";
	}

	public void saveScenario() {
		final StringBuilder saveFile = new StringBuilder();
		// TODO: remember when loading to not loop over the last province as it will be empty
		for (int i = 0; i < map.getAmountOfProvinces(); i++) {
			saveFile
					.append(";v:")
					.append(Arrays.toString(map.getProvince(i).getVertices()).replace(" ", ""))
					.append(";n:")
					.append(map.getProvince(i).name)
					.append(";p:")
					.append(map.getProvince(i).populationCount)
					.append('\n');
		}

		Helper.writeToFile("res/saves/save1.txt", saveFile.toString());
	}

	public void loadScenario() {
		final String[] lines = Helper.loadFileAsString("res/saves/save1.txt").split("\n");

		for (int i = 0; i < lines.length - 1; i++) {
			provinceData = lines[i];

			final Province province = map.createProvince();

			// get all the indices of the semicolons
			int currentSemicolonIndex = provinceData.indexOf(';');
			while (currentSemicolonIndex >= 0) {
				semicolonIndices.add(currentSemicolonIndex);
				currentSemicolonIndex = provinceData.indexOf(';', currentSemicolonIndex + 1);
			}

			final float[] vertices = Helper.FLOAT_ARR(getProperty(";v:"));

			final float[] pointPositions = new float[vertices.length / province.getVertexStride() * Consts.POINT_POS_STRIDE];
			     int j = 0;
			for (int k = 0; k < vertices.length; k += province.getVertexStride()) {
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

			final String name = getProperty(";n:");

			province.setVertices(vertices);
			province.setPointsPoses(pointPositions);
			province.setColor(color);
			province.refreshMesh();
			province.refreshMaxPoints();
			province.name.set(name);

			provinceData = "";
			semicolonIndices.clear();

			if (i == 0) { continue; }
			map.addProvinceToMesh(province);
			// NO OTHER CODE HERE!!!
		}
	}

	private String getProperty(final String label) {
		final int startIndex = provinceData.indexOf(label) + label.length(); // we want to skip the 3 characters
		for (final int semicolon : semicolonIndices) {
			if (startIndex <= semicolon) {
				return provinceData.substring(startIndex, semicolon);
			}
		}
		System.err.println("No property of the name: " + label + " was found!");
		return ""; // if no property of such name is found
	}

}
