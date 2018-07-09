/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


package com.thommil.animalsgo.gl.libgl;

/** A color class, holding the r, g, b and alpha component as floats in the range [0,1]. All methods perform clamping on the
 * internal values after execution.
 * 
 * @author mzechner */
public class GlColor {
	public static final GlColor WHITE = new GlColor(1, 1, 1,1);
	public static final GlColor LIGHT_GRAY = new GlColor(0xbfbfbfff);
	public static final GlColor GRAY = new GlColor(0x7f7f7fff);
	public static final GlColor DARK_GRAY = new GlColor(0x3f3f3fff);
	public static final GlColor BLACK = new GlColor(0, 0, 0, 1);

	/** Convenience for frequently used <code>WHITE.toFloatBits()</code> */
	public static final float WHITE_FLOAT_BITS = WHITE.toFloatBits();

	public static final GlColor CLEAR = new GlColor(0, 0, 0, 0);

	public static final GlColor BLUE = new GlColor(0, 0, 1, 1);
	public static final GlColor NAVY = new GlColor(0, 0, 0.5f, 1);
	public static final GlColor ROYAL = new GlColor(0x4169e1ff);
	public static final GlColor SLATE = new GlColor(0x708090ff);
	public static final GlColor SKY = new GlColor(0x87ceebff);
	public static final GlColor CYAN = new GlColor(0, 1, 1, 1);
	public static final GlColor TEAL = new GlColor(0, 0.5f, 0.5f, 1);

	public static final GlColor GREEN = new GlColor(0x00ff00ff);
	public static final GlColor CHARTREUSE = new GlColor(0x7fff00ff);
	public static final GlColor LIME = new GlColor(0x32cd32ff);
	public static final GlColor FOREST = new GlColor(0x228b22ff);
	public static final GlColor OLIVE = new GlColor(0x6b8e23ff);

	public static final GlColor YELLOW = new GlColor(0xffff00ff);
	public static final GlColor GOLD = new GlColor(0xffd700ff);
	public static final GlColor GOLDENROD = new GlColor(0xdaa520ff);
	public static final GlColor ORANGE = new GlColor(0xffa500ff);

	public static final GlColor BROWN = new GlColor(0x8b4513ff);
	public static final GlColor TAN = new GlColor(0xd2b48cff);
	public static final GlColor FIREBRICK = new GlColor(0xb22222ff);

	public static final GlColor RED = new GlColor(0xff0000ff);
	public static final GlColor SCARLET = new GlColor(0xff341cff);
	public static final GlColor CORAL = new GlColor(0xff7f50ff);
	public static final GlColor SALMON = new GlColor(0xfa8072ff);
	public static final GlColor PINK = new GlColor(0xff69b4ff);
	public static final GlColor MAGENTA = new GlColor(1, 0, 1, 1);

	public static final GlColor PURPLE = new GlColor(0xa020f0ff);
	public static final GlColor VIOLET = new GlColor(0xee82eeff);
	public static final GlColor MAROON = new GlColor(0xb03060ff);

	/** the red, green, blue and alpha components **/
	public float r, g, b, a;

	/** Constructs a new GlColor with all components set to 0. */
	public GlColor() {
	}

	/** @see #rgba8888ToColor(GlColor, int) */
	public GlColor(int rgba8888) {
		rgba8888ToColor(this, rgba8888);
	}

	/** Constructor, sets the components of the color
	 * 
	 * @param r the red component
	 * @param g the green component
	 * @param b the blue component
	 * @param a the alpha component */
	public GlColor(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		clamp();
	}

	/** Constructs a new color using the given color
	 * 
	 * @param color the color */
	public GlColor(GlColor color) {
		set(color);
	}

	/** Sets this color to the given color.
	 * 
	 * @param color the GlColor */
	public GlColor set (GlColor color) {
		this.r = color.r;
		this.g = color.g;
		this.b = color.b;
		this.a = color.a;
		return this;
	}

	/** Multiplies the this color and the given color
	 * 
	 * @param color the color
	 * @return this color. */
	public GlColor mul (GlColor color) {
		this.r *= color.r;
		this.g *= color.g;
		this.b *= color.b;
		this.a *= color.a;
		return clamp();
	}

	/** Multiplies all components of this GlColor with the given value.
	 * 
	 * @param value the value
	 * @return this color */
	public GlColor mul (float value) {
		this.r *= value;
		this.g *= value;
		this.b *= value;
		this.a *= value;
		return clamp();
	}

	/** Adds the given color to this color.
	 * 
	 * @param color the color
	 * @return this color */
	public GlColor add (GlColor color) {
		this.r += color.r;
		this.g += color.g;
		this.b += color.b;
		this.a += color.a;
		return clamp();
	}

