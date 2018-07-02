package com.horowitz.commons;

/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is the Jemmy library.
 * The Initial Developer of the Original Software is Alexandre Iline.
 * All Rights Reserved.
 *
 * Contributor(s): Alexandre Iline.
 *
 * $Id: RoughImageComparator.java,v 1.2.192.1 2006/07/01 05:03:48 jtulach Exp $ $Revision: 1.2.192.1 $ $Date: 2006/07/01 05:03:48 $
 *
 */

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Threshold;

/**
 * Compares two images roughly (i.e. not all of the pixel colors should match).
 * 
 * @author Me
 */
public class CrazyImageComparator {
	private double precision;
	private double threshold;
	private boolean bw = false;
	private int maxBWErrors = 5;
	private Color defaultColorToBypass = null;

	/**
	 * Creates a comparator with <code>roughness</code> allowed roughness.
	 * 
	 * @param threshold
	 *            Allowed comparison roughness.
	 * @param precision
	 *            TODO
	 */
	public CrazyImageComparator(double threshold, int precision) {
		this.threshold = threshold;
		this.precision = Math.sqrt(precision * precision * 3);
	}

	public CrazyImageComparator() {
		this(0.97, 20);
	}

	public static final Threshold THRESHOLD = new Threshold(100);

	public BufferedImage toBW(BufferedImage src) {
		FastBitmap fb = new FastBitmap(src);
		if (!fb.isGrayscale())
			fb.toGrayscale();
		THRESHOLD.applyInPlace(fb);
		return fb.toBufferedImage();
	}

	public boolean compare(BufferedImage image1, BufferedImage image2) {
		if (bw) {
			image1 = toBW(image1);
			image2 = toBW(image2);
			return compareBW(image1, image2);
		}

		return compare(image1, image2, (int) ((image1.getWidth() * image1.getHeight()) * (1 - threshold)), precision);
	}

	public boolean compareBW(BufferedImage image1, BufferedImage image2) {
		return compareBW2(image1, image2, (int) ((image1.getWidth() * image1.getHeight()) * (1 - threshold)),
				precision);
	}

	private boolean compareBWOLD(BufferedImage image1, BufferedImage image2, int maxErrors, double allowedDistance) {
		if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
			System.err.println("SIZE DIFFERS\n" + image1.getWidth() + " vs " + image2.getWidth() + "\n"
					+ image1.getHeight() + " vs " + image2.getHeight());
			return false;
		}

