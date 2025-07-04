package org.src.rendering.wrapper;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.stb.STBImage.*;

public final class Texture {
	private final int slot;
	private int id;

	private String fileName;

	private int width;
	private int height;

	public Texture(final String fileName, final int slot) {
		this.fileName = fileName;
		this.slot = slot;
		regenerate();
	}

	public Texture(final ByteBuffer image, final int width, final int height, final int slot) {
		this.width = width;
		this.height = height;
		this.slot = slot;
		this.fileName = "NO_SPECIFIED";
		regenerate(image);
	}

	public void regenerate(final String newFilename) {
		this.fileName = newFilename;
		regenerate();
	}

	public void regenerate() {
		final int[] imgWidth = new int[1];
		final int[] imgHeight = new int[1];
		final int[] imgChannels = new int[1];
		final ByteBuffer image = stbi_load(fileName, imgWidth, imgHeight, imgChannels, 3);

		width = imgWidth[0];
		height = imgHeight[0];

		regenerate(image);
	}

	public void regenerate(final ByteBuffer image) {
		id = glCreateTextures(GL_TEXTURE_2D);

		glTextureParameteri(id, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
		glTextureParameteri(id, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTextureStorage2D(id, 3, GL_RGB8, width, height);

		glTextureSubImage2D(id, 0, 0, 0, width, height, GL_RGB, GL_UNSIGNED_BYTE, image);
		glGenerateTextureMipmap(id);
		// HUGE memory leak fixed
		stbi_image_free(image);
	}

	public int getSlot() {
		return slot;
	}

	public void bind() {
		glBindTextureUnit(slot, id);
	}

	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public void dispose() {
		glDeleteTextures(id);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
