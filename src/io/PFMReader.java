package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of a class capable of reading PFM files.
 * 
 * @author Niels Billen
 * @version 1.0
 */
public class PFMReader {
	/**
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static PFMImage read(File file) throws IOException {
		FileInputStream reader = new FileInputStream(file);

		int lines = 0;

		/*****************************************************
		 * Read the header
		 *****************************************************/
		String[] header = new String[] { "", "", "" };

		while (lines < 3) {
			char c = (char) reader.read();
			if (c == '\n')
				++lines;
			else
				header[lines] += c;
		}

		/*****************************************************
		 * Parse the header
		 *****************************************************/

		int type = -1;
		if (header[0].contains("Pf"))
			type = 0;
		else if (header[0].contains("PF"))
			type = 1;

		if (type < 0) {
			reader.close();
			throw new IllegalArgumentException(
					"header does not contain a valid PFM format!");
		}

		int width = -1, height = -1;
		String[] dimension = header[1].split(" ");
		try {
			width = Integer.parseInt(dimension[0]);
			height = Integer.parseInt(dimension[1]);
		} catch (NumberFormatException e) {
			reader.close();
			throw new IllegalArgumentException(
					"header does not contain a valid size!");
		} catch (ArrayIndexOutOfBoundsException e) {
			reader.close();
			throw new IllegalArgumentException(
					"header does not contain a valid size!");
		}
		if (width < 0 || height < 0) {
			reader.close();
			throw new IllegalArgumentException(
					"header does not contain a valid size!");
		}
		float scale = -1;
		try {
			scale = Float.parseFloat(header[2]);
		} catch (NumberFormatException e) {
			reader.close();
			throw new IllegalArgumentException(
					"header does not contain a valid scale!");
		}
		float inv_scale = 1.f / Math.abs(scale);
		boolean littleEndian = scale < 0;

		/********************************************************
		 * Read the data
		 ********************************************************/
		int samples = (type == 0 ? 1 : 3);
		int size = width * height * 4 * samples;
		byte[] bytes = new byte[size];
		int read, offset = 0;

		while ((read = reader.read(bytes, offset, size - offset)) > 0)
			offset += read;

		reader.close();

		ByteBuffer buffer = ByteBuffer.wrap(bytes).order(
				littleEndian ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
		float[] floats = new float[samples * width * height];
		for (int i = 0; i < samples * width * height; ++i)
			floats[i] = buffer.getFloat() * inv_scale;

		return new PFMImage(width, height, floats);
	}
}
