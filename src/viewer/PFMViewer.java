package viewer;

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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Viewer for PFM files.
 * 
 * @author Niels Billen
 * @version 1.0
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
		if (args.length == 0) {
			args = new File(".").list(new FilenameFilter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see java.io.FilenameFilter#accept(java.io.File,
				 * java.lang.String)
				 */
				@Override
				public boolean accept(File arg0, String arg1) {
					return arg1.endsWith(".pfm");
				}
			});
		}

		for (String filename : args) {
			try {
				System.out.println(args);
				File file = new File(filename);
				open(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Opens the given PFM file in a seperate JFrame.
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
