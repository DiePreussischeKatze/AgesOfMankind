package org.src.components.ui.gameplay;

import org.joml.Vector2i;
import org.src.core.helper.Component;
import org.src.core.main.Window;

import java.util.ArrayList;

public final class NotificationManager extends Component {

	private final ArrayList<Notification> notifications;

	private static final Vector2i START_POS;

	private float currentPos;

	private long currentId; // this is gonna be enough ids

	static {
		START_POS = new Vector2i(Window.getWidth() - Notification.SIZE.x - 5, Window.getHeight() - Notification.SIZE.y - 5);
	}

	public NotificationManager() {
		this.notifications = new ArrayList<>();
		this.currentPos = START_POS.y;
		this.currentId = 0;
	}

	public void clear() {
		this.notifications.clear();
	}

	public void addNotification(final String text) {
		this.notifications.add(new Notification(new Vector2i(START_POS.x, (int) currentPos), 500, text, currentId));
		this.currentPos -= Notification.SIZE.y + 5;
		this.currentId++;
	}

	void clearUnwantedNotifications() {
		for (int i = 0; i < notifications.size(); i++) {
			if (notifications.get(i).getLife() <= 0) {
				notifications.remove(notifications.get(i));
			}
		}
	}

	@Override
	public void draw() {
		this.notifications.forEach(Notification::draw);
	}

	@Override
	public void update(double deltaTime) {
		this.notifications.forEach(n -> n.updateLife(deltaTime));
		clearUnwantedNotifications();
	}

	@Override
	public void dispose() {

	}

}
