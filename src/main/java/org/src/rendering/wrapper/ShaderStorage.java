package org.src.rendering.wrapper;

import org.lwjgl.BufferUtils;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferBase;
import static org.lwjgl.opengl.GL43.GL_SHADER_STORAGE_BUFFER;
import static org.lwjgl.opengl.GL45.*;

public final class ShaderStorage {
	private int id;

	private int binding;

	public ShaderStorage(final float[] data, final int binding) {
		this.binding = binding;

		id = glCreateBuffers();

		glNamedBufferData(id, BufferUtils.createFloatBuffer(data.length).put(data).flip(), GL_DYNAMIC_COPY);
		glBindBufferBase(GL_SHADER_STORAGE_BUFFER, binding, id);
	}

	public void regenerate(final float[] newData) {
		glNamedBufferData(id, BufferUtils.createFloatBuffer(newData.length).put(newData).flip(), GL_DYNAMIC_COPY);
	}

	public void unbind() {
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, 0);
	}

	public void bind() {
		glBindBuffer(GL_SHADER_STORAGE_BUFFER, id);
	}

	public int getBinding() {
		return binding;
	}

	public void dispose() {
		glDeleteBuffers(id);
	}

}
