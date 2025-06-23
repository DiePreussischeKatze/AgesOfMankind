package org.src.rendering.wrapper;

import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.src.core.helper.Helper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.*;

public final class Shader {
	private final int id;

	private final ArrayList<String> uniforms;
	private final HashMap<String, Integer> cache; // for optimization

	public Shader(final String vertexPath, final String fragmentPath) {
		uniforms = new ArrayList<>();
		cache = new HashMap<>();

		final int vertexSource = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexSource, Helper.loadFileAsString(vertexPath));
		glCompileShader(vertexSource);
		debugShader(vertexSource);

		final int fragmentSource = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentSource, Helper.loadFileAsString(fragmentPath));
		glCompileShader(fragmentSource);
		debugShader(fragmentSource);

		this.id = glCreateProgram();
		glAttachShader(this.id, vertexSource);
		glAttachShader(this.id, fragmentSource);
		glLinkProgram(this.id);

		glDeleteShader(vertexSource);
		glDeleteShader(fragmentSource);

		listUniforms();
		fillCache();
	}

	// To be removed in final production code
	private void listUniforms() {
		int[] count = new int[1];
		int[] bufferSize = new int[1];
		int[] size = new int[1];
		int[] type = new int[1];
		bufferSize[0] = 32;
		ByteBuffer name = BufferUtils.createByteBuffer(bufferSize[0]);

		glGetProgramiv(id, GL_ACTIVE_UNIFORMS, count);
		for (int i = 0; i < count[0]; i++) {
			glGetActiveUniform(id, i, bufferSize, size, type, name);

			uniforms.add(Helper.byteBufferToString(name));
		}
	}

	private void fillCache() {
		for (final String name: uniforms) {
			cache.put(name, getUnsafe(name));
		}
	}

	private static void debugShader(int shader) {
		int[] success = new int[1];
		glGetShaderiv(shader, GL_COMPILE_STATUS, success);
		if (success[0] != GL_TRUE) {
			System.err.println(glGetShaderInfoLog(shader));
		}
	}

	/**
	 * Note that this shouldn't be used outside the shader if a method like setInt() exists
	 * @param parameterName the name of the uniform in the shader code
	 * @return the index of the parameter
	 */
	// TODO: remove when I create more glUniform fillers
	public int get(final String parameterName) {
		if (!uniforms.contains(parameterName)) { throw new RuntimeException("Parameter: " + parameterName + " Not found"); }
		return getUnsafe(parameterName);
	}

	public int getUnsafe(final String parameterName) {
		return glGetUniformLocation(this.id, parameterName);
	}

	public void bind() {
		glUseProgram(this.id);
	}

	public void unbind() {
		glUseProgram(0);
	}

	public void dispose() {
		glDeleteProgram(this.id);
	}

	public int getId() {
		return id;
	}

	public void setInt(final String parameterName, final int value) {
		glUniform1i(cache.get(parameterName), value);
	}

	public void setFloat(final String parameterName, final float value) {
		glUniform1f(cache.get(parameterName), value);
	}

	public void setFloat2(final String parameterName, final float value0, final float value1) {
		glUniform2f(cache.get(parameterName), value0, value1);
	}

	public void setFloat3(final String parameterName, final float value0, final float value1, final float value2) {
		glUniform3f(cache.get(parameterName), value0, value1, value2);
	}

	public void setFloat3(final String parameterName, final Vector3f values) {
		glUniform3f(cache.get(parameterName), values.x, values.y, values.z);
	}

	public void setFloat3(final String parameterName, final float[] values) {
		glUniform3f(cache.get(parameterName), values[0], values[1], values[2]);
	}

}
