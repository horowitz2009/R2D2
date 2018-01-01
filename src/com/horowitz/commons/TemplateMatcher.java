package com.horowitz.commons;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class TemplateMatcher {

  public static void main(String[] args) {
    try {
      BufferedImage image = ImageIO.read(new File("d:/work/repos/bigbusiness/images2/tags/area.bmp"));
      BufferedImage template = ImageIO.read(new File("d:/work/repos/bigbusiness/images2/tags/coins.bmp"));

      TemplateMatcher matcher = new TemplateMatcher();
      matcher.precision = 2500;
      matcher.errors = 20;
      long start = System.currentTimeMillis();
      Pixel p = matcher.findMatch(template, image, null);
      System.err.println("time: " + (System.currentTimeMillis() - start));
      if (p != null) {
        System.err.println(p);
      } else {
        System.err.println("sorry");
      }
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }
  private int errors;
  private int precision;

  private double similarityThreshold = 0.95d;
  
  public double analize(BufferedImage image1, BufferedImage image2, double maxDistance, Color colorToBypass) {
    if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
      return Integer.MAX_VALUE;
    }
    double res = 0;

    for (int x = 0; x < image1.getWidth(); x++) {
      for (int y = 0; y < image1.getHeight(); y++) {
        int rgb1 = image1.getRGB(x, y);
        int rgb2 = image2.getRGB(x, y);

        if (new Color(rgb1).equals(colorToBypass)) {
          rgb1 = rgb2;
        }

        int diff = Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))
            * Math.abs(((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))

            + Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))
            * Math.abs(((rgb1 >> 8) & 0xFF) - ((rgb2 >> 8) & 0xFF))

            + Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF))
            * Math.abs(((rgb1 >> 0) & 0xFF) - ((rgb2 >> 0) & 0xFF));
        res += Math.sqrt(diff);
        if (res > maxDistance) {
          // too different => skip
          return res;
        }
      }
    }
    return res;
  }
  
  public boolean compare(BufferedImage image1, BufferedImage image2) {
    if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
      return false;
    }

    double allowedDistance = Math.sqrt(255 * 255 * 3) * image1.getWidth() * image1.getHeight() * (1 - similarityThreshold);

    return analizeQ(image1, image2, allowedDistance) <= allowedDistance;
  }
  
  public double analizeQ(BufferedImage image1, BufferedImage image2, double allowedDistance) {
    if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
      return Integer.MAX_VALUE;
    }
    double res = 0;
    int w = image1.getWidth();
    int h = image1.getHeight();
    int m = 4;
    for (int xOff = 0; xOff < m; xOff++) {
      for (int yOff = 0; yOff < m; yOff++) {
        
        for (int x = 0 + xOff; x < w; x += m) {
          for (int y = 0 + yOff; y < h; y += m) {
            int rgb1 = image1.getRGB(x, y);
            int rgb2 = image2.getRGB(x, y);
            
            int diff = Math.abs(    (((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) *
                (((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))      )     
                
                +  Math.abs(    (((rgb1 >>  8) & 0xFF) - ((rgb2 >>  8) & 0xFF)) *
                    (((rgb1 >>  8) & 0xFF) - ((rgb2 >>  8) & 0xFF))      )
                    
                    + Math.abs(    (((rgb1 >>  0) & 0xFF) - ((rgb2 >>  0) & 0xFF)) * 
                        (((rgb1 >>  0) & 0xFF) - ((rgb2 >>  0) & 0xFF))      );
            
            
            // System.err.println(diff1 + "   vs   "  + diff2);
            res += Math.sqrt(diff);
            if (res > allowedDistance) {
              // too different => skip
              return res;
            }
          }// y
        }// x
      }// yOff
    }// xOff
    return res;
  }

  public double analizeQ(BufferedImage image1, BufferedImage image2, double maxDistance, Color colorToBypass) {
    if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
      return Integer.MAX_VALUE;
    }
    double res = 0;
    int w = image1.getWidth();
    int h = image1.getHeight();
    int m = 4;
    for (int xOff = 0; xOff < m; xOff++) {
      for (int yOff = 0; yOff < m; yOff++) {

        for (int x = 0 + xOff; x < w; x += m) {
          for (int y = 0 + yOff; y < h; y += m) {
            int rgb1 = image1.getRGB(x, y);
            int rgb2 = image2.getRGB(x, y);

            if (new Color(rgb1).equals(colorToBypass)) {
              rgb1 = rgb2;
            }

            int diff2 = Math.abs(    (((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF)) *
                                     (((rgb1 >> 16) & 0xFF) - ((rgb2 >> 16) & 0xFF))      )     
                
                     +  Math.abs(    (((rgb1 >>  8) & 0xFF) - ((rgb2 >>  8) & 0xFF)) *
                                     (((rgb1 >>  8) & 0xFF) - ((rgb2 >>  8) & 0xFF))      )
                
                      + Math.abs(    (((rgb1 >>  0) & 0xFF) - ((rgb2 >>  0) & 0xFF)) * 
                                     (((rgb1 >>  0) & 0xFF) - ((rgb2 >>  0) & 0xFF))      );
            
            
           // System.err.println(diff1 + "   vs   "  + diff2);
            res += Math.sqrt(diff2);
            if (res > maxDistance) {
              // too different => skip
              return res;
            }
          }// y
        }// x
      }// yOff
    }// xOff
    return res;
  }
  
  public double analizeQBW(BufferedImage image1, BufferedImage image2, double maxDistance) {
    if ((image1.getWidth() != image2.getWidth()) || (image1.getHeight() != image2.getHeight())) {
      return Integer.MAX_VALUE;
    }
    double res = 0;
    int w = image1.getWidth();
    int h = image1.getHeight();
    int m = 4;
    for (int xOff = 0; xOff < m; xOff++) {
      for (int yOff = 0; yOff < m; yOff++) {
        
        for (int x = 0 + xOff; x < w; x += m) {
          for (int y = 0 + yOff; y < h; y += m) {
            int rgb1 = image1.getRGB(x, y);
            int rgb2 = image2.getRGB(x, y);
            
            // System.err.println(diff1 + "   vs   "  + diff2);
            res += Math.abs(    (((rgb1 >>  0) & 0xFF) - ((rgb2 >>  0) & 0xFF)));
            if (res > maxDistance) {
              // too different => skip
              return res;
            }
          }// y
        }// x
      }// yOff
    }// xOff
    return res;
  }

  public Pixel findMatch(BufferedImage template, BufferedImage image, Color colorToBypass) {
    double maxDistance = Math.sqrt(255 * 255 * 3) * template.getWidth() * template.getHeight();
    maxDistance *= (1 - similarityThreshold);
    for (int i = 0; i <= (image.getWidth() - template.getWidth()); i++) {
      for (int j = 0; j <= (image.getHeight() - template.getHeight()); j++) {
        final BufferedImage subimage = image.getSubimage(i, j, template.getWidth(), template.getHeight());
        double distance = analize(template, subimage, maxDistance, colorToBypass);
        if (distance <= maxDistance) {
          return new Pixel(i, j);
        }
        
      }
    }
    return null;
  }

  public List<Pixel> findMatches(BufferedImage template, BufferedImage image, Color colorToBypass) {
    List<Pixel> result = new ArrayList<Pixel>();

    double maxDistance = Math.sqrt(255 * 255 * 3) * template.getWidth() * template.getHeight();
    maxDistance *= (1 - similarityThreshold);

    for (int j = 0; j <= (image.getHeight() - template.getHeight()); j++) {
      for (int i = 0; i <= (image.getWidth() - template.getWidth()); i++) {
        final BufferedImage subimage = image.getSubimage(i, j, template.getWidth(), template.getHeight());
        double distance = analize(template, subimage, maxDistance, colorToBypass);
        if (distance <= maxDistance) {
          result.add(new Pixel(i, j));
          i += template.getWidth() - 2;
          // j += template.getHeight() - 2;
          // j += 3;
          i += 5;
        }

      }
    }
    return result;
  }
  
  public Pixel findMatchQ(BufferedImage image, BufferedImage screen) {
    double allowedDistance = Math.sqrt(255 * 255 * 3) * image.getWidth() * image.getHeight() * (1 - similarityThreshold);
    for (int i = 0; i <= (screen.getWidth() - image.getWidth()); i++) {
      for (int j = 0; j <= (screen.getHeight() - image.getHeight()); j++) {
        final BufferedImage subimage = screen.getSubimage(i, j, image.getWidth(), image.getHeight());
        double distance = analizeQ(image, subimage, allowedDistance);
        if (distance <= allowedDistance) {
          return new Pixel(i, j);
        }
        
      }
    }
    return null;
  }

  public Pixel findMatchQ(BufferedImage image, BufferedImage screen, Color colorToBypass) {
    if (colorToBypass == null)
      return findMatchQ(image, screen);
    
    double maxDistance = Math.sqrt(255 * 255 * 3) * image.getWidth() * image.getHeight();
    maxDistance *= (1 - similarityThreshold);
    for (int i = 0; i <= (screen.getWidth() - image.getWidth()); i++) {
      for (int j = 0; j <= (screen.getHeight() - image.getHeight()); j++) {
        final BufferedImage subimage = screen.getSubimage(i, j, image.getWidth(), image.getHeight());
        double distance = analizeQ(image, subimage, maxDistance, colorToBypass);
        if (distance <= maxDistance) {
          return new Pixel(i, j);
        }

      }
    }
    return null;
  }
  
  public Pixel findMatchQBW(BufferedImage image, BufferedImage screen) {
    double maxDistance = 255 * image.getWidth() * image.getHeight();
    maxDistance *= (1 - similarityThreshold);
    for (int i = 0; i <= (screen.getWidth() - image.getWidth()); i++) {
      for (int j = 0; j <= (screen.getHeight() - image.getHeight()); j++) {
        final BufferedImage subimage = screen.getSubimage(i, j, image.getWidth(), image.getHeight());
        double distance = analizeQBW(image, subimage, maxDistance);
        if (distance <= maxDistance) {
          return new Pixel(i, j);
        }
        
      }
    }
    return null;
  }

  public double getSimilarityThreshold() {
    return similarityThreshold;
  }

  public void setSimilarityThreshold(double similarityThreshold) {
    this.similarityThreshold = similarityThreshold;
  }

}
