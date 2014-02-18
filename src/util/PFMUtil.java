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
					"the images do not have matching size!");

		BigDecimal decimal = new BigDecimal(0).setScale(100);

		if (image1.gray == image2.gray) {
			for (int i = 0; i < image1.nbOfFloats(); ++i) {
				BigDecimal c1 = new BigDecimal(image1.getFloat(i))
						.setScale(100);
				BigDecimal c2 = new BigDecimal(image2.getFloat(i))
						.setScale(100);
				BigDecimal d = c1.subtract(c2).pow(2);
				decimal = decimal.add(d);
			}
			decimal = decimal.divide(new BigDecimal(image1.nbOfFloats()),
					new MathContext(100, RoundingMode.HALF_DOWN));
		} else {
			throw new IllegalArgumentException();
			// PFMImage grayImage = image1.gray ? image1 : image2;
			// PFMImage colorImage = image1.gray ? image2 : image1;
			//
			// for (int i = 0; i < grayImage.nbOfFloats(); ++i) {
			// BigDecimal c1 = new BigDecimal(grayImage.getFloat(i))
			// .setScale(100);
			// for (int j = 0; j < 3; ++j) {
			// BigDecimal c2 = new BigDecimal(colorImage.getFloat(3 * i
			// + j));
			// BigDecimal d = c1.subtract(c2).pow(2);
			// decimal = decimal.add(d);
			// }
			// }
			// decimal = decimal.divide(new BigDecimal(3.f * image1.width
			// * image1.height), new MathContext(100,
			// RoundingMode.HALF_DOWN));
		}

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
			// final PFMImage gray = image1.gray ? image1 : image2;
			// final PFMImage color = image1.gray ? image2 : image1;
			// final int nbOfFloats = color.nbOfFloats();
			// float[] floats = new float[nbOfFloats];
			//
			// for (int i = 0; i < nbOfFloats; ++i) {
			// float g = gray.getFloat(i / 3);
			// floats[i] = Math.abs(color.getFloat(i) - g);
			// }
			// return new PFMImage(image1.width, image2.height, floats);
			throw new IllegalArgumentException();
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
