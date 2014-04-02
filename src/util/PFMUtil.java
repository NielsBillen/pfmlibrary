package util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import io.PFMImage;

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

		BigDecimal decimal = new BigDecimal(0).setScale(100);

		if (image1.gray == image2.gray) {
			BigDecimal c1, c2, d;
			for (int i = 0; i < image1.nbOfFloats(); ++i) {
				c1 = new BigDecimal(image1.getFloat(i)).setScale(100);
				c2 = new BigDecimal(image2.getFloat(i)).setScale(100);
				d = c1.subtract(c2).pow(2);
				decimal = decimal.add(d);
			}
		} else {
			PFMImage gray = image1.gray ? image1 : image2;
			PFMImage color = image1.gray ? image2 : image1;
			BigDecimal grayColor, colorColor, d;
			for (int i = 0; i < gray.nbOfFloats(); ++i) {
				grayColor = new BigDecimal(gray.getFloat(i)).setScale(100);
				for (int j = 0; j < 3; ++j) {
					colorColor = new BigDecimal(color.getFloat(3 * i + j))
							.setScale(100);
					d = grayColor.subtract(colorColor).pow(2);
					decimal = decimal.add(d);
				}
			}
		}
		int resolution = image1.width * image1.height;
		decimal = decimal.divide(new BigDecimal(resolution, new MathContext(
				100, RoundingMode.HALF_DOWN)));
		return decimal.doubleValue();
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
		if (image1.gray != image2.gray) {
			PFMImage gray = image1.gray ? image1 : image2;
			PFMImage color = image1.gray ? image2 : image1;
			final int nbOfFloats = color.nbOfFloats();
			float[] floats = new float[nbOfFloats];

			for (int i = 0; i < gray.nbOfFloats(); ++i)
				for (int j = 0; j < 3; ++j)
					floats[3 * i + j] = scale
							* Math.abs(gray.getFloat(i)
									- color.getFloat(3 * i + j));
			return new PFMImage(image1.width, image1.height, floats);
		} else {
			final int nbOfFloats = image1.nbOfFloats();
			float[] floats = new float[nbOfFloats];

			for (int i = 0; i < nbOfFloats; ++i)
				floats[i] = scale
						* Math.abs(image1.getFloat(i) - image2.getFloat(i));

			return new PFMImage(image1.width, image1.height, floats);
		}
	}
}
