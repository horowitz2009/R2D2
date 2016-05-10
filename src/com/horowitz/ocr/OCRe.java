package com.horowitz.ocr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Add;
import Catalano.Imaging.Filters.And;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Filters.Or;
import Catalano.Imaging.Filters.ReplaceColor;
import Catalano.Imaging.Filters.Threshold;
import Catalano.Imaging.Filters.Xor;
import Catalano.Imaging.Tools.Blob;

public class OCRe {
  private ColorFiltering colorFiltering;
  private Threshold threshold;

  public Threshold getThreshold() {
    return threshold;
  }

  public void setThreshold(Threshold threshold) {
    this.threshold = threshold;
  }

  public ColorFiltering getColorFiltering() {
    return colorFiltering;
  }

  public void setColorFiltering(ColorFiltering colorFiltering) {
    this.colorFiltering = colorFiltering;
  }

  public static void main(String[] args) {
    OCRe ocr = new OCRe();
    Threshold t = new Threshold(200);
    ocr.setThreshold(t);
    ocr.learn("dazeEnergy", "s", "dazeEnergy/res", true);

  }

  public void learn(String inputFolder, String prefix, String resultFolder, boolean clean) {
    processResources(inputFolder, prefix, resultFolder);
    if (clean)
      clean(inputFolder, prefix);
  }

  private void clean(String folder, String prefix) {
    File dir = new File(folder + "/output");
    if (dir.exists()) {

      File[] listFiles = dir.listFiles();
      for (File file : listFiles) {
        file.delete();
      }
      dir.delete();
    }
  }

  public List<Blob> detect(BufferedImage image1, BufferedImage image2) {
    FastBitmap fb = new FastBitmap(image1);
    FastBitmap fb2 = new FastBitmap(image2);
    fb.toGrayscale();
    fb2.toGrayscale();
    return null;
  }

  private void processResources(String folder, String prefix, String resultFolder) {
    File dir = new File(folder);
    if (dir.exists() && dir.isDirectory()) {

      // SLASH
      {
        int n = 0;
        String fn = "";
        do {
          n++;
          fn = folder + "/" + prefix + "slash" + n + ".bmp";
        } while (new File(fn).exists());

        processResources(folder, prefix + "slash", n - 1, resultFolder);
      }
      
      // NUMBERS
      for (int d = 0; d <= 9; d++) {
        int n = 0;
        String fn = "";
        do {
          n++;
          fn = folder + "/" + prefix + d + n + ".bmp";
        } while (new File(fn).exists());

        processResources(folder, prefix + d, n - 1, resultFolder);
      }

    }
  }

  private void processResources(String folder, String prefix, int n, String resultFolder) {
    try {
      FastBitmap fb = null;
      for (int j = 1; j <= n; j++) {

        BufferedImage image = ImageIO.read(new File(folder + "/" + prefix + j + ".bmp"));
        fb = new FastBitmap(image);

        if (colorFiltering != null)
          colorFiltering.applyInPlace(fb);
        if (threshold != null) {
          if (!fb.isGrayscale())
            fb.toGrayscale();
          threshold.applyInPlace(fb);
        }

        new File(folder + "/output").mkdirs();
        new File(resultFolder).mkdirs();

        fb.saveAsBMP(folder + "/output/" + prefix + j + ".bmp");

        System.out.println(folder + "/output/" + prefix + j + ".bmp");

      }
      // /////////////////////////////////////
      if (n > 1) {
        FastBitmap fbAND = processDigitAND(folder + "/output/" + prefix, n);
        FastBitmap fbXOR = processDigitOR(folder + "/output/" + prefix, n);
        Xor xor = new Xor(fbAND);
        xor.applyInPlace(fbXOR);

        try {
          ImageIO.write(fbXOR.toBufferedImage(), "BMP", new File(folder + "/output/" + prefix + " XOR.bmp"));
        } catch (IOException e) {
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
          ImageIO.write(fbXOR.toBufferedImage(), "BMP", new File(resultFolder + "/" + prefix + ".bmp"));
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        if (fb != null) {
          fb.toRGB();
          fb.saveAsBMP(resultFolder + "/" + prefix + ".bmp");
        }
      }

      // /////////////////////////////////////

      System.out.println("Done.");

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static FastBitmap processDigitAND(String prefixFilename, int numImages) {
    try {
      BufferedImage image = ImageIO.read(new File(prefixFilename + 1 + ".bmp"));
      FastBitmap fb1 = new FastBitmap(image);

      // fb1.toGrayscale();
      for (int i = 2; i <= numImages; i++) {

        image = ImageIO.read(new File(prefixFilename + i + ".bmp"));
        FastBitmap fb = new FastBitmap(image);
        // fb.toGrayscale();

        And and = new And(fb);
        and.applyInPlace(fb1);
        ImageIO.write(fb1.toBufferedImage(), "BMP", new File(prefixFilename + i + "AND.bmp"));
      }
      ImageIO.write(fb1.toBufferedImage(), "BMP", new File(prefixFilename + "AND.bmp"));
      System.out.println("AND Done.");
      return fb1;
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private static FastBitmap processDigitOR(String prefixFilename, int numImages) {
    try {
      BufferedImage image = ImageIO.read(new File(prefixFilename + 1 + ".bmp"));
      FastBitmap fb1 = new FastBitmap(image);

      // fb1.toGrayscale();
      for (int i = 2; i <= numImages; i++) {

        image = ImageIO.read(new File(prefixFilename + i + ".bmp"));
        FastBitmap fb = new FastBitmap(image);
        // fb.toGrayscale();

        Or xor = new Or(fb);
        xor.applyInPlace(fb1);
      }
      ImageIO.write(fb1.toBufferedImage(), "BMP", new File(prefixFilename + "OR.bmp"));
      System.out.println("Done.");

      return fb1;

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
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
