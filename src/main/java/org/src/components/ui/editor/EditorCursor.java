package org.src.components.ui.editor;

import org.src.core.helper.Component;
import org.src.core.helper.ShaderID;
import org.src.core.managers.ShaderManager;
import org.src.rendering.wrapper.Mesh;

import static org.src.core.helper.Consts.RECTANGLE_INDICES;
import static org.src.core.helper.Helper.isInImGuiWindow;

public final class EditorCursor extends Component {
	private final Mesh boxMesh;

	private float x;
	private float y;

	public EditorCursor() {
		boxMesh = new Mesh(new float[]{
				0.0008f,  0.0008f,
				0.0008f, -0.0008f,
				-0.0008f, -0.0008f,
				-0.0008f,  0.0008f,
		}, RECTANGLE_INDICES, new byte[] {
				2
		});
	}

	public void updatePosition(final float x, final float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void draw() {
		if (isInImGuiWindow()) { return; }
		ShaderManager.get(ShaderID.EDITOR).bind();
		ShaderManager.get(ShaderID.EDITOR).setFloat2("offset", x, y);
		boxMesh.draw();
	}

	@Override
	public void update(double deltaTime) {}

	@Override
	public void dispose() {
		boxMesh.dispose();
	}

}
