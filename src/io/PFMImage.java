package io;

import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * The PFMImage describes an image in the Portable Float Map format.
 * 
 * @author Niels Billen
 * @version 1.0
 * 
 *          Redistribution and use in source and binary forms, with or without
 *          modification, are permitted provided that the following conditions
 *          are met:
 * 
 *          - Redistributions of source code must retain the above copyright
 *          notice, this list of conditions and the following disclaimer.
 * 
 *          - Redistributions in binary form must reproduce the above copyright
 *          notice, this list of conditions and the following disclaimer in the
 *          documentation and/or other materials provided with the distribution.
 * 
 *          THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *          "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *          LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 *          FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 *          COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 *          INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 *          BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *          LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 *          CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 *          LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *          ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *          POSSIBILITY OF SUCH DAMAGE.
 */
public class PFMImage {
	private final float[] floats;
	public final int width;
	public final int height;

	/**
	 * Creates a new image with the given dimensions using the given array of
	 * floats.
	 * 
	 * Let r = width*height, then the length of the float array can either be
	 * equal to r or 3*r.
	 * 
	 * @param width
	 *            The width of the image.
	 * @param height
	 *            The height of the image.
	 * @param floats
	 *            The floats to construct the image.
	 * @throws IllegalArgumentException
	 *             When the width or height are smaller than or equal to zero.
	 * @throws IllegalArgumentException
	 *             When the length of the float array is not equal to
	 *             width*height or 3*width*height.
	 * @throws NullPointerException
	 *             When the given float array is null.
	 */
	public PFMImage(int width, int height, float[] floats)
			throws IllegalArgumentException, NullPointerException {
		if (width <= 0)
			throw new IllegalArgumentException(
					"the width has to be larger than zero!");
		if (height <= 0)
			throw new IllegalArgumentException(
					"the height has to be larger than zero!");
		if (floats == null)
			throw new NullPointerException("the given float array is null!");

		int res = width * height;

		if (res != floats.length && 3 * res != floats.length)
			throw new IllegalArgumentException(String.format(
					"the number of floats must match the resolution of "
							+ "the image! the number of given floats is"
							+ " %i, but should be %i for a gray image "
							+ "or %i for a color image!", floats.length, res,
					3 * res));

		this.width = width;
		this.height = height;
		this.floats = Arrays.copyOf(floats, floats.length);
	}

	/**
	 * Returns the i'th float of the image.
	 * 
	 * @param i
	 *            the index of the float we wish to access.
	 * @throws ArrayIndexOutOfBoundsException
	 *             when the given index is out of bounds.
	 * @return the i'th float of the image.
	 */
	public float getFloat(int i) throws ArrayIndexOutOfBoundsException {
		return floats[i];
	}

	/**
	 * Sets the i'th float of the image.
	 * 
	 * @param i
	 *            index of the float we wish to set.
	 * @param value
	 *            the value for the float.
	 * @throws ArrayIndexOutOfBoundsException
	 *             when the given index is out of bounds.
	 */
	public void setFloat(int i, float value)
			throws ArrayIndexOutOfBoundsException {
		floats[i] = value;
	}

	/**
	 * Returns the number of floats in this image.
	 * 
	 * @return the number of floats in this image.
	 */
	public int nbOfFloats() {
		return floats.length;
	}

	/**
	 * Returns whether this image is a gray scale image.
	 * 
	 * @return whether this image is a gray scale image.
	 */
	public boolean isGrayScale() {
		return nbOfFloats() == width * height;
	}

	/**
	 * Returns whether this image is a color image.
	 * 
	 * @return whether this image is a color image.
	 */
	public boolean isColor() {
		return nbOfFloats() == 3 * width * height;
	}