		int errors = 0;
		for (int x = 0; x < image1.getWidth(); x++) {
			for (int y = 0; y < image1.getHeight(); y++) {
				int rgb1 = image1.getRGB(x, y);
				int rgb2 = image2.getRGB(x, y);

				if (rgb1 != -1 && rgb1 != rgb2)
					errors++;
				// int diff = Math.abs((rgb1 >> 16 & 0xFF) - (rgb2 >> 16 & 0xFF)) *
				// Math.abs((rgb1 >> 16 & 0xFF) - (rgb2 >> 16 &
				// 0xFF))
				// + Math.abs((rgb1 >> 8 & 0xFF) - (rgb2 >> 8 & 0xFF)) * Math.abs((rgb1 >> 8 &
				// 0xFF) - (rgb2 >> 8 & 0xFF))
				// + Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF)) * Math.abs((rgb1 & 0xFF) - (rgb2 &
				// 0xFF));

				// int r1 = rgb1 >> 16 & 0xFF;// pixels[x*strideX+y*strideY] >> 16 & 0xFF;
				// int g1 = rgb1 >> 8 & 0xFF;
				// int b1 = rgb1 & 0xFF; //rgb1 >> 0 & 0xFF;
				//
				// int r2 = rgb2 >> 16 & 0xFF;// pixels[x*strideX+y*strideY] >> 16 & 0xFF;
				// int g2 = rgb2 >> 8 & 0xFF;
				// int b2 = rgb2 & 0xFF; //rgb1 >> 0 & 0xFF;
				//
				// int r11 = fb1.getRed(y, x);
				// int g11 = fb1.getGreen(y, x);
				// int b11 = fb1.getBlue(y, x);
				// int r22 = fb2.getRed(y, x);
				// int g22 = fb2.getGreen(y, x);
				// int b22 = fb2.getBlue(y, x);
				// if (r1 != r11 || g1 != g11 || b1 != b11) {
				// System.err.println("WHAAAAAAAAAAAAAAAAAAAT?");
				// }
				// if (r2 != r22 || g2 != g22 || b2 != b22) {
				// System.err.println("WHAAAAT?");
				// }
				// System.err.println(r1 + " " + g1 + " " + b1);
				// System.err.println(r2 + " " + g2 + " " + b2);
				// int d1 = (r1 - r2);
				// d1 = d1 * d1;
				// d1 = Math.abs(d1);
				//
				// int d2 = (g1 - g2);
				// d2 = d2 * d2;
				// d2 = Math.abs(d2);
				//
				// int d3 = (b1 - b2);
				// d3 = d3 * d3;
				// d3 = Math.abs(d3);
				//
				// int diff = d1 + d2 + d3;
				// double dsqrt = Math.sqrt(diff);
				// final int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
				// * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
				// + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
				// * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
				// + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF))
				// * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));

				// int diff = Math.abs((((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
				// * (((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)))
				//
				// + Math.abs((((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * (((rgb1 >> 8) &
				// 0xFF) - ((rgb2 >> 8) & 0xFF)))
				//
				// + Math.abs((((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * (((rgb1 >> 0) &
				// 0xFF) - ((rgb2 >> 0) & 0xFF)));

				// if (Math.sqrt(diff) > allowedDistance)
				// if (Math.sqrt(diff) > allowedDistance)
				// errors++;

				if (errors > maxBWErrors)
					// enough
					return false;

				// System.err.println(diff1 + " vs " + diff2);

			}
		}
		return (true);
	}

	private boolean compareBW2(BufferedImage image1, BufferedImage image2, int maxErrors, double allowedDistance) {
		if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
			System.err.println("SIZE DIFFERS\n" + image1.getWidth() + " vs " + image2.getWidth() + "\n"
					+ image1.getHeight() + " vs " + image2.getHeight());
			return false;
		}
		int errors = 0;
		int good = 0;
		int tot = image1.getHeight() * image1.getWidth();
		for (int x = 0; x < image1.getWidth(); x++) {
			for (int y = 0; y < image1.getHeight(); y++) {
				int rgb1 = image1.getRGB(x, y);
				int rgb2 = image2.getRGB(x, y);

				if (rgb1 == rgb2)
					good++;

				if (rgb1 != rgb2)
					errors++;
				// early reject option
				float check = good + errors;
				if (check > (tot * .33)) {
					check = good / check;
					// System.err.println("check: " + check);

					if (check - threshold < 0)
						return false;
				}
				// if ((good + errors) > (tot * 0.33) && (good / (good + errors) < threshold))
				// return false;
			}
		}
		// MyImageIO.writeImageTS(image1, "micro1.png");
		// MyImageIO.writeImageTS(image2, "micro2 " + good + "-" + errors + ".png");

		// System.err.println("hmmmmmmmmmmmmmmm " + good + "-" + errors + "->" + (good /
		// (good + errors)) + " < " +
		// threshold);

		float check = good + errors;
		check = good / check;
		// System.err.println("check: " + check);

		if (check - threshold < 0)
			return false;

		// System.err.println("BINGOOOOOOOOOOOO " + good + "-" + errors + "->" + check +
		// " < " + threshold);
		return true;
	}

	private double evalCompareBW2(BufferedImage image1, BufferedImage image2, int maxErrors, double allowedDistance) {
		if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
			System.err.println("SIZE DIFFERS\n" + image1.getWidth() + " vs " + image2.getWidth() + "\n"
					+ image1.getHeight() + " vs " + image2.getHeight());
			return 0.0;
		}
		int errors = 0;
		int good = 0;
