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

package com.thommil.animalsgo.utils;

import java.util.Random;

/** Utility and fast math functions.
 * <p>
 * Thanks to Riven on JavaGaming.org for the basis of sin/cos/floor/ceil.
 * @author Nathan Sweet */
public final class MathUtils {
	static public final float nanoToSec = 1 / 1000000000f;

	// ---
	static public final float FLOAT_ROUNDING_ERROR = 0.000001f; // 32 bits
	static public final float PI = 3.1415927f;
	static public final float PI2 = PI * 2;

	static public final float E = 2.7182818f;

	static private final int SIN_BITS = 14; // 16KB. Adjust for accuracy.
	static private final int SIN_MASK = ~(-1 << SIN_BITS);
	static private final int SIN_COUNT = SIN_MASK + 1;

	static private final float radFull = PI * 2;
	static private final float degFull = 360;
	static private final float radToIndex = SIN_COUNT / radFull;
	static private final float degToIndex = SIN_COUNT / degFull;

	/** multiply by this to convert from radians to degrees */
	static public final float radiansToDegrees = 180f / PI;
	static public final float radDeg = radiansToDegrees;
	/** multiply by this to convert from degrees to radians */
	static public final float degreesToRadians = PI / 180;
	static public final float degRad = degreesToRadians;

	static private class Sin {
		static final float[] table = new float[SIN_COUNT];

		static {
			for (int i = 0; i < SIN_COUNT; i++)
				table[i] = (float)Math.sin((i + 0.5f) / SIN_COUNT * radFull);
			for (int i = 0; i < 360; i += 90)
				table[(int)(i * degToIndex) & SIN_MASK] = (float)Math.sin(i * degreesToRadians);
		}
	}

	/** Returns the sine in radians from a lookup table. */
	static public float sin (float radians) {
		return Sin.table[(int)(radians * radToIndex) & SIN_MASK];
	}

	/** Returns the cosine in radians from a lookup table. */
	static public float cos (float radians) {
		return Sin.table[(int)((radians + PI / 2) * radToIndex) & SIN_MASK];
	}

	/** Returns the sine in radians from a lookup table. */
	static public float sinDeg (float degrees) {
		return Sin.table[(int)(degrees * degToIndex) & SIN_MASK];
	}

	/** Returns the cosine in radians from a lookup table. */
	static public float cosDeg (float degrees) {
		return Sin.table[(int)((degrees + 90) * degToIndex) & SIN_MASK];
	}

	// ---

	/** Returns atan2 in radians, faster but less accurate than Math.atan2. Average error of 0.00231 radians (0.1323 degrees),
	 * largest error of 0.00488 radians (0.2796 degrees). */
	static public float atan2 (float y, float x) {
		if (x == 0f) {
			if (y > 0f) return PI / 2;
			if (y == 0f) return 0f;
			return -PI / 2;
		}
		final float atan, z = y / x;
		if (Math.abs(z) < 1f) {
			atan = z / (1f + 0.28f * z * z);
			if (x < 0f) return atan + (y < 0f ? -PI : PI);
			return atan;
		}
		atan = PI / 2 - z / (z * z + 0.28f);
		return y < 0f ? atan - PI : atan;
	}

	// ---

	static public Random random = new RandomXS128();

	/** Returns a random number between 0 (inclusive) and the specified value (inclusive). */
	static public int random (int range) {
		return random.nextInt(range + 1);
	}

	/** Returns a random number between start (inclusive) and end (inclusive). */
	static public int random (int start, int end) {
		return start + random.nextInt(end - start + 1);
	}

	/** Returns a random number between 0 (inclusive) and the specified value (inclusive). */
	static public long random (long range) {
		return (long)(random.nextDouble() * range);
	}

	/** Returns a random number between start (inclusive) and end (inclusive). */
	static public long random (long start, long end) {
		return start + (long)(random.nextDouble() * (end - start));
	}

	/** Returns a random boolean value. */
	static public boolean randomBoolean () {
		return random.nextBoolean();
	}

	/** Returns true if a random value between 0 and 1 is less than the specified value. */
	static public boolean randomBoolean (float chance) {
		return MathUtils.random() < chance;
	}

	/** Returns random number between 0.0 (inclusive) and 1.0 (exclusive). */
	static public float random () {
		return random.nextFloat();
	}

	/** Returns a random number between 0 (inclusive) and the specified value (exclusive). */
	static public float random (float range) {
		return random.nextFloat() * range;
	}

	/** Returns a random number between start (inclusive) and end (exclusive). */
	static public float random (float start, float end) {
		return start + random.nextFloat() * (end - start);
	}

