package com.horowitz.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Add;
import Catalano.Imaging.Filters.And;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Filters.Or;
import Catalano.Imaging.Filters.ReplaceColor;
import Catalano.Imaging.Filters.Xor;
import Catalano.Imaging.Tools.Blob;

public class OCRC {

  public List<Blob> detect(BufferedImage image1, BufferedImage image2) {
    FastBitmap fb = new FastBitmap(image1);
    FastBitmap fb2 = new FastBitmap(image2);
    fb.toGrayscale();
    fb2.toGrayscale();
    return null;
  }

  private static void processResources(String prefix, int n) {
    try {
      for (int j = 1; j <= n; j++) {

        BufferedImage image = ImageIO.read(new File("ocr/" + prefix + j + ".bmp"));
        FastBitmap fb = new FastBitmap(image);
        // fb.toGrayscale();

        // ContrastCorrection contrast = new ContrastCorrection(64);
        // contrast.applyInPlace(fb);

        // SHARPEN
        // Sharpen sharpenFilter = new Sharpen();
        // sharpenFilter.applyInPlace(fb);

        // COLOR FILTERING
        ColorFiltering colorFiltering = new ColorFiltering(
        // new IntRange(70, 190),
        // new IntRange(110, 255),
        // new IntRange(0, 70));

            new IntRange(40, 100), new IntRange(80, 110), new IntRange(10, 255));
//////////GREEN        new IntRange(70, 140), new IntRange(110, 255), new IntRange(0, 65));
        colorFiltering.applyInPlace(fb);

        // // SHARPEN
        // sharpenFilter = new Sharpen();
        // sharpenFilter.applyInPlace(fb);

        ImageIO.write(fb.toBufferedImage(), "BMP", new File("ocr/output" + j + ".bmp"));
        System.out.println("Done.");
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static FastBitmap processDigitAND(String digit, int numImages) {
    try {
      BufferedImage image = ImageIO.read(new File("ocr/" + digit + 1 + ".bmp"));
      FastBitmap fb1 = new FastBitmap(image);

      //fb1.toGrayscale();
      for (int i = 2; i <= numImages; i++) {

        image = ImageIO.read(new File("ocr/" + digit + i + ".bmp"));
        FastBitmap fb = new FastBitmap(image);
        //fb.toGrayscale();

        And and = new And(fb);
        and.applyInPlace(fb1);
        ImageIO.write(fb1.toBufferedImage(), "BMP", new File("ocr/digitAND" + digit + i +".bmp"));
      }
      ImageIO.write(fb1.toBufferedImage(), "BMP", new File("ocr/digitAND" + digit + ".bmp"));
      System.out.println("Done.");
      return fb1;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static FastBitmap processDigitOR(String prefix, int numImages) {
    try {
      BufferedImage image = ImageIO.read(new File("ocr/" + prefix + 1 + ".bmp"));
      FastBitmap fb1 = new FastBitmap(image);

      //fb1.toGrayscale();
      for (int i = 2; i <= numImages; i++) {

        image = ImageIO.read(new File("ocr/" + prefix + i + ".bmp"));
        FastBitmap fb = new FastBitmap(image);
        //fb.toGrayscale();

        Or xor = new Or(fb);
        xor.applyInPlace(fb1);
      }
      ImageIO.write(fb1.toBufferedImage(), "BMP", new File("ocr/digitOR" + prefix + ".bmp"));
      System.out.println("Done.");

      return fb1;

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static void main(String[] args) {
  	doDigit("gate10", 7);
  	
  	
//    processResources("Diggy", 5);
//    doDigit("output", 5);
    
//    processResources("clickable", 12);
//
//    doDigit("output", 12);
    
    // doDigit(0, 4);
    // doDigit(1, 3);
    // doDigit(2, 4);
    // doDigit(3, 4);
    // doDigit(4, 3);
    // doDigit(5, 3);
    // doDigit(6, 4);
    // doDigit(7, 4);
    // doDigit(8, 4);
    // doDigit(9, 3);
  	if (false) {
    String test = "decor3";
    try {
      BufferedImage image = ImageIO.read(new File("ocr/" + test + ".bmp"));
      FastBitmap fb1 = new FastBitmap(image);
      ColorFiltering colorFiltering = new ColorFiltering(
      // new IntRange(70, 190),
      // new IntRange(110, 255),
      // new IntRange(0, 70));
          new IntRange(70, 140), new IntRange(110, 255), new IntRange(0, 65));
      colorFiltering.applyInPlace(fb1);
      ImageIO.write(fb1.toBufferedImage(), "BMP", new File("ocr/" + test + "_filtered.bmp"));
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  	}

  }

  private static void doDigit(String digit, int n) {
    FastBitmap fbAND = processDigitAND(digit, n);
    FastBitmap fbXOR = processDigitOR(digit, n);

    Xor xor = new Xor(fbAND);
    xor.applyInPlace(fbXOR);
    try {
      // This is the final result
      ImageIO.write(fbXOR.toBufferedImage(), "BMP", new File("ocr/digitXOR" + digit + ".bmp"));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    fbAND.toRGB();
    fbXOR.toRGB();
    ReplaceColor rc = new ReplaceColor(255, 255, 255);
    rc.ApplyInPlace(fbXOR, 255, 0, 0);
    Add add = new Add(255, 0, 0);
    add.setOverlayImage(fbAND);
    add.applyInPlace(fbXOR);
    try {
      // This is the final result
      ImageIO.write(fbXOR.toBufferedImage(), "BMP", new File("ocr/digit" + digit + ".bmp"));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