//		int tot = image1.getHeight() * image1.getWidth();
		for (int x = 0; x < image1.getWidth(); x++) {
			for (int y = 0; y < image1.getHeight(); y++) {
				int rgb1 = image1.getRGB(x, y);
				int rgb2 = image2.getRGB(x, y);
				
				if (rgb1 == rgb2)
					good++;
				
				if (rgb1 != rgb2)
					errors++;
				// early reject option
//				float check = good + errors;
//				if (check > (tot * .33)) {
//					check = good / check;
//					// System.err.println("check: " + check);
//					
//					if (check - threshold < 0)
//						return check;
//				}
				// if ((good + errors) > (tot * 0.33) && (good / (good + errors) < threshold))
				// return false;
			}
		}
		// MyImageIO.writeImageTS(image1, "micro1.png");
		// MyImageIO.writeImageTS(image2, "micro2 " + good + "-" + errors + ".png");
		
		// System.err.println("hmmmmmmmmmmmmmmm " + good + "-" + errors + "->" + (good /
		// (good + errors)) + " < " +
		// threshold);
		
		float check = good + errors;
		check = good / check;
		// System.err.println("check: " + check);
		
		if (check - threshold < 0)
			return check;
		
		// System.err.println("BINGOOOOOOOOOOOO " + good + "-" + errors + "->" + check +
		// " < " + threshold);
		return check;
	}
	
	private boolean compare(BufferedImage image1, BufferedImage image2, int maxErrors, double allowedDistance) {
		if (image1.getType() == BufferedImage.TYPE_BYTE_GRAY && image2.getType() == BufferedImage.TYPE_BYTE_GRAY) {
			if (bw)
				return compareBW2(image1, image2, maxErrors, allowedDistance);
			else
				return compareBWOLD(image1, image2, maxErrors, allowedDistance);
		}

		if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
			System.err.println("SIZE DIFFERS\n" + image1.getWidth() + " vs " + image2.getWidth() + "\n"
					+ image1.getHeight() + " vs " + image2.getHeight());
			return false;
		}

		// FastBitmap fb1 = new FastBitmap(image1);
		// FastBitmap fb2 = new FastBitmap(image2);
		// fb1.saveAsBMP("FB1.bmp");
		// fb2.saveAsBMP("FB2.bmp");
		int errors = 0;
		for (int x = 0; x < image1.getWidth(); x++) {
			for (int y = 0; y < image1.getHeight(); y++) {
				int rgb1 = image1.getRGB(x, y);
				int rgb2 = image2.getRGB(x, y);

				int diff = Math.abs((rgb1 >> 16 & 0xFF) - (rgb2 >> 16 & 0xFF))
						* Math.abs((rgb1 >> 16 & 0xFF) - (rgb2 >> 16 & 0xFF))
						+ Math.abs((rgb1 >> 8 & 0xFF) - (rgb2 >> 8 & 0xFF))
								* Math.abs((rgb1 >> 8 & 0xFF) - (rgb2 >> 8 & 0xFF))
						+ Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF)) * Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF));

				// int r1 = rgb1 >> 16 & 0xFF;// pixels[x*strideX+y*strideY] >> 16 & 0xFF;
				// int g1 = rgb1 >> 8 & 0xFF;
				// int b1 = rgb1 & 0xFF; //rgb1 >> 0 & 0xFF;
				//
				// int r2 = rgb2 >> 16 & 0xFF;// pixels[x*strideX+y*strideY] >> 16 & 0xFF;
				// int g2 = rgb2 >> 8 & 0xFF;
				// int b2 = rgb2 & 0xFF; //rgb1 >> 0 & 0xFF;
				//
				// int r11 = fb1.getRed(y, x);
				// int g11 = fb1.getGreen(y, x);
				// int b11 = fb1.getBlue(y, x);
				// int r22 = fb2.getRed(y, x);
				// int g22 = fb2.getGreen(y, x);
				// int b22 = fb2.getBlue(y, x);
				// if (r1 != r11 || g1 != g11 || b1 != b11) {
				// System.err.println("WHAAAAAAAAAAAAAAAAAAAT?");
				// }
				// if (r2 != r22 || g2 != g22 || b2 != b22) {
				// System.err.println("WHAAAAT?");
				// }
				// // System.err.println(r1 + " " + g1 + " " + b1);
				// // System.err.println(r2 + " " + g2 + " " + b2);
				// int d1 = (r1 - r2);
				// d1 = d1 * d1;
				// d1 = Math.abs(d1);
				//
				// int d2 = (g1 - g2);
				// d2 = d2 * d2;
				// d2 = Math.abs(d2);
				//
				// int d3 = (b1 - b2);
				// d3 = d3 * d3;
				// d3 = Math.abs(d3);
				//
				// int diff = d1 + d2 + d3;
				// double dsqrt = Math.sqrt(diff);
				// final int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
				// * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
				// + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
				// * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
				// + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF))
				// * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));

				// int diff = Math.abs((((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
				// * (((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)))
				//
				// + Math.abs((((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * (((rgb1 >> 8) &
				// 0xFF) - ((rgb2 >> 8) & 0xFF)))
				//
				// + Math.abs((((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * (((rgb1 >> 0) &
				// 0xFF) - ((rgb2 >> 0) & 0xFF)));

				// if (Math.sqrt(diff) > allowedDistance)
				if (Math.sqrt(diff) > allowedDistance)
					errors++;
				if (errors > maxErrors)
					// enough
					return false;

				// System.err.println(diff1 + " vs " + diff2);

			}
		}
		return (true);
	}

	private boolean compare(BufferedImage image1, BufferedImage image2, int maxErrors, double allowedDistance,
			Color colorToBypass) {
		if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
			return (false);
		}
		int errors = 0;
		for (int x = 0; x < image1.getWidth(); x++) {
			for (int y = 0; y < image1.getHeight(); y++) {
				int rgb1 = image1.getRGB(x, y);

				if ((rgb1 >> 16 & 0xFF) == colorToBypass.getRed() && (rgb1 >> 8 & 0xFF) == colorToBypass.getGreen()
						&& (rgb1 & 0xFF) == colorToBypass.getBlue()) {
					continue;
				}

				int rgb2 = image2.getRGB(x, y);

				int diff = Math.abs((rgb1 >> 16 & 0xFF) - (rgb2 >> 16 & 0xFF))
						* Math.abs((rgb1 >> 16 & 0xFF) - (rgb2 >> 16 & 0xFF))
						+ Math.abs((rgb1 >> 8 & 0xFF) - (rgb2 >> 8 & 0xFF))
								* Math.abs((rgb1 >> 8 & 0xFF) - (rgb2 >> 8 & 0xFF))
						+ Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF)) * Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF));

				// int diff = Math.abs((((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
				// * (((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)))
				//
				// + Math.abs((((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)) * (((rgb1 >> 8) &
				// 0xFF) - ((rgb2 >> 8) & 0xFF)))
				//
				// + Math.abs((((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)) * (((rgb1 >> 0) &
				// 0xFF) - ((rgb2 >> 0) & 0xFF)));

				if (Math.sqrt(diff) > allowedDistance)
					errors++;
				if (errors > maxErrors)
					// enough
					return false;

				// System.err.println(diff1 + " vs " + diff2);

			}
		}
		return (true);
	}

	private boolean compareQ(BufferedImage image1, BufferedImage image2, int maxErrors, double allowedDistance) {
		if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
			return (false);
		}
		// int maxErrors = (int) ((image1.getWidth() * image1.getHeight()) * (1 -
		// 0.95));
		int errors = 0;
		// int allowedDiff = 10 * 10 * 3;
		int m = 4;
		for (int xOff = 0; xOff < m; xOff++) {
			for (int yOff = 0; yOff < m; yOff++) {

				for (int x = 0 + xOff; x < image1.getWidth(); x += m) {
					for (int y = 0 + yOff; y < image1.getHeight(); y += m) {
						int rgb1 = image1.getRGB(x, y);
						int rgb2 = image2.getRGB(x, y);

						int diff = Math.abs((rgb1 >> 16 & 0xFF) - (rgb2 >> 16 & 0xFF))
								* Math.abs((rgb1 >> 16 & 0xFF) - (rgb2 >> 16 & 0xFF))
								+ Math.abs((rgb1 >> 8 & 0xFF) - (rgb2 >> 8 & 0xFF))
										* Math.abs((rgb1 >> 8 & 0xFF) - (rgb2 >> 8 & 0xFF))
								+ Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF)) * Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF));

						// int diff = Math.abs((((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
						// * (((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)))
						//
						// + Math.abs((((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
						// * (((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)))
						//
						// + Math.abs((((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF))
						// * (((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)));

						if (Math.sqrt(diff) > allowedDistance)
							errors++;
						if (errors > maxErrors)
							// enough errors
							return false;

					} // y
				} // x
			} // yOff
		} // xOff
		return (true);
	}

	private boolean compareQ(BufferedImage image1, BufferedImage image2, int maxErrors, double allowedDistance,
			Color colorToBypass) {
		if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
			return (false);
		}
		// int maxErrors = (int) ((image1.getWidth() * image1.getHeight()) * (1 -
		// 0.95));
		int errors = 0;
		// int allowedDiff = 10 * 10 * 3;
		int m = 4;
		for (int xOff = 0; xOff < m; xOff++) {
			for (int yOff = 0; yOff < m; yOff++) {

				for (int x = 0 + xOff; x < image1.getWidth(); x += m) {
					for (int y = 0 + yOff; y < image1.getHeight(); y += m) {
						int rgb1 = image1.getRGB(x, y);

						if ((rgb1 >> 16 & 0xFF) == colorToBypass.getRed()
								&& (rgb1 >> 8 & 0xFF) == colorToBypass.getGreen()
								&& (rgb1 & 0xFF) == colorToBypass.getBlue()) {
							continue;
						}

						int rgb2 = image2.getRGB(x, y);

						int diff = Math.abs((rgb1 >> 16 & 0xFF) - (rgb2 >> 16 & 0xFF))
								* Math.abs((rgb1 >> 16 & 0xFF) - (rgb2 >> 16 & 0xFF))
								+ Math.abs((rgb1 >> 8 & 0xFF) - (rgb2 >> 8 & 0xFF))
										* Math.abs((rgb1 >> 8 & 0xFF) - (rgb2 >> 8 & 0xFF))
								+ Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF)) * Math.abs((rgb1 & 0xFF) - (rgb2 & 0xFF));

						// int diff = Math.abs((((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
						// * (((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)))
						//
						// + Math.abs((((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
						// * (((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF)))
						//
						// + Math.abs((((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF))
						// * (((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF)));

						if (Math.sqrt(diff) > allowedDistance)
							errors++;
						if (errors > maxErrors)
							// enough errors
							return false;

					} // y
				} // x
			} // yOff
		} // xOff
		return (true);
	}

	public Pixel findImage(BufferedImage image, BufferedImage screen) {
		if (defaultColorToBypass != null)
			return findImage(image, screen, defaultColorToBypass);

		int maxErrors;
		if (bw) {
			if (image.getType() != BufferedImage.TYPE_BYTE_GRAY)
				image = toBW(image);
			if (screen.getType() != BufferedImage.TYPE_BYTE_GRAY)
				screen = toBW(screen);

		}
		if (image.getType() == BufferedImage.TYPE_BYTE_GRAY && screen.getType() == BufferedImage.TYPE_BYTE_GRAY) {
			maxErrors = (int) ((image.getWidth() * image.getHeight()) * (1 - threshold));
			// do not normalize for now
		} else {
			maxErrors = (int) ((image.getWidth() * image.getHeight()) * (1 - threshold));
			image = normalizeRGB(image);
			screen = normalizeRGB(screen);
		}
		for (int i = 0; i <= (screen.getWidth() - image.getWidth()); i++) {
			for (int j = 0; j <= (screen.getHeight() - image.getHeight()); j++) {
				final BufferedImage subimage = screen.getSubimage(i, j, image.getWidth(), image.getHeight());
				if (compare(image, subimage, maxErrors, precision)) {
					Pixel p = new Pixel(i, j);
					return p;
				}
			}
		}
		return null;
	}

	public double evalImageBW(BufferedImage image, BufferedImage screen) {
		int maxErrors;
		if (image.getType() != BufferedImage.TYPE_BYTE_GRAY)
			image = toBW(image);
		if (screen.getType() != BufferedImage.TYPE_BYTE_GRAY)
			screen = toBW(screen);

		if (image.getType() == BufferedImage.TYPE_BYTE_GRAY && screen.getType() == BufferedImage.TYPE_BYTE_GRAY) {
			maxErrors = (int) ((image.getWidth() * image.getHeight()) * (1 - threshold));
			// do not normalize for now
		} else {
			maxErrors = (int) ((image.getWidth() * image.getHeight()) * (1 - threshold));
			image = normalizeRGB(image);
			screen = normalizeRGB(screen);
		}
		
		double bestResult = 0.0;
		
		for (int i = 0; i <= (screen.getWidth() - image.getWidth()); i++) {
			for (int j = 0; j <= (screen.getHeight() - image.getHeight()); j++) {
				final BufferedImage subimage = screen.getSubimage(i, j, image.getWidth(), image.getHeight());
				double eval = evalCompareBW2(image, subimage, maxErrors, precision);
				if (eval > bestResult) {
					bestResult = eval;
				}
			}
		}
	
		return bestResult;
	}

	public BufferedImage normalizeRGB(BufferedImage src) {
		FastBitmap fb = new FastBitmap(src);
		fb.toRGB();
		return fb.toBufferedImage();
		// if (src.getType() != BufferedImage.TYPE_INT_RGB) {
		// return toRGB(src);
		// }
		// return src;
	}

	public BufferedImage toRGB(BufferedImage src) {
		BufferedImage b = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = b.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.dispose();
		return b;
	}

	public Pixel findImage(BufferedImage image, BufferedImage screen, Color colorToBypass) {
		if (colorToBypass == null)
			colorToBypass = defaultColorToBypass;
		if (colorToBypass == null)
			colorToBypass = Color.red;

		image = normalizeRGB(image);
		screen = normalizeRGB(screen);

		int maxErrors = (int) ((image.getWidth() * image.getHeight()) * (1 - threshold));

		for (int i = 0; i <= (screen.getWidth() - image.getWidth()); i++) {
			for (int j = 0; j <= (screen.getHeight() - image.getHeight()); j++) {
				final BufferedImage subimage = screen.getSubimage(i, j, image.getWidth(), image.getHeight());
				if (compare(image, subimage, maxErrors, precision, colorToBypass)) {
					Pixel p = new Pixel(i, j);
					return p;
				}
			}
		}
		return null;
	}

	public Pixel findImageQ(BufferedImage image, BufferedImage screen) {
		if (defaultColorToBypass != null)
			return findImageQ(image, screen, defaultColorToBypass);

		image = normalizeRGB(image);
		screen = normalizeRGB(screen);

		int maxErrors = (int) ((image.getWidth() * image.getHeight()) * (1 - threshold));
		for (int i = 0; i <= (screen.getWidth() - image.getWidth()); i++) {
			for (int j = 0; j <= (screen.getHeight() - image.getHeight()); j++) {
				final BufferedImage subimage = screen.getSubimage(i, j, image.getWidth(), image.getHeight());

				if (compareQ(image, subimage, maxErrors, precision)) {
					Pixel p = new Pixel(i, j);
					return p;
				}
			}
		}
		return null;
	}

	public Pixel findImageQ(BufferedImage image, BufferedImage screen, Color colorToBypass) {
		if (colorToBypass == null)
			colorToBypass = defaultColorToBypass;
		if (colorToBypass == null)
			colorToBypass = Color.red;

		image = normalizeRGB(image);
		screen = normalizeRGB(screen);
		int maxErrors = (int) ((image.getWidth() * image.getHeight()) * (1 - threshold));
		for (int i = 0; i <= (screen.getWidth() - image.getWidth()); i++) {
			for (int j = 0; j <= (screen.getHeight() - image.getHeight()); j++) {
				final BufferedImage subimage = screen.getSubimage(i, j, image.getWidth(), image.getHeight());

				if (compareQ(image, subimage, maxErrors, precision, colorToBypass)) {
					Pixel p = new Pixel(i, j);
					return p;
				}
			}
		}
		return null;
	}

	public Pixel findImageQQ(BufferedImage image, BufferedImage screen, Color colorToBypass) {
		if (colorToBypass == null)
			colorToBypass = defaultColorToBypass;
		if (colorToBypass == null)
			colorToBypass = Color.red;

		image = normalizeRGB(image);
		screen = normalizeRGB(screen);

		int maxErrors = (int) ((image.getWidth() * image.getHeight()) * (1 - threshold));

		int w = (screen.getWidth() - image.getWidth());
		int h = (screen.getHeight() - image.getHeight());
		int m = 4;

		for (int xOff = 0; xOff < m; xOff++) {
			for (int yOff = 0; yOff < m; yOff++) {

				for (int i = 0 + xOff; i <= w; i += m) {
					for (int j = 0 + yOff; j <= h; j += m) {

						final BufferedImage subimage = screen.getSubimage(i, j, image.getWidth(), image.getHeight());
						if (compareQ(image, subimage, maxErrors, precision, colorToBypass)) {
							Pixel p = new Pixel(i, j);
							return p;
						}
					}
				}
			}
		}
		return null;
	}

	public double getPrecision() {
		return precision;
	}

	public void setPrecision(int precision) {
		this.precision = Math.sqrt(precision * precision * 3);
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}

	public void setBW(boolean b) {
		this.bw = b;
	}

	public int getMaxBWErrors() {
		return maxBWErrors;
	}

	public void setMaxBWErrors(int maxBWErrors) {
		this.maxBWErrors = maxBWErrors;
	}

	public Color getDefaultColorToBypass() {
		return defaultColorToBypass;
	}

	public void setDefaultColorToBypass(Color defaultColorToBypass) {
		this.defaultColorToBypass = defaultColorToBypass;
	}

}