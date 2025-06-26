package org.src.rendering.wrapper;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL30.glBindBufferRange;
import static org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER;
import static org.lwjgl.opengl.GL45.glCreateBuffers;
import static org.lwjgl.opengl.GL45.glNamedBufferData;

public final class UniformBuffer {
	private final int id;
	private final int usage;
	private final int uniformByteSize;
	private final int binding;

	/**
	 * @param uniformByteSize the size of the buffer in bytes
	 * @param usage can either be GL_STATIC_DRAW, GL_DYNAMIC_DRAW or GL_STREAM_DRAW
	 */
	public UniformBuffer(final int uniformByteSize, final int binding, final int usage) {
		this.uniformByteSize = uniformByteSize;
		this.usage = usage;
		this.binding = binding;

		id = glCreateBuffers();

		glNamedBufferData(id, uniformByteSize, usage);
		glBindBufferRange(GL_UNIFORM_BUFFER, binding, id, 0, uniformByteSize);
	}

	public void regenerate(final float[] newData) {
		glNamedBufferData(id, uniformByteSize, usage);
		glBindBufferRange(GL_UNIFORM_BUFFER, binding, id, 0, uniformByteSize);
		glNamedBufferData(id, newData, usage);
	}

	public void bind() {
		glBindBuffer(GL_UNIFORM_BUFFER, id);
	}

	public void unbind() {
		glBindBuffer(GL_UNIFORM_BUFFER, 0);
	}

	public void dispose() {
		glDeleteBuffers(id);
	}

	public int getUniformByteSize() {
		return uniformByteSize;
	}

	public int getId() {
		return id;
	}


}