	/** Returns -1 or 1, randomly. */
	static public int randomSign () {
		return 1 | (random.nextInt() >> 31);
	}

	/** Returns a triangularly distributed random number between -1.0 (exclusive) and 1.0 (exclusive), where values around zero are
	 * more likely.
	 * <p>
	 * This is an optimized version of {@link #randomTriangular(float, float, float) randomTriangular(-1, 1, 0)} */
	public static float randomTriangular () {
		return random.nextFloat() - random.nextFloat();
	}

	/** Returns a triangularly distributed random number between {@code -max} (exclusive) and {@code max} (exclusive), where values
	 * around zero are more likely.
	 * <p>
	 * This is an optimized version of {@link #randomTriangular(float, float, float) randomTriangular(-max, max, 0)}
	 * @param max the upper limit */
	public static float randomTriangular (float max) {
		return (random.nextFloat() - random.nextFloat()) * max;
	}

	/** Returns a triangularly distributed random number between {@code min} (inclusive) and {@code max} (exclusive), where the
	 * {@code mode} argument defaults to the midpoint between the bounds, giving a symmetric distribution.
	 * <p>
	 * This method is equivalent of {@link #randomTriangular(float, float, float) randomTriangular(min, max, (min + max) * .5f)}
	 * @param min the lower limit
	 * @param max the upper limit */
	public static float randomTriangular (float min, float max) {
		return randomTriangular(min, max, (min + max) * 0.5f);
	}

	/** Returns a triangularly distributed random number between {@code min} (inclusive) and {@code max} (exclusive), where values
	 * around {@code mode} are more likely.
	 * @param min the lower limit
	 * @param max the upper limit
	 * @param mode the point around which the values are more likely */
	public static float randomTriangular (float min, float max, float mode) {
		float u = random.nextFloat();
		float d = max - min;
		if (u <= (mode - min) / d) return min + (float)Math.sqrt(u * d * (mode - min));
		return max - (float)Math.sqrt((1 - u) * d * (max - mode));
	}

	// ---

	/** Returns the next power of two. Returns the specified value if the value is already a power of two. */
	static public int nextPowerOfTwo (int value) {
		if (value == 0) return 1;
		value--;
		value |= value >> 1;
		value |= value >> 2;
		value |= value >> 4;
		value |= value >> 8;
		value |= value >> 16;
		return value + 1;
	}

	static public boolean isPowerOfTwo (int value) {
		return value != 0 && (value & value - 1) == 0;
	}

	// ---