	/** Subtracts the given color from this color
	 * 
	 * @param color the color
	 * @return this color */
	public GlColor sub (GlColor color) {
		this.r -= color.r;
		this.g -= color.g;
		this.b -= color.b;
		this.a -= color.a;
		return clamp();
	}

	/** Clamps this GlColor's components to a valid range [0 - 1]
	 * @return this GlColor for chaining */
	public GlColor clamp () {
		if (r < 0)
			r = 0;
		else if (r > 1) r = 1;

		if (g < 0)
			g = 0;
		else if (g > 1) g = 1;

		if (b < 0)
			b = 0;
		else if (b > 1) b = 1;

		if (a < 0)
			a = 0;
		else if (a > 1) a = 1;
		return this;
	}

	/** Sets this GlColor's component values.
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 * 
	 * @return this GlColor for chaining */
	public GlColor set (float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
		return clamp();
	}

	/** Sets this color's component values through an integer representation.
	 * 
	 * @return this GlColor for chaining
	 * @see #rgba8888ToColor(GlColor, int) */
	public GlColor set (int rgba) {
		rgba8888ToColor(this, rgba);
		return this;
	}

	/** Adds the given color component values to this GlColor's values.
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 * 
	 * @return this GlColor for chaining */
	public GlColor add (float r, float g, float b, float a) {
		this.r += r;
		this.g += g;
		this.b += b;
		this.a += a;
		return clamp();
	}

	/** Subtracts the given values from this GlColor's component values.
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 * 
	 * @return this GlColor for chaining */
	public GlColor sub (float r, float g, float b, float a) {
		this.r -= r;
		this.g -= g;
		this.b -= b;
		this.a -= a;
		return clamp();
	}

	/** Multiplies this GlColor's color components by the given ones.
	 * 
	 * @param r Red component
	 * @param g Green component
	 * @param b Blue component
	 * @param a Alpha component
	 * 
	 * @return this GlColor for chaining */
	public GlColor mul (float r, float g, float b, float a) {
		this.r *= r;
		this.g *= g;
		this.b *= b;
		this.a *= a;
		return clamp();
	}

	/** Linearly interpolates between this color and the target color by t which is in the range [0,1]. The result is stored in
	 * this color.
	 * @param target The target color
	 * @param t The interpolation coefficient
	 * @return This color for chaining. */
	public GlColor lerp (final GlColor target, final float t) {
		this.r += t * (target.r - this.r);
		this.g += t * (target.g - this.g);
		this.b += t * (target.b - this.b);
		this.a += t * (target.a - this.a);
		return clamp();
	}

	/** Linearly interpolates between this color and the target color by t which is in the range [0,1]. The result is stored in
	 * this color.
	 * @param r The red component of the target color
	 * @param g The green component of the target color
	 * @param b The blue component of the target color
	 * @param a The alpha component of the target color
	 * @param t The interpolation coefficient
	 * @return This color for chaining. */
	public GlColor lerp (final float r, final float g, final float b, final float a, final float t) {
		this.r += t * (r - this.r);
		this.g += t * (g - this.g);
		this.b += t * (b - this.b);
		this.a += t * (a - this.a);
		return clamp();
	}

	/** Multiplies the RGB values by the alpha. */
	public GlColor premultiplyAlpha () {
		r *= a;
		g *= a;
		b *= a;
		return this;
	}

	@Override
	public boolean equals (Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GlColor color = (GlColor)o;
		return toIntBits() == color.toIntBits();
	}

	@Override
	public int hashCode () {
		int result = (r != +0.0f ? Float.floatToIntBits(r) : 0);
		result = 31 * result + (g != +0.0f ? Float.floatToIntBits(g) : 0);
		result = 31 * result + (b != +0.0f ? Float.floatToIntBits(b) : 0);
		result = 31 * result + (a != +0.0f ? Float.floatToIntBits(a) : 0);
		return result;
	}

	/** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float.
	 * @return the packed color as a 32-bit float
	 **/
	public float toFloatBits () {
		int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		return Float.intBitsToFloat(color & 0xfeffffff);
	}

	/** Packs the color components into a 32-bit integer with the format ABGR.
	 * @return the packed color as a 32-bit int. */
	public int toIntBits () {
		int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		return color;
	}

	/** Returns the color encoded as hex string with the format RRGGBBAA. */
	public String toString () {
		String value = Integer
			.toHexString(((int)(255 * r) << 24) | ((int)(255 * g) << 16) | ((int)(255 * b) << 8) | ((int)(255 * a)));
		while (value.length() < 8)
			value = "0" + value;
		return value;
	}

	/** Returns a new color from a hex string with the format RRGGBBAA.
	 * @see #toString() */
	public static GlColor valueOf (String hex) {
		hex = hex.charAt(0) == '#' ? hex.substring(1) : hex;
		int r = Integer.valueOf(hex.substring(0, 2), 16);
		int g = Integer.valueOf(hex.substring(2, 4), 16);
		int b = Integer.valueOf(hex.substring(4, 6), 16);
		int a = hex.length() != 8 ? 255 : Integer.valueOf(hex.substring(6, 8), 16);
		return new GlColor(r / 255f, g / 255f, b / 255f, a / 255f);
	}

