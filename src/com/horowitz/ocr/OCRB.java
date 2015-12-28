package com.horowitz.ocr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.ColorFiltering;

import com.horowitz.commons.ImageComparator;
import com.horowitz.commons.ImageManager;
import com.horowitz.commons.MyImageIO;
import com.horowitz.commons.Pixel;
import com.horowitz.commons.SimilarityImageComparator;

public class OCRB {

	private ImageComparator _comparator;

	private List<BufferedImage> _digits;
	private int _minWidth;
	private int _maxWidth;
	private int _maxHeight;

	public void setErrors(int err) {
		_comparator.setErrors(err);
	}

	public OCRB(String prefix) throws IOException {
		this(prefix, new SimilarityImageComparator(0.04, 2000));
	}

	public OCRB(String prefix, ImageComparator comparator) throws IOException {
		_comparator = comparator;
		_comparator.setErrors(1);
		_digits = new ArrayList<BufferedImage>(10);

		for (int i = 0; i < 10; i++) {
			_digits.add(ImageIO.read(ImageManager.getImageURL(prefix + i + ".bmp")));
		}
		try {
			_digits.add(ImageIO.read(ImageManager.getImageURL(prefix + "slash" + ".bmp")));
		} catch (Exception e) {
		}
		_minWidth = Integer.MAX_VALUE;
		_maxWidth = 0;
		_maxHeight = 0;
		for (BufferedImage bi : _digits) {
			int w = bi.getWidth();
			int h = bi.getHeight();
			if (w > _maxWidth)
				_maxWidth = w;
			if (w < _minWidth)
				_minWidth = w;
			if (h > _maxHeight)
				_maxHeight = h;
		}

	}

