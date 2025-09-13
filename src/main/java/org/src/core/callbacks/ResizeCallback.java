package org.src.core.callbacks;

@FunctionalInterface
public interface ResizeCallback {
	void invoke(final long window, final int x, final int y);
}
