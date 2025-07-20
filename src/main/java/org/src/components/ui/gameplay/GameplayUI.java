package org.src.components.ui.gameplay;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import org.src.components.environment.WorldCalendar;
import org.src.core.helper.Component;
import org.src.core.main.Window;

public final class GameplayUI extends Component {
	private final WorldCalendar worldCalendar;

	public GameplayUI() {
		worldCalendar = new WorldCalendar(2025, 7, 20, 13);
	}

	@Override
	public void draw() {
		drawDate();
		Window.uiEnd();
	}

	private void drawDate() {
		ImGui.begin("Date", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);

		ImGui.setWindowPos(Window.getWidth() - ImGui.getWindowWidth(), 0);
		ImGui.text(worldCalendar.getDate().year + " " + worldCalendar.getDate().month + " " + worldCalendar.getDate().day + " " + worldCalendar.toStringHour());
		ImGui.end();
	}

	@Override
	public void update(double deltaTime) {

	}

	@Override
	public void dispose() {

	}

}
