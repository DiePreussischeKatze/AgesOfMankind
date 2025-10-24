package org.src.components.ui.gameplay;

import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImInt;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;

import org.src.components.civilisation.State;
import org.src.components.environment.WorldCalendar;
import org.src.components.map.DisplayMode;
import org.src.components.map.Map;
import org.src.components.ui.editor.EditorWindow;
import org.src.core.callbacks.KeyPressCallback;
import org.src.core.helper.Component;
import org.src.core.helper.Helper;
import org.src.core.main.Window;
import org.src.core.managers.InputManager;
import org.src.scenes.GameplayScene;

public final class GameplayUI extends Component {
	private WorldCalendar worldCalendar;
	private final NotificationManager notificationManager;

	private Map map;
	private final GameplayScene gameplayScene;

	private State selectedState;

	private boolean drawHud;
	private boolean isBottomBarNeeded;

	private int mainBarWidth;

	private final KeyPressCallback keyPressed = (long window, int key, int action, int mods) -> {
		switch (key) {
			case GLFW_KEY_F1 -> drawHud = !drawHud;
		}
		// worldCalendar.incrementQuarter();
	};

	public GameplayUI(final Map map, final GameplayScene gameplayScene) {
		this.worldCalendar = new WorldCalendar(1934, 3, 2, 2, 0);
		this.notificationManager = new NotificationManager();

		this.gameplayScene = gameplayScene;
		this.map = map;
		
		this.selectedState = map.getState("Germany");
		if (this.selectedState == null) {
			this.selectedState = map.getStates().getFirst();
		}
		
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
		drawMapSelection();
		drawPauseNotificationWindow();
		notificationManager.draw();
		Window.uiEnd();
	}

	private void drawPauseNotificationWindow() {
		if (!gameplayScene.isGamePaused()) { return; }

		ImGui.begin("Pause notification", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoInputs | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoScrollbar);
		ImGui.setWindowPos((Window.getWidth() + mainBarWidth - 200) / 2, Window.getHeight() / 10);
		ImGui.setWindowSize(200, 30);
		
		final String text = "Game Paused";
		final float textWidth = ImGui.calcTextSize(text).x;
		ImGui.setCursorPosX((ImGui.getWindowWidth() - textWidth) / 2);
		ImGui.text(text);

		ImGui.end();
	}

	private void drawDate() {
		ImGui.begin("Date", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoResize);
		ImGui.setWindowPos(Window.getWidth() - ImGui.getWindowWidth(), 0);
		ImGui.setWindowSize(250, 90);

		// display the text at the center
		final String worldCalendarText = worldCalendar.getDate().year + " " + worldCalendar.getDate().month + " " + worldCalendar.getDate().day + " " + worldCalendar.toStringHour();
		final float textWidth = ImGui.calcTextSize(worldCalendarText).x;
		
		ImGui.setCursorPosX((ImGui.getWindowWidth() - textWidth) / 2);
		ImGui.text(worldCalendarText);
		
		// time control buttons
		if (ImGui.button("<<")) {
			gameplayScene.decrementGameSpeed();
		} // TODO: add some floating popups when hovered

		ImGui.sameLine();
		if (ImGui.button(gameplayScene.isGamePaused() ? "Unpause" : "Pause")) {
			gameplayScene.togglePause();
		}

		ImGui.sameLine();
		if (ImGui.button(">>")) {
			gameplayScene.incrementGameSpeed();
		}

		ImGui.end();
	}

	private void drawSidePanel() {
		ImGui.begin(selectedState.getName().get(), ImGuiWindowFlags.NoMove);

		if (Window.getWidth() - 100 > 299) {
			ImGui.setWindowSize(new ImVec2(Math.clamp(ImGui.getWindowSizeX(), 300, Window.getWidth() - 100), Window.getHeight()));
		}

		mainBarWidth = (int) ImGui.getWindowSizeX();

		ImGui.setWindowPos(0, 0);

		// WINDOW CODE HERE
		ImGui.text("State population: " + Helper.readableSTR(selectedState.getPopulation()));

		ImGui.end();
	}
	
	private void drawBottomPanel() {
		if (!isBottomBarNeeded) { return; }

		ImGui.begin("[SOMETHING]", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize);
		
		ImGui.setWindowPos(new ImVec2(mainBarWidth, Window.getHeight() - 300));
		ImGui.setWindowSize(new ImVec2(Window.getWidth() - mainBarWidth, 300));

		ImGui.end();
	}

	private void drawMapSelection() {
		ImGui.begin("Modes", ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);
		ImGui.setWindowPos(new ImVec2(mainBarWidth + 10, 0));
		ImGui.setWindowSize(new ImVec2(150, 40));

		final ImInt currentMode = new ImInt(map.getDisplayMode().ordinal());

		if (ImGui.combo(" ", currentMode, Map.RENDERING_MODES)) {
			map.setDisplayMode(DisplayMode.values()[currentMode.get()]);
		}

		ImGui.end();
	}

	public void tickCalendar() {
		worldCalendar.incrementQuarter();
	}

	@Override
	public void update(double deltaTime) {
		notificationManager.update(deltaTime);
	}

	@Override
	public void dispose() {
		notificationManager.dispose();
	}

	public void setIsBottomBarNeeded(final boolean value) {
		this.isBottomBarNeeded = value;
	}

}
