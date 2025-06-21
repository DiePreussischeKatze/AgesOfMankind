package org.src.core.managers;

import org.src.rendering.wrapper.Texture;

import java.nio.ByteBuffer;

public final class TextureManager {
	private static int currentSlot;

	static {
		currentSlot = 0;
	}

	public static Texture createTexture(final String path) {
		incrementSlot();
		return new Texture(path, currentSlot);
	}

	public static Texture createTexture(final ByteBuffer image, final int width, final int height) {
		incrementSlot();
		return new Texture(image, width, height, currentSlot);
	}

	private static void incrementSlot() {
		currentSlot++;
		if (currentSlot > 31) {
			currentSlot = 0;
		}
	}

	public static int getCurrentSlot() {
		return currentSlot;
	}

}
