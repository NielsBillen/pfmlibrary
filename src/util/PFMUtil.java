package util;

import io.PFMImage;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utility classes for operations on PFM images.
 * 
 * @author Niels Billen
 * @version 1.0
 */
public class PFMUtil {
	/**
	 * 
	 * @param image1
	 * @param image2
	 * @return
	 */
	public static double MSE(PFMImage image1, PFMImage image2)
			throws IllegalArgumentException {
		if (image1.width != image2.width || image1.height != image2.height)
			throw new IllegalArgumentException(
					"the images do not have matching size!" + image1.width
							+ "x" + image1.height + " vs " + image2.width + "x"
							+ image2.height);
		BigDecimal t, bc1, bc2;
		BigDecimal r = new BigDecimal(0).setScale(100);
		float[] c1, c2;
		for (int y = 0; y < image1.height; ++y) {
			for (int x = 0; x < image1.width; ++x) {
				c1 = image1.getColorAt(x, y);
				c2 = image2.getColorAt(x, y);

				for (int i = 0; i < 3; ++i) {
					bc1 = new BigDecimal(c1[i]).setScale(100);
					bc2 = new BigDecimal(c2[i]).setScale(100);
					t = bc1.subtract(bc2).pow(2);
					r = r.add(t);
				}
			}
		}

		int resolution = image1.width * image1.height;
		r = r.divide(new BigDecimal(resolution, new MathContext(100,
				RoundingMode.HALF_DOWN)));
		return r.doubleValue();
	}

	/**
	 * 
	 * @param image1
	 * @param image2
	 * @return
	 */
	public static PFMImage difference(PFMImage image1, PFMImage image2) {
		return difference(image1, image2, 1);
	}

	/**
	 * 
	 * @param image1
	 * @param image2
	 * @return
	 */
	public static PFMImage difference(PFMImage image1, PFMImage image2,
			int scale) {
		if (image1.width != image2.width || image1.height != image2.height)
			throw new IllegalArgumentException(
					"the images do not have matching size!");

		final int resolution = image1.width * image1.height;
		final int nbOfFloats = 3 * resolution;
		float[] floats = new float[nbOfFloats];
		float[] c1, c2;

		for (int y = 0; y < image1.height; ++y)
			for (int x = 0; x < image1.width; ++x) {
				c1 = image1.getColorAt(x, y);
				c2 = image2.getColorAt(x, y);
				int index = 3 * (image1.height * y + x);

				for (int i = 0; i < 3; ++i)
					floats[index + i] = scale * Math.abs(c1[i] - c2[i]);
			}

		return new PFMImage(image1.width, image1.height, floats);
	}
}
