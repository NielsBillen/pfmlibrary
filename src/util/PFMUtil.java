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
	public static BigDecimal MSE(PFMImage image1, PFMImage image2)
			throws IllegalArgumentException {
		if (image1 == null)
			throw new NullPointerException("the first image is null!");
		if (image2 == null)
			throw new NullPointerException("the second image is null!");
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
		return r.divide(new BigDecimal(resolution), new MathContext(100,
				RoundingMode.HALF_DOWN));
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
