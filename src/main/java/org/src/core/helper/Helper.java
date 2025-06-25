package org.src.core.helper;

import imgui.ImGui;
import org.joml.Vector2f;
import org.src.rendering.wrapper.Mesh;

import java.io.*;
import java.nio.ByteBuffer;

import static org.src.core.helper.Consts.RECTANGLE_INDICES;

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
	public static float[] addElementsToFloatArray(final float[] oldArray, final float[] newElements) {
		final float[] newArray = new float[oldArray.length + newElements.length];
		System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);
		System.arraycopy(newElements, 0, newArray, oldArray.length, newElements.length);
		return newArray;
	}

	public static int[] addElementsToIntArray(final int[] oldArray, final int[] newElements) {
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

	/**
	 * @return the array without the specified elements (it deletes the elements including the start index)
	 */
	public static float[] deleteElementsFromFloatArray(final float[] array, final int startIndex, final int deleteCount) {
		if (array.length == 0) { return array; }
		final float[] newArray = new float[array.length - deleteCount];
		System.arraycopy(array, 0, newArray, 0, array.length - deleteCount);
		System.arraycopy(array, startIndex + deleteCount, newArray, startIndex, array.length - startIndex - deleteCount);
		return newArray;
	}

	public static int[] deleteElementsFromIntArray(final int[] array, final int startIndex, final int deleteCount) {
		if (array.length == 0) { return array; }
		final int[] newArray = new int[array.length - deleteCount];
		System.arraycopy(array, 0, newArray, 0, array.length - deleteCount);
		System.arraycopy(array, startIndex + deleteCount, newArray, startIndex, array.length - startIndex - deleteCount);
		return newArray;
	}

	public static float[] insertElementsToFloatArray(final float[] array, final int startIndex, final float[] newElements) {
		final float[] newArray = new float[array.length + newElements.length];
		System.arraycopy(array, 0, newArray, 0, startIndex);
		System.arraycopy(newElements, 0, newArray, startIndex, newElements.length);
		System.arraycopy(array, startIndex, newArray, newElements.length + startIndex, newArray.length - startIndex - newElements.length);
		return newArray;
	}

	/**
	 * creates a simple mesh with just x;y coordinates
	 */
	public static Mesh createPlainBoxMesh(final float xScale, final float yScale) {
		return new Mesh(new float[] {
				 xScale,  yScale,
				 xScale, -yScale,
				-xScale, -yScale,
				-xScale,  yScale,
		}, RECTANGLE_INDICES, new byte[] { Consts.POINT_POS_STRIDE});
	}

	public static boolean pointTriangleIntersection(final Vector2f point, final Vector2f v0, final Vector2f v1, final Vector2f v2) {
		return inRange(
				triangleArea(v0, point, v1)
						+ triangleArea(v0, point, v2)
						+ triangleArea(v1, point, v2),
				triangleArea(v0, v1, v2),
				0.00001f // fuck floating point comparison
		);
	}

	public static float triangleArea(final Vector2f p0, final Vector2f p1, final Vector2f p2) {
		// formula taken from: https://www.cuemath.com/geometry/area-of-triangle-in-coordinate-geometry/
		return Math.abs(p0.x * (p1.y - p2.y) + p1.x * (p2.y - p0.y) + p2.x * (p0.y - p1.y)) / 2;
	}

	public static boolean inRange(final float value, final float value2, final float offset) {
		return value + offset > value2 && value - offset < value2;
	}

	public static float max(final float... floats) {
		float max = Float.MIN_VALUE;
		for (final float f: floats) {
			if (f > max) {
				max = f;
			}
		}
		return max;
	}

	public static float min(final float... floats) {
		float min = Float.MAX_VALUE;
		for (final float f: floats) {
			if (f < min) {
				min = f;
			}
		}
		return min;
	}

}
