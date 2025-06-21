package org.src.core.callbacks;

@FunctionalInterface
public interface KeyPressCallback {
	void invoke(final long window, final int key, final int action, final int mods);
}
