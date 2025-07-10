package org.src.components.ui.editor;

import org.joml.Vector2f;
import org.src.core.helper.Component;
import org.src.core.helper.Helper;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;

import static org.lwjgl.opengl.GL11.*;
import static org.src.core.helper.Helper.isInImGuiWindow;

public final class EditorCursor extends Component {
	private final Mesh boxMesh;

	private Vector2f position;

	EditorCursor() {
		this.position = new Vector2f();

		this.boxMesh = Helper.createPlainBoxMesh(0.0008f, 0.0008f);
	}

	void updatePos(final float x, final float y) {
		position.x = x;
		position.y = y;
	}

	void updatePos(final Vector2f position) {
		this.position = position;
	}

	@Override
	public void draw() {
		if (isInImGuiWindow()) { return; }

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_SRC_COLOR);
		ShaderManager.get(ShaderID.EDITOR).bind();
		ShaderManager.get(ShaderID.EDITOR).setFloat2("offset", position.x, position.y);
		ShaderManager.get(ShaderID.EDITOR).setFloat3("color", 0.8f, 0.6f, 0.1f);
		boxMesh.draw();
		glDisable(GL_BLEND);
	}

	Mesh getBoxMesh() {
		return boxMesh;
	}

	public Vector2f getPosition() {
		return position;
	}

	@Override
	public void update(double deltaTime) {}

	@Override
	public void dispose() {
		boxMesh.dispose();
	}

}
