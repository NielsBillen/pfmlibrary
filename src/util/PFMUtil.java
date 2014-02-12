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
		} else {
			PFMImage grayImage = image1.gray ? image1 : image2;
			PFMImage colorImage = image1.gray ? image2 : image1;

			for (int i = 0; i < grayImage.nbOfFloats(); ++i) {
				BigDecimal c1 = new BigDecimal(grayImage.getFloat(i))
						.setScale(100);
				for (int j = 0; j < 3; ++j) {
					BigDecimal c2 = new BigDecimal(colorImage.getFloat(3 * i
							+ j));
					BigDecimal d = c1.subtract(c2).pow(2);
					decimal = decimal.add(d);
				}
			}
		}

		decimal = decimal.divide(new BigDecimal(image1.width * image1.height),new MathContext(100, RoundingMode.HALF_DOWN));

		return decimal.doubleValue();
	}
}
