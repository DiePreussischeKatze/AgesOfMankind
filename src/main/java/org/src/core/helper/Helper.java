package org.src.core.helper;

import imgui.ImGui;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * A class containing a bunch of helper methods like quick string to int conversion
 * */
public final class Helper {
	public static String loadFileAsString(final String fileName) {
		final StringBuilder file = new StringBuilder();
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(fileName));

			String line = reader.readLine();
			while (line != null) {
				file.append(line).append('\n');
				line = reader.readLine();
			}
		} catch (IOException e) {
			System.err.println(
					"File: " + fileName + " Not found!"
			);
			System.exit(1);
		}

		return file.toString();
	}

	public static void writeToFile(final String path, final String newContent) {
		try {
			final BufferedWriter writer = new BufferedWriter(new FileWriter(path));

			writer.write(newContent);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static int INT(final String string) {
		return Integer.parseInt(string);
	}

	public static int INT(final boolean bool) {
		return bool ? 1 : 0;
	}

	public static float FLOAT(final String string) {
		return Float.parseFloat(string);
	}

	public static boolean BOOL(final String string) {
		return !(string.equalsIgnoreCase("false") || string.equals("0"));
	}

	public static String STR(final int value) {
		return Integer.toString(value);
	}

	/**
	 * For whatever reason when I use ByteBuffer.array() it crashes
	 * @return The string made from individual characters of the byte buffer
	 */
	public static String byteBufferToString(final ByteBuffer buffer) {
		final StringBuilder string = new StringBuilder();

		for (int i = 0; i < buffer.capacity(); i++) {
			if (buffer.get(i) == 0) { break; }
			string.append((char)buffer.get(i));
		}

		return string.toString();
	}

	public static boolean insideRange(final int value, final int max, final int min) {
		return value >= min && value <= max;
	}

	public static boolean insideRange(final float value, final float max, final float min) {
		return value >= min && value <= max;
	}

	public static boolean isInImGuiWindow() {
		return ImGui.getIO().getWantCaptureMouse();
	}

	// This method should be used in moderation
	public static float[] insertElementsToFloatArray(final float[] oldArray, final float[] newElements) {
		final float[] newArray = new float[oldArray.length + newElements.length];
		System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
		System.arraycopy(newElements, 0, newArray, oldArray.length, newElements.length);
		return newArray;
	}

	public static int[] insertElementsToIntArray(final int[] oldArray, final int[] newElements) {
		final int[] newArray = new int[oldArray.length + newElements.length];
		System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
		System.arraycopy(newElements, 0, newArray, oldArray.length, newElements.length);
		return newArray;
	}

	/**
	 * This method will take the old array and truncate it to a given size, for instance for an array of [0.5, 0.6, 0.3]
	 * and new size of 2 the return value will be [0.5, 0.6]
	 * @return the new truncated array
	 */
	public static float[] truncateFloatArray(final float[] oldArray, final int newSize) {
		if (newSize <= 0) { return new float[0]; }

		final float[] newArray = new float[newSize];
		System.arraycopy(oldArray, 0, newArray, 0, newSize);
		return newArray;
	}

	public static double[] toDoubleArray(final float[] floatArray) {
		double[] doubleArray = new double[floatArray.length];

		for (int i = 0; i < floatArray.length; i++) {
			doubleArray[i] = floatArray[i];
		}

		return doubleArray;
	}

	public static int maxFromIntArray(final int[] intArray) {
		int max = Integer.MIN_VALUE;
		for (final int i: intArray) {
			if (max < i) {
				max = i;
			}
		}
		return max;
	}

}
