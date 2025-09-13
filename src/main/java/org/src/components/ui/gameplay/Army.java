package org.src.components.ui.gameplay;

import org.joml.Vector2f;
import org.src.components.civilisation.State;

public class Army {
	private final State owner;

	private final Vector2f pos;

	private final float[] color; // for now

	private boolean selected;

	private int size;
	
	private Vector2f orderedPosition;
	private float speed;

	public Army(final Vector2f pos, final State owner) {
		this.owner = owner;
		this.speed = 0.00004f;
		this.orderedPosition = new Vector2f(pos);
		this.color = new float[3];

		this.selected = false;
	
		this.pos = pos;

		this.color[0] = 1.0F;
	}

	public Vector2f getPos() {
		return this.pos;
	}

	public boolean isSelected() {
		return this.selected;
	}

	public void setOrderedPosition(final Vector2f value) {
		this.orderedPosition.set(value);
		// System.out.println("set the values");
	}

	public void setSelected(final boolean value) {
		this.selected = value;
	}

	public void update(final double deltaTime) {
		move(deltaTime);
	}

	private void move(final double deltaTime) {
		// System.out.println(pos.distance(orderedPosition));
		if (pos.distance(orderedPosition) < 0.002f) {
			return;
		}

		final float actualSpeed = (float) (speed * deltaTime);
		// temporary solution, gotta use the pythagorean theorem and trigonometry
		if (pos.x > orderedPosition.x) {
			pos.x -= actualSpeed;
		} else {
			pos.x += actualSpeed;
		}

		if (pos.y > orderedPosition.y) {
			pos.y -= actualSpeed;
		} else {
			pos.y += actualSpeed;
		}
	}

}
