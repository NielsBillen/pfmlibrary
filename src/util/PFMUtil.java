package util;

import java.math.BigDecimal;

import io.PFMImage;

/**
 * Utility classes.
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
	public double MSE(PFMImage image1, PFMImage image2)
			throws IllegalArgumentException {
		if (image1.width != image2.width || image1.height != image2.height)
			throw new IllegalArgumentException(
					"the images do not have matching size!");

		BigDecimal decimal = new BigDecimal(0);

		if (image1.gray == image2.gray) {
			for (int i = 0; i < image1.nbOfFloats(); ++i) {
				double d = Math.pow(image1.getFloat(i) - image2.getFloat(i), 2);
				decimal = decimal.add(new BigDecimal(d));
			}
		}

		decimal = decimal.divide(new BigDecimal(image1.width * image1.height));

		return decimal.doubleValue();
	}
}
