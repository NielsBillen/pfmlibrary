package io;

import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Represents a PFM image.
 * 
 * @author Niels Billen
 * @version 1.0
 */
public class PFMImage {
	private final float[] floats;
	public final int width;
	public final int height;
	public final boolean gray;

	/**
	 * 
	 * @param width
	 * @param height
	 * @param floats
	 */
	public PFMImage(int width, int height, float[] floats)
			throws IllegalArgumentException {
		if (width <= 0)
			throw new IllegalArgumentException(
					"the width has to be larger than zero!");
		if (height <= 0)
			throw new IllegalArgumentException(
					"the height has to be larger than zero!");

		int resolution = width * height;
		if (resolution != floats.length && 3 * resolution != floats.length)
			throw new IllegalArgumentException(
					"the number of floats must match the resolution of the image! the number of given floats is "
							+ floats.length
							+ " and should be "
							+ resolution
							+ " for a gray image or "
							+ (3 * resolution)
							+ " for a color image!");


		this.width = width;
		this.height = height;
		this.gray = floats.length == resolution;

		// if (floats.length == resolution) {
		// this.floats = new float[3 * resolution];
		// for (int i = 0; i < resolution; ++i)
		// for (int j = 0; j < 3; ++j)
		// this.floats[3 * i + j] = floats[i];
		// } else {
		this.floats = Arrays.copyOf(floats, floats.length);
		// }
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public float getFloat(int i) {
		return floats[i];
	}

	/**
	 * 
	 * @param i
	 * @return
	 */
	public void setFloat(int i, float value) {
		floats[i] = value;
	}

	/**
	 * 
	 * @return
	 */
	public int nbOfFloats() {
		return floats.length;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isGrayScale() {
		return nbOfFloats() == width * height;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isColor() {
		return nbOfFloats() == 3 * width * height;
	}

	/**
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public float[] getColorAt(int x, int y) {
		if (gray) {
			int o = y * width + x;
			float c = getFloat(o);
			return new float[] { c, c, c };
		} else {
			int o = 3 * (y * width + x);
			return new float[] { getFloat(o), getFloat(o + 1), getFloat(o + 2) };
		}
	}

	/**
	 * Converts this PFM image to a BufferedImage.
	 * 
	 * @param gamma
	 *            The gamma correction factor (default 2.2).
	 * @return a buffered image.
	 */
	public BufferedImage toBufferedImage(double gamma) {
		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		double inv_gamma = 1.0 / gamma;
		int[] rgba = new int[] { 0, 0, 0, 255 };

		for (int i = 0; i < width * height; ++i) {
			if (gray) {
				rgba[0] = toInt(floats[i], inv_gamma);
				rgba[1] = rgba[0];
				rgba[2] = rgba[0];
				result.getRaster().setPixel(i % width, height - 1 - i / width,
						rgba);
			} else {
				rgba[0] = toInt(getFloat(3 * i), inv_gamma);
				rgba[1] = toInt(getFloat(3 * i + 1), inv_gamma);
				rgba[2] = toInt(getFloat(3 * i + 2), inv_gamma);
				result.getRaster().setPixel(i % width, height - 1 - i / width,
						rgba);
			}
			for (int j = 0; j < 3; ++j)
				if (rgba[j] < 0 || rgba[j] > 255)
					throw new IllegalStateException();
		}

		return result;
	}

	/**
	 * Returns a gamma corrected float within a valid RGB range.
	 * 
	 * @param f
	 *            The float within the range [-Infinity,Infinity]
	 * @param inv_gamma
	 *            The inverse of the gamma. (I pass the inverse to avoid
	 *            repeated divisions).
	 * @return the gamma corrected float within the range [0,255].
	 */
	public static int toInt(double f, double inv_gamma) {
		if (f < 0.0)
			return 0;
		else if (f > 1.0)
			return 255;
		else
			return Math.min(255,
					Math.max(0, (int) (255.0 * Math.pow(f, inv_gamma))));
	}
}
