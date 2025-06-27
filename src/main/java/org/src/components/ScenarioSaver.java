package org.src.components;

import org.src.components.province.Province;
import org.src.core.helper.Consts;
import org.src.core.helper.Helper;

import java.util.ArrayList;
import java.util.Arrays;

public final class ScenarioSaver {

	private final Map map;
	public ScenarioSaver(final Map map) {
		this.map = map;
	}

	public void saveScenario() {
		final StringBuilder saveFile = new StringBuilder();
		// TODO: remember when loading to not loop over the last province as it will be empty
		for (int i = 0; i < map.getAmountOfProvinces() - 1; i++) {
			saveFile
					.append(";v:")
					.append(Arrays.toString(map.getProvince(i).getVertices()).replace(" ", ""))
					.append(";c:")
					.append(Arrays.toString(map.getProvince(i).getColor()).replace(" ", ""))

					.append('\n');
		}

		Helper.writeToFile("res/saves/save1.txt", saveFile.toString());
	}

	public void loadScenario() {
		final String[] lines = Helper.loadFileAsString("res/saves/save1.txt").split("\n");

		for (final String provinceData: lines) {
			final Province province = map.createProvince();

			// get all the indices of the semicolons
			final ArrayList<Integer> semicolonIndices = new ArrayList<>();

			int currentSemicolonIndex = provinceData.indexOf(';');
			while (currentSemicolonIndex >= 0) {
				semicolonIndices.add(currentSemicolonIndex);
				currentSemicolonIndex = provinceData.indexOf(';', currentSemicolonIndex + 1);
			}

			final int verticesStartIndex = provinceData.indexOf(";v:") + 3; // we want to skip the 3 characters
			int verticesStopIndex = -1;
			for (final int semicolon : semicolonIndices) {
				if (verticesStartIndex < semicolon) {
					verticesStopIndex = semicolon;
				}
			}

			final float[] vertices = Helper.FLOAT_ARR(provinceData.substring(verticesStartIndex, verticesStopIndex));

			final float[] pointPositions = new float[vertices.length / province.getMeshStride() * Consts.POINT_POS_STRIDE];
			     int j = 0;
			for (int i = 0; i < vertices.length; i += province.getMeshStride()) {
				pointPositions[j] = vertices[i];
				pointPositions[j + 1] = vertices[i + 1];
				j += Consts.POINT_POS_STRIDE;
			}

			province.setVertices(vertices);
			province.setPointsPoses(pointPositions);
			province.refreshMesh();

			map.addProvinceToMesh(province);
		}
	}

}
