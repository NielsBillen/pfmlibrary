package viewer;

import io.PFMImage;
import io.PFMReader;

import java.io.File;
import java.io.IOException;

import util.PFMUtil;

public class PFMMSE {
	public static void main(String[] args) throws IOException {
		PFMImage i1 = PFMReader.read(new File("pfm/plants-dusk.pfm"));
		PFMImage i2 = PFMReader.read(new File("pfm/plants-dusk-probvis.pfm"));
		System.out.println(PFMUtil.MSE(i1, i2));
	}
}
