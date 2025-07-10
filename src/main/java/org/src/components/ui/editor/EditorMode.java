package org.src.components.ui.editor;

public abstract class EditorMode {

	protected final Editor editor;

	public EditorMode(final Editor editor) {
		this.editor = editor;
	}

	public abstract void keyPressAction(final int key);
	public abstract void keyReleaseAction(final int key);

	public abstract void mouseMovedAction();

	public abstract void mouseLeftPressedAction();
	public abstract void mouseLeftReleasedAction();

	public abstract void mouseRightPressedAction();
	public abstract void mouseRightReleasedAction();

	public abstract void renderGui();
	public abstract void draw();

	public abstract void update(final double deltaTime);

}
