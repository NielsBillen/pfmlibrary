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
	public PFMImage(int width, int height, float[] floats) {
		this.width = width;
		this.height = height;
		this.floats = Arrays.copyOf(floats, floats.length);
		this.gray = floats.length == width * height;
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
	 * Converts this PFM image to a BufferedImage.
	 * 
	 * @param gamma
	 *            The gamma correction factor (default 2.2).
	 * @return a buffered image.
	 */
	public BufferedImage toBufferedImage(double gamma) {

		BufferedImage result = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_ARGB);

		double inv_gamma =  1.0/gamma;
		double[] rgba = new double[] { 0, 0, 0, 255 };

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
	public static float toInt(double f, double inv_gamma) {
		if (f < 0)
			return 0.f;
		else if (f > 1)
			return 255.f;
		else
			return (float) (255.f * Math.pow(f, inv_gamma));
	}
}