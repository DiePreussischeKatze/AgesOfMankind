package org.src.rendering.wrapper;

import java.util.Arrays;

// TODO: Make it standard
public final class Color {
	public final float[] value;

	public Color() {
		value = new float[3];
		Arrays.fill(value, 0.0F);
	}

	public void set(final float red, final float green, final float blue) {
		value[0] = red;
		value[1] = green;
		value[2] = blue;
	}

	public void set(final float[] color) {
		if (color.length != 3) {
			System.err.println("Something is wrong with the color - Color.java");
			return;
		}

		value[0] = color[0];
		value[1] = color[1];
		value[2] = color[2];
	}

}
