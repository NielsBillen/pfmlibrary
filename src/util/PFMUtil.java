package util;

import io.PFMImage;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Utility methods for operations on Portable Float Map images.
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
public class PFMUtil {
	private static final BigDecimal THREE = new BigDecimal(3).setScale(100);
	private static final MathContext CONTEXT = new MathContext(100,
			RoundingMode.HALF_DOWN);

	/**
	 * Computes the mean squared error between the two given images.
	 * 
	 * @param image1
	 *            the first image.
	 * @param image2
	 *            the second image.
	 * @throws NullPointerException
	 *             when one of the images is null.
	 * @throws IllegalArgumentException
	 *             when the sizes of the images do not match.
	 * @return the mean squared error between the two given images.
	 */
	public static double MSE(PFMImage image1, PFMImage image2)
			throws IllegalArgumentException {
		if (image1 == null)
			throw new NullPointerException("the first image is null!");
		if (image2 == null)
			throw new NullPointerException("the second image is null!");
		if (image1.width != image2.width || image1.height != image2.height)
			throw new IllegalArgumentException(String.format(
					"the images do not have matching sizes! "
							+ "image1 has size %ix%i; image2 has size %ix%i!",
					image1.width, image1.height, image2.width, image2.height));

		BigDecimal decimal = new BigDecimal(0).setScale(100);

		if (image1.gray == image2.gray) {
			// if the images are both in color/gray, then we can just iterate
			// over the floats
			BigDecimal c1, c2, d;
			for (int i = 0; i < image1.nbOfFloats(); ++i) {
				c1 = new BigDecimal(image1.getFloat(i)).setScale(100);
				c2 = new BigDecimal(image2.getFloat(i)).setScale(100);
				d = c1.subtract(c2).pow(2);
				decimal = decimal.add(d.multiply(THREE));
			}
		} else {
			// if one of the images is gray and the other in color, then we must
			// retrieve the colors and subtract those.
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
		decimal = decimal.divide(new BigDecimal(resolution, CONTEXT));
		return decimal.doubleValue();
	}

	/**
	 * Returns the difference between the two given images.
	 * 
	 * @param image1
	 *            the first image.
	 * @param image2
	 *            the second image.
	 * @throws NullPointerException
	 *             when one of the images is null.
	 * @throws IllegalArgumentException
	 *             when the sizes of the images do not match.
	 * @return the mean squared error between the two given images.
	 */
	public static PFMImage difference(PFMImage image1, PFMImage image2) {
		return difference(image1, image2, 1);
	}

	/**
	 * Returns the difference between the two given images scaled by the given
	 * amount.
	 * 
	 * @param image1
	 *            the first image.
	 * @param image2
	 *            the second image.
	 * @param scale
	 *            the scale used to scale the difference.
	 * @throws NullPointerException
	 *             when one of the images is null.
	 * @throws IllegalArgumentException
	 *             when the sizes of the images do not match.
	 * @return the mean squared error between the two given images.
	 */
	public static PFMImage difference(PFMImage image1, PFMImage image2,
			float scale) {
		if (image1 == null)
			throw new NullPointerException("the first image is null!");
		if (image2 == null)
			throw new NullPointerException("the second image is null!");
		if (image1.width != image2.width || image1.height != image2.height)
			throw new IllegalArgumentException(String.format(
					"the images do not have matching sizes! "
							+ "image1 has size %ix%i; image2 has size %ix%i!",
					image1.width, image1.height, image2.width, image2.height));
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
