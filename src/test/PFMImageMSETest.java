package test;
import io.PFMImage;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Arrays;

import org.junit.Test;

import util.PFMUtil;

/**
 * 
 * @author Niels Billen
 * @version 1.0
 */
public class PFMImageMSETest {
	/*
	 * 
	 */
	@Test
	public void mseTest() {
		int width = 128;
		int height = 128;
		float[] white = new float[3 * width * height];
		float[] black = new float[width * height];
		Arrays.fill(white, 1.f);
		Arrays.fill(black, 0.f);
		PFMImage blackImage = new PFMImage(width, height, black);
		PFMImage whiteImage = new PFMImage(width, height, white);

		System.out.println(PFMUtil.MSE(blackImage, whiteImage));

		float[] sum = new float[3 * width * height];
		BigDecimal d = new BigDecimal(0).setScale(100);
		for (int i = 0; i < 3 * width * height; ++i) {
			sum[i] = i;
			d = d.add(new BigDecimal(i).subtract(new BigDecimal(1)).pow(2));
		}
		d = d.divide(new BigDecimal(width * height), new MathContext(100,
				RoundingMode.DOWN));
		PFMImage sumImage = new PFMImage(width, height, sum);
		System.out.println(PFMUtil.MSE(sumImage, whiteImage));
		System.out.println(d);
	}

}