	/**
	 * Returns the color at the given position as a float array.
	 * 
	 * @param x
	 *            x position in the image.
	 * @param y
	 *            y position in the image.
	 * @throws IllegalArgumentException
	 *             when the given pixel coordinates are out of range.
	 * @return an array consisting of the red, green and blue color channel at
	 *         the specified pixel in the image.
	 */
	public float[] getColorAt(int x, int y) {
		if (isGrayScale()) {
			final int o = y * width + x;
			final float c = getFloat(o);
			return new float[] { c, c, c };
		} else {
			final int o = 3 * (y * width + x);
			return new float[] { getFloat(o), getFloat(o + 1), getFloat(o + 2) };
		}
	}

	/**
	 * Converts this Portable Float Map image to a BufferedImage.
	 * 
	 * @param gamma
	 *            The gamma correction factor.
	 * @return a Buffered Image representation of this image.
	 */
	public BufferedImage toBufferedImage(double gamma) {
		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		int[] rgba = new int[] { 0, 0, 0, 255 };

		for (int i = 0; i < width * height; ++i) {
			if (isGrayScale()) {
				rgba[0] = toInt(getFloat(i), gamma);
				rgba[1] = rgba[0];
				rgba[2] = rgba[0];
				result.getRaster().setPixel(i % width, height - 1 - i / width,
						rgba);
			} else {
				rgba[0] = toInt(getFloat(3 * i), gamma);
				rgba[1] = toInt(getFloat(3 * i + 1), gamma);
				rgba[2] = toInt(getFloat(3 * i + 2), gamma);
				result.getRaster().setPixel(i % width, height - 1 - i / width,
						rgba);
			}
		}

		return result;
	}

	/**
	 * Converts this Portable Float Map image to a BufferedImage where all the
	 * floats in the image are first scaled to values between [0,1].
	 * 
	 * @param gamma
	 *            The gamma correction factor.
	 * @return a Buffered Image representation of this image.
	 */
	public BufferedImage toScaledBufferedImage(double gamma) {
		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		int[] rgba = new int[] { 0, 0, 0, 255 };

		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < width * height; ++i) {
			floats[i] = (float) Math.pow(floats[i], gamma);
			if (floats[i] < min)
				min = floats[i];
			if (floats[i] > max)
				max = floats[i];
		}

		float[] f = new float[nbOfFloats()];
		double inv_range = 1.0 / (max - min);
		for (int i = 0; i < width * height; ++i)
			f[i] = (float) ((floats[i] - min) * inv_range);

		for (int i = 0; i < width * height; ++i) {
			if (isGrayScale()) {
				rgba[0] = clamp((int) (255.f * floats[i]), 0, 255);
				rgba[1] = rgba[0];
				rgba[2] = rgba[0];
				result.getRaster().setPixel(i % width, height - 1 - i / width,
						rgba);
			} else {
				rgba[0] = clamp((int) (255.f * f[3 * i]), 0, 255);
				rgba[1] = clamp((int) (255.f * f[3 * i + 1]), 0, 255);
				rgba[2] = clamp((int) (255.f * f[3 * i + 2]), 0, 255);
				result.getRaster().setPixel(i % width, height - 1 - i / width,
						rgba);
			}
		}

		return result;
	}

	/**
	 * Clamps the given value between the given minimum and maximum.
	 * 
	 * @param value
	 *            the value to clamp.
	 * @param min
	 *            the minimum.
	 * @param max
	 *            the maximum.
	 * @return the given value clamped between to the interval [min,max].
	 */
	public static int clamp(int value, int min, int max) {
		if (value < min)
			return min;
		else if (value > max)
			return max;
		else
			return value;
	}

	/**
	 * Returns a gamma corrected float within a valid RGB range.
	 * 
	 * @param f
	 *            The float within the range [-Infinity,Infinity]
	 * @param gamma
	 *            The inverse of the gamma. (I pass the inverse to avoid
	 *            repeated divisions).
	 * @return the gamma corrected float within the range [0,255].
	 */
	public static int toInt(double f, double gamma) {
		if (f < 0.0)
			return 0;
		else if (f > 1.0)
			return 255;
		else
			return clamp((int) (255.0 * Math.pow(f, 1.0 / gamma)), 0, 255);
	}
}