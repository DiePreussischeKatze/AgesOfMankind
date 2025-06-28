package org.src.core.helper;

import org.joml.Vector2f;

public final class Rect2D {
	private float x;
	private float y;
	private float width;
	private float height;

	public Rect2D(final float x, final float y, final float width, final float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public Rect2D() {
		this.x = 0;
		this.y = 0;
		this.width = 0;
		this.height = 0;
	}

	public boolean intersects(final Rect2D what) {
		return this.x + this.width >= what.x && this.x <= what.x + what.width && this.y + this.height >= what.y && this.y <= what.y + what.height;
	}

	public boolean intersects(final float x, final float y, final float width, final float height) {
		return this.x + this.width > x && this.x < x + width && this.y + this.height > y && this.y < y + height;
	}

	public boolean intersects(final Vector2f start, final Vector2f end) {
		return this.x + this.width < end.x && this.x > start.x && this.y + this.height < end.y && this.y > start.y;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	public void setDimensions(final float x, final float y, final float width, final float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

}
