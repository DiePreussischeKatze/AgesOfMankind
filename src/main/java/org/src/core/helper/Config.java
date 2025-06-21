package org.src.core.helper;

import java.util.Objects;

/**
 * Config reader
 */
public final class Config {

	private record Param(String name, String value) {

	}

	/**
	 * Gets the string value of a parameter from the config file
	 * If the method is used frequently, assign its return value to a variable
	 * @param name the name of the parameter to get from the config file
	 * @return the parameter's string value
	*/
	public static String get(final String name) {
		// get the file as a string divide it into an array of lines
		final String[] lines = Helper.loadFileAsString("res/config/config.txt").trim().split("\n");

		// Loop through all the lines
		for (final String line: lines) {
			final Param param = splitLineIntoNameAndValue(line);

			if (param == null) { continue; }

			if (Objects.equals(param.name(), name)) {
				return param.value();
			}

		}

		return null; // dummy return for make the compiler not yell at me
	}

	// This might be slow
	// If someone knows of a better way to do that, please make an issue on GitHub
	public static void set(final String name, final String value) {
		final String[] lines = Helper.loadFileAsString("res/config/config.txt").trim().split("\n");
		for (int i = 0; i < lines.length; i++) {
			final Param param = splitLineIntoNameAndValue(lines[i]);

			if (param == null) { continue; }

			if (Objects.equals(param.name, name)) {
				lines[i] = param.name + ": " + value;
			}
		}

		final StringBuilder file = new StringBuilder();

		for (final String line: lines) {
			file.append(line).append('\n');
		}

		Helper.writeToFile("res/config/config.txt", file.toString());
	}

	public static void setNew(final String newName, final String value) {
		final StringBuilder file = new StringBuilder(Helper.loadFileAsString("res/config/config.txt"));
		file.append(newName).append(": ").append(value).append('\n');
		Helper.writeToFile("res/config/config.txt", file.toString());
	}

	private static Param splitLineIntoNameAndValue(final String line) {
		final StringBuilder name = new StringBuilder();
		final StringBuilder value = new StringBuilder();
		int i = 0;
		for (; i < line.length(); i++) {
			if (line.charAt(i) == '?') { return null; } // comment
			if (line.charAt(i) == ' ') { continue; } // space
			if (line.charAt(i) == ':') { break; } // separator

			name.append(line.charAt(i));
		}

		for (int j = i + 1; j < line.length(); j++) {
			if (line.charAt(j) == ' ') { continue; }
			if (line.charAt(j) == '?' || line.charAt(j) == '\n') { return new Param(name.toString(), value.toString()); }

			value.append(line.charAt(j));
		}

		return new Param(name.toString(), value.toString());
	}


}
