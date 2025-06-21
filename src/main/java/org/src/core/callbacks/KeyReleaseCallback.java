package org.src.core.callbacks;

@FunctionalInterface
public interface KeyReleaseCallback {
	void invoke(final long window, final int key, final int action, final int mods);
}
