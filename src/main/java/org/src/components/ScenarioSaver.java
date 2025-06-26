package org.src.components;

import org.src.components.province.Province;

import java.util.Arrays;

public final class ScenarioSaver {

	private final Map map;
	public ScenarioSaver(final Map map) {
		this.map = map;
	}

	public void saveScenario() {
		final StringBuilder saveFile = new StringBuilder();
		// TODO: remember when loading to not loop over the last province as it will be empty
		for (final Province province: map.getProvinces()) {
			saveFile
					.append("{")


					.append("i:")
					.append(Arrays.toString(province.getIndices()).replace(" ", ""))
					.append(";v:")
					.append(Arrays.toString(province.getVertices()).replace(" ", ""))
					.append(";c:")
					.append(Arrays.toString(province.getColor()).replace(" ", ""))
					.append("}")
					.append('\n');
		}

		System.out.println(saveFile);

	}

	public void loadScenario() {

	}

}
