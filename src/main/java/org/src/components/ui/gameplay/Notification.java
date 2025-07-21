package org.src.components.ui.gameplay;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import org.joml.Vector2i;


public final class Notification {
	static final Vector2i SIZE;

	static {
		SIZE = new Vector2i(250, 80);
	}

	private final ImVec2 pos;

	private float life;
	private float maxLife;

	private final String text;

	private long id;

	Notification(final Vector2i pos, final float life, final String text, final long id) {
		this.pos = new ImVec2(pos.x, pos.y);
		this.life = life;
		this.maxLife = this.life;
		this.text = text;
		this.id = id;
	}

	void updateLife(final double deltaTime) {
		life -= (float) deltaTime;
	}

	public float getLife() {
		return this.life;
	}

	void draw() {
		final ImGuiStyle style = ImGui.getStyle();
		style.setColor(ImGuiCol.WindowBg, 0, 0, 0, (life / maxLife));
		ImGui.begin("Notification" + id, ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoCollapse);
		ImGui.setWindowPos(pos);
		ImGui.setWindowSize(SIZE.x, SIZE.y);
		ImGui.textWrapped(text);
		ImGui.end();
		style.setColor(ImGuiCol.WindowBg, 0, 0, 0, 255);
	}

}
