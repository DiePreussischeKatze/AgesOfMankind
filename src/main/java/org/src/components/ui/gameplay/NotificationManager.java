package org.src.components.ui.gameplay;

import org.joml.Vector2i;
import org.src.core.callbacks.ResizeCallback;
import org.src.core.helper.Component;
import org.src.core.main.Window;

import java.util.ArrayList;

public final class NotificationManager extends Component {

	private int oldY;

	private static final int NOTIFICATION_OFFSET = 5;

	private ArrayList<Notification> notifications;

	private static final Vector2i START_POS;

	private float currentPos;

	private long currentId; // this is gonna be enough ids

	private final ResizeCallback resizeCallback = (final long window, final int x, final int y) -> {
		for (final Notification notification: notifications) {
			notification.setX(x - NOTIFICATION_OFFSET - Notification.SIZE.x);

			final int yChange = oldY - Window.getHeight();
			notification.setY(notification.getY() - yChange);
		}

		oldY = Window.getHeight();
	};

	static {
		START_POS = new Vector2i(Window.getWidth() - Notification.SIZE.x - NOTIFICATION_OFFSET, Window.getHeight() - Notification.SIZE.y - NOTIFICATION_OFFSET);
	}

	public NotificationManager() {
		this.notifications = new ArrayList<>();
		this.currentPos = START_POS.y;
		this.currentId = 0;

		Window.addResizeCallback(resizeCallback);

		this.oldY = Window.getHeight();
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
