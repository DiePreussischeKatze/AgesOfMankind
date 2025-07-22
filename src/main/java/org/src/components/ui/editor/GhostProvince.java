package org.src.components.ui.editor;

import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;

public final class GhostProvince {
	private Mesh mesh;

	public GhostProvince() {
		this.mesh = new Mesh(new byte[] { 2, 3});
	}

	public void changeMesh(final Mesh mesh) {
		// we need to make a deep copy of the data
		final float[] newVertices = new float[mesh.vertices.length];
		System.arraycopy(mesh.vertices, 0, newVertices, 0, mesh.vertices.length);

		final int[] newIndices = new int[mesh.indices.length];
		System.arraycopy(mesh.indices, 0, newIndices, 0, mesh.indices.length);

		this.mesh = new Mesh(newVertices, newIndices, new byte[] { 2, 3 });

		// we need to assume the 3-rd index of the stride is the red color
		for (int i = 0; i < this.mesh.vertices.length; i += this.mesh.getStrideSum()) {
			this.mesh.vertices[i + 2] = this.mesh.vertices[i + 3] = this.mesh.vertices[i + 4] = 1.0f;
		}
		this.mesh.regenerate();
	}

	public void draw() {
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_SRC_COLOR);
		ShaderManager.get(ShaderID.GHOST).bind();
		mesh.draw();
		glDisable(GL_BLEND);
	}

	public void dispose() {
		this.mesh.dispose();
	}

}