	static public short clamp (short value, short min, short max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public int clamp (int value, int min, int max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public long clamp (long value, long min, long max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public float clamp (float value, float min, float max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	static public double clamp (double value, double min, double max) {
		if (value < min) return min;
		if (value > max) return max;
		return value;
	}

	// ---

	/** Linearly interpolates between fromValue to toValue on progress position. */
	static public float lerp (float fromValue, float toValue, float progress) {
		return fromValue + (toValue - fromValue) * progress;
	}

	/** Linearly interpolates between two angles in radians. Takes into account that angles wrap at two pi and always takes the
	 * direction with the smallest delta angle.
	 * 
	 * @param fromRadians start angle in radians
	 * @param toRadians target angle in radians
	 * @param progress interpolation value in the range [0, 1]
	 * @return the interpolated angle in the range [0, PI2[ */
	public static float lerpAngle (float fromRadians, float toRadians, float progress) {
		float delta = ((toRadians - fromRadians + PI2 + PI) % PI2) - PI;
		return (fromRadians + delta * progress + PI2) % PI2;
	}

	/** Linearly interpolates between two angles in degrees. Takes into account that angles wrap at 360 degrees and always takes
	 * the direction with the smallest delta angle.
	 * 
	 * @param fromDegrees start angle in degrees
	 * @param toDegrees target angle in degrees
	 * @param progress interpolation value in the range [0, 1]
	 * @return the interpolated angle in the range [0, 360[ */
	public static float lerpAngleDeg (float fromDegrees, float toDegrees, float progress) {
		float delta = ((toDegrees - fromDegrees + 360 + 180) % 360) - 180;
		return (fromDegrees + delta * progress + 360) % 360;
	}

	// ---

	static private final int BIG_ENOUGH_INT = 16 * 1024;
	static private final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
	static private final double CEIL = 0.9999999;
	static private final double BIG_ENOUGH_CEIL = 16384.999999999996;
	static private final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5f;

	/** Returns the largest integer less than or equal to the specified float. This method will only properly floor floats from
	 * -(2^14) to (Float.MAX_VALUE - 2^14). */
	static public int floor (float value) {
		return (int)(value + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
	}

	/** Returns the largest integer less than or equal to the specified float. This method will only properly floor floats that are
	 * positive. Note this method simply casts the float to int. */
	static public int floorPositive (float value) {
		return (int)value;
	}

	/** Returns the smallest integer greater than or equal to the specified float. This method will only properly ceil floats from
	 * -(2^14) to (Float.MAX_VALUE - 2^14). */
	static public int ceil (float value) {
		return BIG_ENOUGH_INT - (int)(BIG_ENOUGH_FLOOR - value);
	}

	/** Returns the smallest integer greater than or equal to the specified float. This method will only properly ceil floats that
	 * are positive. */
	static public int ceilPositive (float value) {
		return (int)(value + CEIL);
	}

	/** Returns the closest integer to the specified float. This method will only properly round floats from -(2^14) to
	 * (Float.MAX_VALUE - 2^14). */
	static public int round (float value) {
		return (int)(value + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
	}

	/** Returns the closest integer to the specified float. This method will only properly round floats that are positive. */
	static public int roundPositive (float value) {
		return (int)(value + 0.5f);
	}

	/** Returns true if the value is zero (using the default tolerance as upper bound) */
	static public boolean isZero (float value) {
		return Math.abs(value) <= FLOAT_ROUNDING_ERROR;
	}

	/** Returns true if the value is zero.
	 * @param tolerance represent an upper bound below which the value is considered zero. */
	static public boolean isZero (float value, float tolerance) {
		return Math.abs(value) <= tolerance;
	}

	/** Returns true if a is nearly equal to b. The function uses the default floating error tolerance.
	 * @param a the first value.
	 * @param b the second value. */
	static public boolean isEqual (float a, float b) {
		return Math.abs(a - b) <= FLOAT_ROUNDING_ERROR;
	}

	/** Returns true if a is nearly equal to b.
	 * @param a the first value.
	 * @param b the second value.
	 * @param tolerance represent an upper bound below which the two values are considered equal. */
	static public boolean isEqual (float a, float b, float tolerance) {
		return Math.abs(a - b) <= tolerance;
	}

	/** @return the logarithm of value with base a */
	static public float log (float a, float value) {
		return (float)(Math.log(value) / Math.log(a));
	}

	/** @return the logarithm of value with base 2 */
	static public float log2 (float value) {
		return log(2, value);
	}

	/** This class implements the xorshift128+ algorithm that is a very fast, top-quality 64-bit pseudo-random number generator. The
	 * quality of this PRNG is much higher than {@link Random}'s, and its cycle length is 2<sup>128</sup>&nbsp;&minus;&nbsp;1, which
	 * is more than enough for any single-thread application. More details and algorithms can be found <a
	 * href="http://xorshift.di.unimi.it/">here</a>.
	 * <p>
	 * Instances of RandomXS128 are not thread-safe.
	 *
	 * @author Inferno
	 * @author davebaol */
	public static class RandomXS128 extends Random {

		/** Normalization constant for double. */
		private static final double NORM_DOUBLE = 1.0 / (1L << 53);

		/** Normalization constant for float. */
		private static final double NORM_FLOAT = 1.0 / (1L << 24);

		/** The first half of the internal state of this pseudo-random number generator. */
		private long seed0;

		/** The second half of the internal state of this pseudo-random number generator. */
		private long seed1;

		/** Creates a new random number generator. This constructor sets the seed of the random number generator to a value very likely
		 * to be distinct from any other invocation of this constructor.
		 * <p>
		 * This implementation creates a {@link Random} instance to generate the initial seed. */
		public RandomXS128 () {
			setSeed(new Random().nextLong());
		}

		/** Creates a new random number generator using a single {@code long} seed.
		 * @param seed the initial seed */
		public RandomXS128 (long seed) {
			setSeed(seed);
		}

		/** Creates a new random number generator using two {@code long} seeds.
		 * @param seed0 the first part of the initial seed
		 * @param seed1 the second part of the initial seed */
		public RandomXS128 (long seed0, long seed1) {
			setState(seed0, seed1);
		}

		/** Returns the next pseudo-random, uniformly distributed {@code long} value from this random number generator's sequence.
		 * <p>
		 * Subclasses should override this, as this is used by all other methods. */
		@Override
		public long nextLong () {
			long s1 = this.seed0;
			final long s0 = this.seed1;
			this.seed0 = s0;
			s1 ^= s1 << 23;
			return (this.seed1 = (s1 ^ s0 ^ (s1 >>> 17) ^ (s0 >>> 26))) + s0;
		}

		/** This protected method is final because, contrary to the superclass, it's not used anymore by the other methods. */
		@Override
		protected final int next (int bits) {
			return (int)(nextLong() & ((1L << bits) - 1));
		}

		/** Returns the next pseudo-random, uniformly distributed {@code int} value from this random number generator's sequence.
		 * <p>
		 * This implementation uses {@link #nextLong()} internally. */
		@Override
		public int nextInt () {
			return (int)nextLong();
		}

		/** Returns a pseudo-random, uniformly distributed {@code int} value between 0 (inclusive) and the specified value (exclusive),
		 * drawn from this random number generator's sequence.
		 * <p>
		 * This implementation uses {@link #nextLong()} internally.
		 * @param n the positive bound on the random number to be returned.
		 * @return the next pseudo-random {@code int} value between {@code 0} (inclusive) and {@code n} (exclusive). */
		@Override
		public int nextInt (final int n) {
			return (int)nextLong(n);
		}

		/** Returns a pseudo-random, uniformly distributed {@code long} value between 0 (inclusive) and the specified value (exclusive),
		 * drawn from this random number generator's sequence. The algorithm used to generate the value guarantees that the result is
		 * uniform, provided that the sequence of 64-bit values produced by this generator is.
		 * <p>
		 * This implementation uses {@link #nextLong()} internally.
		 * @param n the positive bound on the random number to be returned.
		 * @return the next pseudo-random {@code long} value between {@code 0} (inclusive) and {@code n} (exclusive). */
		public long nextLong (final long n) {
			if (n <= 0) throw new IllegalArgumentException("n must be positive");
			for (;;) {
				final long bits = nextLong() >>> 1;
				final long value = bits % n;
				if (bits - value + (n - 1) >= 0) return value;
			}
		}

		/** Returns a pseudo-random, uniformly distributed {@code double} value between 0.0 and 1.0 from this random number generator's
		 * sequence.
		 * <p>
		 * This implementation uses {@link #nextLong()} internally. */
		@Override
		public double nextDouble () {
			return (nextLong() >>> 11) * NORM_DOUBLE;
		}

		/** Returns a pseudo-random, uniformly distributed {@code float} value between 0.0 and 1.0 from this random number generator's
		 * sequence.
		 * <p>
		 * This implementation uses {@link #nextLong()} internally. */
		@Override
		public float nextFloat () {
			return (float)((nextLong() >>> 40) * NORM_FLOAT);
		}

		/** Returns a pseudo-random, uniformly distributed {@code boolean } value from this random number generator's sequence.
		 * <p>
		 * This implementation uses {@link #nextLong()} internally. */
		@Override
		public boolean nextBoolean () {
			return (nextLong() & 1) != 0;
		}

		/** Generates random bytes and places them into a user-supplied byte array. The number of random bytes produced is equal to the
		 * length of the byte array.
		 * <p>
		 * This implementation uses {@link #nextLong()} internally. */
		@Override
		public void nextBytes (final byte[] bytes) {
			int n = 0;
			int i = bytes.length;
			while (i != 0) {
				n = i < 8 ? i : 8; // min(i, 8);
				for (long bits = nextLong(); n-- != 0; bits >>= 8)
					bytes[--i] = (byte)bits;
			}
		}

		/** Sets the internal seed of this generator based on the given {@code long} value.
		 * <p>
		 * The given seed is passed twice through a hash function. This way, if the user passes a small value we avoid the short
		 * irregular transient associated with states having a very small number of bits set.
		 * @param seed a nonzero seed for this generator (if zero, the generator will be seeded with {@link Long#MIN_VALUE}). */
		@Override
		public void setSeed (final long seed) {
			long seed0 = murmurHash3(seed == 0 ? Long.MIN_VALUE : seed);
			setState(seed0, murmurHash3(seed0));
		}

		/** Sets the internal state of this generator.
		 * @param seed0 the first part of the internal state
		 * @param seed1 the second part of the internal state */
		public void setState (final long seed0, final long seed1) {
			this.seed0 = seed0;
			this.seed1 = seed1;
		}

		/**
		 * Returns the internal seeds to allow state saving.
		 * @param seed must be 0 or 1, designating which of the 2 long seeds to return
		 * @return the internal seed that can be used in setState
		 */
		public long getState(int seed) {
			return seed == 0 ? seed0 : seed1;
		}

		private final static long murmurHash3 (long x) {
			x ^= x >>> 33;
			x *= 0xff51afd7ed558ccdL;
			x ^= x >>> 33;
			x *= 0xc4ceb9fe1a85ec53L;
			x ^= x >>> 33;

			return x;
		}

	}
}