	/** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float. Note that no range
	 * checking is performed for higher performance.
	 * @param r the red component, 0 - 255
	 * @param g the green component, 0 - 255
	 * @param b the blue component, 0 - 255
	 * @param a the alpha component, 0 - 255
	 * @return the packed color as a float
	 **/
	public static float toFloatBits (int r, int g, int b, int a) {
		int color = (a << 24) | (b << 16) | (g << 8) | r;
		float floatColor = Float.intBitsToFloat(color & 0xfeffffff);
		return floatColor;
	}

	/** Packs the color components into a 32-bit integer with the format ABGR and then converts it to a float.
	 * @return the packed color as a 32-bit float
	 **/
	public static float toFloatBits (float r, float g, float b, float a) {
		int color = ((int)(255 * a) << 24) | ((int)(255 * b) << 16) | ((int)(255 * g) << 8) | ((int)(255 * r));
		return Float.intBitsToFloat(color & 0xfeffffff);
	}

	/** Packs the color components into a 32-bit integer with the format ABGR. Note that no range checking is performed for higher
	 * performance.
	 * @param r the red component, 0 - 255
	 * @param g the green component, 0 - 255
	 * @param b the blue component, 0 - 255
	 * @param a the alpha component, 0 - 255
	 * @return the packed color as a 32-bit int */
	public static int toIntBits (int r, int g, int b, int a) {
		return (a << 24) | (b << 16) | (g << 8) | r;
	}

	public static int alpha (float alpha) {
		return (int)(alpha * 255.0f);
	}

	public static int luminanceAlpha (float luminance, float alpha) {
		return ((int)(luminance * 255.0f) << 8) | (int)(alpha * 255);
	}

	public static int rgb565 (float r, float g, float b) {
		return ((int)(r * 31) << 11) | ((int)(g * 63) << 5) | (int)(b * 31);
	}

	public static int rgba4444 (float r, float g, float b, float a) {
		return ((int)(r * 15) << 12) | ((int)(g * 15) << 8) | ((int)(b * 15) << 4) | (int)(a * 15);
	}

	public static int rgb888 (float r, float g, float b) {
		return ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
	}

	public static int rgba8888 (float r, float g, float b, float a) {
		return ((int)(r * 255) << 24) | ((int)(g * 255) << 16) | ((int)(b * 255) << 8) | (int)(a * 255);
	}

	public static int argb8888 (float a, float r, float g, float b) {
		return ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
	}

	public static int rgb565 (GlColor color) {
		return ((int)(color.r * 31) << 11) | ((int)(color.g * 63) << 5) | (int)(color.b * 31);
	}

	public static int rgba4444 (GlColor color) {
		return ((int)(color.r * 15) << 12) | ((int)(color.g * 15) << 8) | ((int)(color.b * 15) << 4) | (int)(color.a * 15);
	}

	public static int rgb888 (GlColor color) {
		return ((int)(color.r * 255) << 16) | ((int)(color.g * 255) << 8) | (int)(color.b * 255);
	}

	public static int rgba8888 (GlColor color) {
		return ((int)(color.r * 255) << 24) | ((int)(color.g * 255) << 16) | ((int)(color.b * 255) << 8) | (int)(color.a * 255);
	}

	public static int argb8888 (GlColor color) {
		return ((int)(color.a * 255) << 24) | ((int)(color.r * 255) << 16) | ((int)(color.g * 255) << 8) | (int)(color.b * 255);
	}

	/** Sets the GlColor components using the specified integer value in the format RGB565. This is inverse to the rgb565(r, g, b)
	 * method.
	 * 
	 * @param color The GlColor to be modified.
	 * @param value An integer color value in RGB565 format. */
	public static void rgb565ToColor (GlColor color, int value) {
		color.r = ((value & 0x0000F800) >>> 11) / 31f;
		color.g = ((value & 0x000007E0) >>> 5) / 63f;
		color.b = ((value & 0x0000001F) >>> 0) / 31f;
	}

	/** Sets the GlColor components using the specified integer value in the format RGBA4444. This is inverse to the rgba4444(r, g,
	 * b, a) method.
	 * 
	 * @param color The GlColor to be modified.
	 * @param value An integer color value in RGBA4444 format. */
	public static void rgba4444ToColor (GlColor color, int value) {
		color.r = ((value & 0x0000f000) >>> 12) / 15f;
		color.g = ((value & 0x00000f00) >>> 8) / 15f;
		color.b = ((value & 0x000000f0) >>> 4) / 15f;
		color.a = ((value & 0x0000000f)) / 15f;
	}

