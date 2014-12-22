package viewer;

import io.PFMImage;
import io.PFMReader;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A viewer for Portable Float Images.
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
public class PFMViewer {
	private static int openFrames = 0;
	private static final Semaphore sem = new Semaphore(1);

	/**
	 * Opens the PFM files given in the arguments.
	 * 
	 * @param args
	 *            Arguments for the program.
	 * @throws IOException
	 */
	public static void main(String[] args) {
		// PFMImage first = PFMReader.read(new File("tree-scene-regular.pfm"));
		// PFMImage second = PFMReader.read(new File("tree-scene-shadow.pfm"));
		// PFMImage difference = PFMUtil.difference(first, second);
		// ImageIO.write(difference.toBufferedImage(2.2), "png", new File(
		// "difference"));

		if (args.length == 0) {
			System.out
					.println("usage: --gamma <double> --open <files> --convert <files> --r <directory>");
			System.out.println(" --open    : open following .pfm files");
			System.out
					.println(" --convert : convert following .pfm files to .png files");
			System.out
					.println(" --r       : recursive traversal in a directory.");
			System.out
					.println("             all .pfm files will be opened or converted");
			System.out.println(" --gamma   : gamma correction");
			return;
		}

		double gamma = 2.2;
		int status = 0;
		boolean recursive = false;
		for (int i = 0; i < args.length; ++i) {
			try {
				if (args[i].equals("--open"))
					status = 0;
				else if (args[i].equals("--convert"))
					status = 1;
				else if (args[i].equals("--r") || args[i].equals("-r"))
					recursive = true;
				else if (args[i].equals("--gamma")) {
					try {
						gamma = Double.parseDouble(args[i + 1]);
						++i;
					} catch (NumberFormatException e) {
						System.out.println();

					}
				} else {
					File file = new File(args[i]);
					if (!file.exists())
						continue;
					List<File> files;
					if (recursive)
						files = recursive(file);
					else {
						files = new ArrayList<File>();
						files.add(file);
					}

					for (File f : files) {
						if (status == 0) {
							open(f);
						} else if (status == 1) {
							PFMImage image = PFMReader.read(f);
							BufferedImage buf = image.toBufferedImage(gamma);
							ImageIO.write(buf, "png", new File(f
									.getAbsolutePath().replace(".pfm", ".png")));
							System.out.println("converted " + f.getName()
									+ " to png...");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Returns all Portable Float Map images in the given directory.
	 * 
	 * @param file
	 *            the file to retrieve all the Portable Float Map images from.
	 * @return if the given file is a Portable Float Map image, then a list with
	 *         the given file will be returned. If the given file is a
	 *         directory, then the directory will be traversed recursively until
	 *         all the Portable Float Map images are found.
	 */
	private static List<File> recursive(File file) {
		List<File> result = new ArrayList<File>();
		if (file.isFile() && file.getName().endsWith(".pfm"))
			result.add(file);
		else {
			File[] files = file.listFiles();
			for (File f : files) {
				if (f.isDirectory())
					result.addAll(recursive(f));
				else if (f.getName().endsWith(".pfm"))
					result.add(f);
			}
		}

		return result;
	}

	/**
	 * Opens the given PFM file in a separate JFrame.
	 * 
	 * @param file
	 *            The PFM file to display.
	 * @throws IOException
	 */
	private synchronized static void open(final File file) throws IOException {
		final BufferedImage image = PFMReader.read(file).toBufferedImage(2.2);

		// Create the frame.
		final JFrame frame = new JFrame("PFMViewer: " + file.getName());

		// Create the panel for the frame.
		final JPanel panel = new JPanel() {
			private static final long serialVersionUID = -419497003699361974L;

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.JComponent#getPreferredSize()
			 */
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(image.getWidth(), image.getHeight());
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.swing.JComponent#paint(java.awt.Graphics)
			 */
			@Override
			public void paint(Graphics g) {
				Rectangle r = g.getClipBounds();
				int x = (r.width - image.getWidth()) / 2;
				int y = (r.height - image.getHeight()) / 2;
				g.drawImage(image, x, y, null);
			}
		};

		// Add the panel to the frame.
		frame.add(panel);
		frame.pack();

		// Center the JFrame on the screen.
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		Rectangle screen = gs[0].getDefaultConfiguration().getBounds();
		Dimension fsize = frame.getSize();
		frame.setLocation((screen.width - fsize.width) / 2,
				(screen.height - fsize.height) / 2);

		// Open the frame and increment the number of open frames.
		frame.setVisible(true);
		incrementFrames();

		// Listen to when a window is closed.
		frame.addWindowListener(new WindowAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * java.awt.event.WindowAdapter#windowClosing(java.awt.event.WindowEvent
			 * )
			 */
			@Override
			public void windowClosing(WindowEvent arg0) {
				decrementFrames();
			}
		});
	}

	/**
	 * Increment the number of open frames.
	 * 
	 * This method is threadsafe.
	 */
	private synchronized static void incrementFrames() {
		while (true) {
			try {
				sem.acquire();
				break;
			} catch (InterruptedException e) {
			}
		}
		++openFrames;
		sem.release();
	}

	/**
	 * Decrement the number of open frames and exit when there are no open
	 * frames left.
	 * 
	 * This method is threadsafe.
	 */
	private synchronized static void decrementFrames() {
		while (true) {
			try {
				sem.acquire();
				break;
			} catch (InterruptedException e) {
			}
		}
		--openFrames;
		if (openFrames == 0)
			System.exit(0);
		sem.release();
	}
}
