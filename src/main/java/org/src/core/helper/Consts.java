package org.src.core.helper;

import static org.src.core.helper.Helper.FLOAT;

public final class Consts {
	public static final float UPS_PER_SECOND = 1.0F / 120.0F;
	public static final float FRAMES_PER_SECOND = 1.0F / FLOAT(Config.get("maxFps"));

	public static final int[] RECT_INDICES = {
			0, 1, 3,
			1, 2, 3
	};

	public static final int POINT_POS_STRIDE = 2;

	public static final float[] DEEP_SEA_COLORS = { 0.0f, 0.05f, 0.1f };
	public static final float[] SHALLOW_SEA_COLORS = { 0.0f, 0.05f, 0.4f };
	public static final float[] COSTAL_SEA_COLORS = { 0.25f, 0.3f, 0.9f };
	public static final float[] BOG_COLORS = { 0f, 0.4f, 0.05f };
	public static final float[] LOWLANDS_COLORS = { 0.1f, 0.6f, 0.1f };
	public static final float[] HIGHLANDS_COLORS = { 0.1f, 0.9f, 0.1f };
	public static final float[] MOUNTAINS_COLORS = { 0.5f, 0.5f, 0.5f };
}
