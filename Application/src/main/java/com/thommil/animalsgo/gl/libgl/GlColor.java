package com.thommil.animalsgo.gl.libgl;

public class GlColor {

	public static final float WHITE = toFloatBits(1f, 1f, 1f, 1f);

	public static float toFloatBits (int r, int g, int b, int a) {
		int color = (a << 24) | (b << 16) | (g << 8) | r;
		float floatColor = Float.intBitsToFloat(color & 0xfeffffff);
		return floatColor;
	}

	public static float toFloatBits (float r, float g, float b, float a) {
		int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		return Float.intBitsToFloat(color & 0xfeffffff);
	}
}