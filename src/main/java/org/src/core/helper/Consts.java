package org.src.core.helper;

import static org.src.core.helper.Helper.FLOAT;

public final class Consts {
	public static final float UPS_PER_SECOND = 1.0F / 120.0F;
	public static final float FRAMES_PER_SECOND = 1.0F / FLOAT(Config.get("maxFps"));

	public static final int[] RECTANGLE_INDICES = {
			0, 1, 3,
			1, 2, 3
	};

	public static final int POINT_POS_STRIDE = 2;

}
