package org.src.components.province;

public final class Soil {
	public float forestCoverage;
	public float quality;

	public boolean isLandProvince;
	public boolean hasRivers;

	public int elevation;

	Soil() {
		this.elevation = 0;
		this.forestCoverage = 0.0f;
		this.hasRivers = false;
		this.quality = 0.0f;
	}

	Soil(final float forestCoverage,
			final float quality,
			final boolean isLandProvince,
			final boolean hasRivers,
			final int elevation) {
		this.forestCoverage = forestCoverage;
		this.quality = quality;
		this.isLandProvince = isLandProvince;
		this.hasRivers = hasRivers;
		this.elevation = elevation;
	}

}