	private void writeImage(BufferedImage image, int n) {
		if (false)
			try {
				MyImageIO.write(image, "PNG", new File("subimage" + n + ".png"));
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

	public String scanImage(BufferedImage image) {
		BufferedImage subimage = image.getSubimage(0, 0, image.getWidth(), image.getHeight());
		writeImage(subimage, 1);
		// subimage = cutEdges(subimage, _foreground);
		// writeImage(subimage, 2);
		BufferedImage subimage2 = subimage.getSubimage(0, 0, subimage.getWidth(), subimage.getHeight());
		String result = "";

		int w = _maxWidth;
		int wmin = _minWidth;
		// int h = masks.getMaxHeight();

		while (subimage.getWidth() >= wmin) {
			// we have space to work
			int ww = w;
			if (subimage.getWidth() < w) {
				ww = subimage.getWidth();
			}
			subimage2 = subimage.getSubimage(0, 0, ww, subimage.getHeight());
			writeImage(subimage2, 101);

			List<Integer> found = new ArrayList<Integer>();
			for (int i = 0; i < _digits.size(); i++) {
				BufferedImage bi = _digits.get(i);
				Pixel p = _comparator.findImage(bi, subimage2, Color.RED);
				if (p != null) {
					found.add(i);
				}
				if (found.size() > 1) {
					// not good
					break;
				}
			}

			if (found.size() == 1) {
				// yahoooo
				Integer m = found.get(0);
				result += ("" + (m < 10 ? m : "/"));
				// cut the chunk and move forward
				if (subimage.getWidth() - _digits.get(m).getWidth() <= 0) {
					// it's over
					break;
				}
				subimage = subimage.getSubimage(0 + _digits.get(m).getWidth(), 0, subimage.getWidth()
				    - _digits.get(m).getWidth(), subimage.getHeight());
				writeImage(subimage, 102);
			} else if (found.isEmpty()) {
				int howMuchToTheRight = 1; // or w
				if (subimage.getWidth() - howMuchToTheRight >= wmin) {
					subimage = subimage.getSubimage(0 + howMuchToTheRight, 0, subimage.getWidth() - howMuchToTheRight,
					    subimage.getHeight());
					writeImage(subimage, 103);
				} else {
					// we're done
					break;
				}
			} else {

				// SKIP FOR NOW

				System.err.println(found);
				return "" + found.get(0);
				 
			}

		}// while

		return result;
	}

	/*
	 * private BufferedImage cutEdges(BufferedImage image, Color foreground) { BufferedImage subimage; // cut north boolean lineClean = true; int yStart = 0; for (int y = 0; y < image.getHeight(); y++)
	 * {
	 * 
	 * for (int x = 0; x < image.getWidth(); x++) { int diff = compareTwoColors(image.getRGB(x, y), foreground.getRGB()); if (diff <= 1100) { // found one, line not clean lineClean = false; break; } }
	 * if (!lineClean) { yStart = y; // enough break; } } subimage = image.getSubimage(0, yStart, image.getWidth(), image.getHeight() - yStart); writeImage(subimage, 3);
	 * 
	 * // cut south lineClean = true; yStart = subimage.getHeight() - 1; for (int y = subimage.getHeight() - 1; y >= 0; y--) {
	 * 
	 * for (int x = 0; x < subimage.getWidth(); x++) { int diff = compareTwoColors(subimage.getRGB(x, y), foreground.getRGB()); if (diff <= 1100) { // found one, line not clean lineClean = false; break;
	 * } } if (!lineClean) { yStart = y; // enough break; } } subimage = subimage.getSubimage(0, 0, subimage.getWidth(), yStart + 1); writeImage(subimage, 4); // cut west boolean colClean = true; int
	 * xStart = 0; for (int xx = 0; xx < subimage.getWidth(); xx++) {
	 * 
	 * for (int y = 0; y < subimage.getHeight(); y++) { int diff = compareTwoColors(subimage.getRGB(xx, y), foreground.getRGB()); if (diff <= 1100) { // found one, line not clean colClean = false;
	 * break; } } if (!colClean) { xStart = xx; if (xStart > 0) xStart--; // enough break; } } subimage = subimage.getSubimage(xStart, 0, subimage.getWidth() - xStart, subimage.getHeight());
	 * writeImage(subimage, 5); // cut east colClean = true; xStart = subimage.getWidth() - 1; for (int xx = subimage.getWidth() - 1; xx >= 0; xx--) {
	 * 
	 * for (int y = 0; y < subimage.getHeight(); y++) { int diff = compareTwoColors(subimage.getRGB(xx, y), foreground.getRGB()); if (diff <= 1100) { // found one, line not clean colClean = false;
	 * break; } } if (!colClean) { xStart = xx; if (xStart < subimage.getWidth() - 1) xStart++; // enough break; } } subimage = subimage.getSubimage(0, 0, xStart + 1, subimage.getHeight());
	 * writeImage(subimage, 6); return subimage; }
	 */

	public static void main(String[] args) {
		try {
			OCRB ocr = new OCRB("ocr/digit");
			ocr.setErrors(1);
			testImage(ocr, "ocr/test_2006.bmp", "2086");
			testImage(ocr, "ocr/test_702.bmp", "702");
			testImage(ocr, "ocr/test_706.bmp", "706");
			testImage(ocr, "ocr/test_708.bmp", "708");
			testImage(ocr, "ocr/test_710.bmp", "710");
			testImage(ocr, "ocr/test_712.bmp", "712");
			testImage(ocr, "ocr/test_713.bmp", "713");
			ocr.setErrors(1);
			testImage(ocr, "ocr/test_714.bmp", "714");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void testImage(OCRB ocr, String filename, String expectedText) throws IOException {
		BufferedImage image = ImageIO.read(ImageManager.getImageURL(filename));
		FastBitmap fb = new FastBitmap(image);
		// COLOR FILTERING
		ColorFiltering colorFiltering = new ColorFiltering(new IntRange(255, 255), new IntRange(255, 255), new IntRange(
		    255, 255));
		colorFiltering.applyInPlace(fb);

		ImageIO.write(fb.toBufferedImage(), "BMP", new File("ocr/test_" + expectedText + ".bmp"));

		String res = ocr.scanImage(fb.toBufferedImage());
		// System.out.println("testing " + filename);
		System.out.println(expectedText);
		System.out.println(res);
		System.out.println(expectedText.equals(res) ? "ok" : "KO");
		System.out.println();

	}

}
