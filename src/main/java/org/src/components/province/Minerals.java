package org.src.components.province;

public final class Minerals {
	public float carbonTons;
	public float ironTons;
	public float goldTons;
	public float copperTons;
	public float oilTons;
	public float woodTons;

	Minerals() {
		carbonTons = 0.0f;
		ironTons = 0.0f;
		goldTons = 0.0f;
		copperTons = 0.0f;
		oilTons = 0.0f;
		woodTons = 0.0f;
	}

	Minerals(final float carbonTons,
			final float ironTons,
			final float goldTons,
			final float copperTons,
			final float oilTons,
			final float woodTons) {
		this.carbonTons = carbonTons;
		this.copperTons = copperTons;
		this.ironTons = ironTons;
		this.goldTons = goldTons;
		this.oilTons = oilTons;
		this.woodTons = woodTons;
	}

}
