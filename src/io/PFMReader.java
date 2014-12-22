package io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of a class capable of reading Portable Float Map images.
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
public class PFMReader {
	/**
	 * Reads a Portable Float Map from the file specified by the given filename.
	 * 
	 * @param filename
	 *            name of the file to read the Portable Float Map from.
	 * @throws IOException
	 *             when an exception occurs during the reading of the file.
	 * @return an object containing the Portable Float Map image.
	 */
	public static PFMImage read(String filename) throws IOException {
		return read(new File(filename));
	}

	/**
	 * Reads a Portable Float Map from the given file.
	 * 
	 * @param file
	 *            file to read the Portable Float Map from.
	 * @throws IOException
	 *             when an exception occurs during the reading of the file.
	 * @return an object containing the Portable Float Map image.
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
