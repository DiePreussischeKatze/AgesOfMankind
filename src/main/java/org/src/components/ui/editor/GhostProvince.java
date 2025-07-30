package org.src.components.ui.editor;

import org.src.core.helper.Helper;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_COLOR;
import static org.lwjgl.opengl.GL15C.GL_DYNAMIC_DRAW;

public final class GhostProvince {
	private final Mesh mesh;

	public GhostProvince() {
		this.mesh = new Mesh(new byte[] { 2, 3 }, GL_DYNAMIC_DRAW);
	}

	public void changeMesh(final Mesh newMesh) {
		// we need to make a deep copy of the data
		final float[] newVertices = Helper.deepCopy(newMesh.vertices);
		final int[] newIndices = Helper.deepCopy(newMesh.indices);

		this.mesh.vertices = newVertices;
		this.mesh.indices = newIndices;

		// we need to assume the 3-rd index of the stride is the red color
		for (int i = 0; i < this.mesh.vertices.length; i += this.mesh.getStrideSum()) {
			this.mesh.vertices[i + 2] = this.mesh.vertices[i + 3] = this.mesh.vertices[i + 4] = 1.0f;
		}

		this.mesh.regenerateGeometry();
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