	/** Sets the GlColor components using the specified integer value in the format RGB888. This is inverse to the rgb888(r, g, b)
	 * method.
	 * 
	 * @param color The GlColor to be modified.
	 * @param value An integer color value in RGB888 format. */
	public static void rgb888ToColor (GlColor color, int value) {
		color.r = ((value & 0x00ff0000) >>> 16) / 255f;
		color.g = ((value & 0x0000ff00) >>> 8) / 255f;
		color.b = ((value & 0x000000ff)) / 255f;
	}

	/** Sets the GlColor components using the specified integer value in the format RGBA8888. This is inverse to the rgba8888(r, g,
	 * b, a) method.
	 * 
	 * @param color The GlColor to be modified.
	 * @param value An integer color value in RGBA8888 format. */
	public static void rgba8888ToColor (GlColor color, int value) {
		color.r = ((value & 0xff000000) >>> 24) / 255f;
		color.g = ((value & 0x00ff0000) >>> 16) / 255f;
		color.b = ((value & 0x0000ff00) >>> 8) / 255f;
		color.a = ((value & 0x000000ff)) / 255f;
	}

	/** Sets the GlColor components using the specified integer value in the format ARGB8888. This is the inverse to the argb8888(a,
	 * r, g, b) method
	 *
	 * @param color The GlColor to be modified.
	 * @param value An integer color value in ARGB8888 format. */
	public static void argb8888ToColor (GlColor color, int value) {
		color.a = ((value & 0xff000000) >>> 24) / 255f;
		color.r = ((value & 0x00ff0000) >>> 16) / 255f;
		color.g = ((value & 0x0000ff00) >>> 8) / 255f;
		color.b = ((value & 0x000000ff)) / 255f;
	}

	/** Sets the GlColor components using the specified float value in the format ABGB8888.
	 * @param color The GlColor to be modified. */
	public static void abgr8888ToColor (GlColor color, float value) {
		int c = Float.floatToRawIntBits(value);
		color.a = ((c & 0xff000000) >>> 24) / 255f;
		color.b = ((c & 0x00ff0000) >>> 16) / 255f;
		color.g = ((c & 0x0000ff00) >>> 8) / 255f;
		color.r = ((c & 0x000000ff)) / 255f;
	}

	/** Sets the RGB GlColor components using the specified Hue-Saturation-Value. Note that HSV components are voluntary not clamped
	 * to preserve high range color and can range beyond typical values.
	 * @param h The Hue in degree from 0 to 360
	 * @param s The Saturation from 0 to 1
	 * @param v The Value (brightness) from 0 to 1
	 * @return The modified GlColor for chaining. */
	public GlColor fromHsv (float h, float s, float v) {
		float x = (h / 60f + 6) % 6;
		int i = (int)x;
		float f = x - i;
		float p = v * (1 - s);
		float q = v * (1 - s * f);
		float t = v * (1 - s * (1 - f));
		switch (i) {
		case 0:
			r = v;
			g = t;
			b = p;
			break;
		case 1:
			r = q;
			g = v;
			b = p;
			break;
		case 2:
			r = p;
			g = v;
			b = t;
			break;
		case 3:
			r = p;
			g = q;
			b = v;
			break;
		case 4:
			r = t;
			g = p;
			b = v;
			break;
		default:
			r = v;
			g = p;
			b = q;
		}

		return clamp();
	}

	/** Sets RGB components using the specified Hue-Saturation-Value. This is a convenient method for
	 * {@link #fromHsv(float, float, float)}. This is the inverse of {@link #toHsv(float[])}.
	 * @param hsv The Hue, Saturation and Value components in that order.
	 * @return The modified GlColor for chaining. */
	public GlColor fromHsv (float[] hsv) {
		return fromHsv(hsv[0], hsv[1], hsv[2]);
	}

	/** Extract Hue-Saturation-Value. This is the inverse of {@link #fromHsv(float[])}.
	 * @param hsv The HSV array to be modified.
	 * @return HSV components for chaining. */
	public float[] toHsv (float[] hsv) {
		float max = Math.max(Math.max(r, g), b);
		float min = Math.min(Math.min(r, g), b);
		float range = max - min;
		if (range == 0) {
			hsv[0] = 0;
		} else if (max == r) {
			hsv[0] = (60 * (g - b) / range + 360) % 360;
		} else if (max == g) {
			hsv[0] = 60 * (b - r) / range + 120;
		} else {
			hsv[0] = 60 * (r - g) / range + 240;
		}

		if (max > 0) {
			hsv[1] = 1 - min / max;
		} else {
			hsv[1] = 0;
		}

		hsv[2] = max;

		return hsv;
	}

	/** @return a copy of this color */
	public GlColor cpy () {
		return new GlColor(this);
	}
}