package org.src.rendering.wrapper;

import org.lwjgl.BufferUtils;
import org.src.core.helper.Helper;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL45.*;

public final class Mesh {
	private int ebo;
	private int vao;
	private int vbo;

	private float[] vertices;
	private byte[] offsets;

	private int[] indices;

	public Mesh(final float[] vertices, final int[] indices, final byte[] offsets) {
		this.vertices = vertices;
		this.offsets = offsets;
		this.indices = indices;

		vao = glCreateVertexArrays();
		vbo = glCreateBuffers();
		ebo = glCreateBuffers();

		regenerate();
	}

	public void regenerate() {
		glNamedBufferData(vbo, BufferUtils.createFloatBuffer(vertices.length).put(vertices).flip(), GL_STATIC_DRAW);
		glNamedBufferData(ebo, BufferUtils.createIntBuffer(indices.length).put(indices).flip(), GL_STATIC_DRAW);

		int offsetSum = 0;
		for (final int offset: offsets) {
			offsetSum += offset;
		}

		glVertexArrayVertexBuffer(vao, 0, vbo, 0, offsetSum * Float.BYTES);
		glVertexArrayElementBuffer(vao, ebo);

		int currentPointer = 0;
		for (int i = 0; i < offsets.length; i++) {
			glVertexArrayAttribFormat(vao, i, offsets[i], GL_FLOAT, false, currentPointer * Float.BYTES);
			glEnableVertexArrayAttrib(vao, i);
			glVertexArrayAttribBinding(vao, i, 0);

			currentPointer += offsets[i];
		}

	}

	public void draw() {
		bind();
		glDrawElements(GL_TRIANGLES, indices.length, GL_UNSIGNED_INT, 0);
	}

	public void addVertices(final float[] newVertices) {
		vertices = Helper.insertElementsToFloatArray(vertices, newVertices);
	}

	public void addIndices(final int[] newIndices) {
		indices = Helper.insertElementsToIntArray(indices, newIndices);
	}

	public void bind() {
		glBindVertexArray(vao);
	}

	public void unbind() {
		glBindVertexArray(0);
	}

	public void setIndices(final int[] indices) {
		this.indices = indices;
	}

	public void clear() {
		indices = new int[0];
		vertices = new float[0];
	}

	public void setVertices(final float[] vertices) {
		this.vertices = vertices;
	}

	public void setOffsets(final byte[] offsets) {
		this.offsets = offsets;
	}

	public float[] getVertices() {
		return vertices;
	}

	public int[] getIndices() {
		return indices;
	}

	public void dispose() {
		glDeleteVertexArrays(vao);
		glDeleteBuffers(vbo);
		glDeleteBuffers(ebo);
	}

}
