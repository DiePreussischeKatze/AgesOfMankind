package org.src.components.province;

public final class Climate {
	public float maxTemp;
	public float minTemp;

	// 0.0 means totally continental; 1.0 means totally marine climate (this will decide the curvature between min and max temps)
	public float character;

	public int sunlightHoursAYear;

	public float precipitationMMAYear;

	Climate() {
		this.maxTemp = 0.0f;
		this.minTemp = 0.0f;
		this.character = 0.5f;
		this.sunlightHoursAYear = 0;
		this.precipitationMMAYear = 0.0f;
	}

	Climate(final double maxTemp,
			final double minTemp,
			final double character,
			final int sunlightHoursAYear,
			final float precipitationMMAYear
	) {
		this.maxTemp = (float) maxTemp;
		this.minTemp = (float) minTemp;
		this.character = (float) character;
		this.sunlightHoursAYear = sunlightHoursAYear;
		this.precipitationMMAYear = precipitationMMAYear;
	}

}
