package org.src.components.ui.gameplay;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;

import org.src.components.environment.WorldCalendar;
import org.src.components.ui.editor.EditorWindow;
import org.src.core.callbacks.KeyPressCallback;
import org.src.core.helper.Component;
import org.src.core.main.Window;
import org.src.core.managers.InputManager;

public final class GameplayUI extends Component {
	private final WorldCalendar worldCalendar;
	private final NotificationManager notificationManager;

	private boolean drawHud;
	private boolean isBottomBarNeeded;

	private int mainBarWidth;

	private final KeyPressCallback keyPressed = (long window, int key, int action, int mods) -> {
		switch (key) {
			case GLFW_KEY_F1 -> drawHud = !drawHud;
		}
	};

	public GameplayUI() {
		this.worldCalendar = new WorldCalendar(2025, 7, 20, 13);
		this.notificationManager = new NotificationManager();

		notificationManager.addNotification("Some important info");
		notificationManager.addNotification("Some important info2");
		
		EditorWindow.setStyle();
	
		this.drawHud = true;
		this.isBottomBarNeeded = false;
	
		InputManager.addKeyPressCallback(keyPressed);
	}

	@Override
	public void draw() {
		if (!drawHud) { return; }

		Window.uiBegin();
		drawDate();
		drawSidePanel();
		drawBottomPanel();
		notificationManager.draw();
		Window.uiEnd();
	}

	private void drawDate() {
		ImGui.begin("Date", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);
		ImGui.setWindowPos(Window.getWidth() - ImGui.getWindowWidth(), 0);
		ImGui.text(worldCalendar.getDate().year + " " + worldCalendar.getDate().month + " " + worldCalendar.getDate().day + " " + worldCalendar.toStringHour());
		ImGui.end();
	}

	private void drawSidePanel() {
		ImGui.begin("[INSERT STATE NAME]", ImGuiWindowFlags.NoMove);

		if (Window.getWidth() - 100 > 299) {
			ImGui.setWindowSize(new ImVec2(Math.clamp(ImGui.getWindowSizeX(), 300, Window.getWidth() - 100), Window.getHeight()));
		}

		mainBarWidth = (int) ImGui.getWindowSizeX();

		ImGui.setWindowPos(0, 0);

		// WINDOW CODE HERE
		

		ImGui.end();
	}

	private void drawBottomPanel() {
		if (!isBottomBarNeeded) { return; }

		ImGui.begin("[SOMETHING]", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize);
		
		ImGui.setWindowPos(new ImVec2(mainBarWidth, Window.getHeight() - 300));
		ImGui.setWindowSize(new ImVec2(Window.getWidth() - mainBarWidth, 300));

		ImGui.end();
	}

	@Override
	public void update(double deltaTime) {
		notificationManager.update(deltaTime);
	}

	@Override
	public void dispose() {
		notificationManager.dispose();
	}

	private void setIsBottomBarNeeded(final boolean value) {
		this.isBottomBarNeeded = value;
	}

}
