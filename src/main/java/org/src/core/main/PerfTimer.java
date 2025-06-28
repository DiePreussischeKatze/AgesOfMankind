package org.src.core.main;

/**
 * A timer for debugging purposes
 * duck jetbrains for not giving the profiler in the free edition
 */
public final class PerfTimer {

	private final String name;

	private long elapsedTime;

	public PerfTimer(final String name) {
		this.name = name;

		elapsedTime = System.nanoTime();
	}

	public void reset() {
		System.out.println("Timer: " + name + " reported: " + (System.nanoTime() - elapsedTime) + " nanoseconds of code execution time");
		elapsedTime = System.nanoTime();
	}

}
