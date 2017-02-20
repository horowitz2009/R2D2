package com.horowitz.commons;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import Catalano.Core.IntRange;
import Catalano.Imaging.FastBitmap;
import Catalano.Imaging.Filters.Add;
import Catalano.Imaging.Filters.ColorFiltering;
import Catalano.Imaging.Filters.ReplaceColor;
import Catalano.Imaging.Filters.Threshold;

public class BaseScreenScanner {

  private final static Logger LOGGER = Logger.getLogger("MAIN");

  private Settings _settings;
  private ImageComparator _comparator;
  private TemplateMatcher _matcher;
  private MouseRobot _mouse;
  public Pixel _br = null;
  public Pixel _tl = null;
  private boolean _optimized = false;
  private boolean _debugMode = false;

  private Map<String, ImageData> _imageDataCache;
  private Map<String, BufferedImage> _imageBWCache;

  private Threshold _threshold = new Threshold(200);

  public BaseScreenScanner(Settings settings) {
    _settings = settings;
    _comparator = new SimilarityImageComparator(0.04, 2000);
    _matcher = new TemplateMatcher();

    try {
      _mouse = new MouseRobot();
    } catch (AWTException e1) {
      e1.printStackTrace();
    }
    _imageDataCache = new Hashtable<String, ImageData>();
    _imageBWCache = new Hashtable<String, BufferedImage>();

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    _tl = new Pixel(0, 0);
    _br = new Pixel(screenSize.width - 3, screenSize.height - 3);

  }

  public Pixel getBottomRight() {
    return _br;
  }

  public void setBottomRight(Pixel br) {
    this._br = br;
  }

  public Pixel getTopLeft() {
    return _tl;
  }

  public void setTopLeft(Pixel tl) {
    this._tl = tl;
  }

  public Rectangle generateWindowedArea(int width, int height) {
    int xx = (getGameWidth() - width) / 2;
    int yy = (getGameHeight() - height) / 2;
    return new Rectangle(_tl.x + xx, _tl.y + yy, width, height);
  }

  public ImageData getImageData(String filename) throws IOException {
    return getImageData(filename, null, 0, 0);
  }

  public ImageData getImageData(String filename, Rectangle defaultArea, int xOff, int yOff) throws IOException {
    // if (!new File(filename).exists())
    // return null;

    if (_imageDataCache.containsKey(filename)) {
      return _imageDataCache.get(filename);
    } else {
      ImageData imageData = null;
      try {
        imageData = new ImageData(filename, defaultArea, _comparator, xOff, yOff);
      } catch (IOException e) {
        System.err.println(e);
        return null;
      }
      if (imageData != null)
        _imageDataCache.put(filename, imageData);
      return imageData;
    }
  }

  public BufferedImage getImageBW(String filename, Color colorToBypass) throws IOException {
    if (_imageBWCache.containsKey(filename)) {
      return _imageBWCache.get(filename);
    } else {
      BufferedImage image = ImageManager.loadImage(filename);
      image = convertToBWSmart(image, colorToBypass);
      _imageBWCache.put(filename, image);
      return image;
    }
  }

  // ColorFiltering

  public BufferedImage convertToBWSmart(BufferedImage image, Color colorToBypass) {
    final FastBitmap fb = new FastBitmap(image);
    final FastBitmap fbRed = new FastBitmap(image);

    if (_threshold != null) {
      if (colorToBypass != null) {

        int min = _threshold.getValue() - 5;
        int max = _threshold.getValue() + 5;

        ColorFiltering filtering = new ColorFiltering(new IntRange(min, max), new IntRange(min, max), new IntRange(min,
            max));
        filtering.applyInPlace(fbRed);
        fbRed.toGrayscale();
        new Threshold(1).applyInPlace(fbRed);
        fbRed.toRGB();

        new ReplaceColor(255, 255, 255).ApplyInPlace(fbRed, colorToBypass.getRed(), colorToBypass.getGreen(),
            colorToBypass.getBlue());

        if (!fb.isGrayscale())
          fb.toGrayscale();
        new Threshold(max).applyInPlace(fb);
        fb.toRGB();

        Add add = new Add(colorToBypass.getRed(), colorToBypass.getGreen(),
            colorToBypass.getBlue());
        add.setOverlayImage(fbRed);
        add.applyInPlace(fb);
        return fb.toBufferedImage();
      } else
        return convertToBW(image, colorToBypass);
    }
    return image;
  }

  public BufferedImage convertToBW(BufferedImage image, Color colorToBypass) {
    final FastBitmap fb = new FastBitmap(image);

    if (_threshold != null) {
      if (!fb.isGrayscale())
        fb.toGrayscale();
      _threshold.applyInPlace(fb);
      fb.toRGB();
      return fb.toBufferedImage();
    }
    return image;
  }

  public boolean isOptimized() {
    return _optimized && _br != null && _tl != null;
  }

  public int getGameWidth() {
    int width = isOptimized() ? _br.x - _tl.x : Toolkit.getDefaultToolkit().getScreenSize().width;
    return width != 0 ? width : Toolkit.getDefaultToolkit().getScreenSize().width;
  }

  public int getGameHeight() {
    if (isOptimized()) {
      return _br.y - _tl.y == 0 ? Toolkit.getDefaultToolkit().getScreenSize().height : _br.y - _tl.y;
    } else {
      return Toolkit.getDefaultToolkit().getScreenSize().height;
    }
  }

  public ImageData generateImageData(String imageFilename) throws IOException {
    return new ImageData(imageFilename, null, _comparator, 0, 0);
  }

  public ImageData setImageData(String imageFilename) throws IOException {
    return getImageData(imageFilename, null, 0, 0);
  }

  public ImageData generateImageData(String imageFilename, int xOff, int yOff) throws IOException {
    return new ImageData(imageFilename, null, _comparator, xOff, yOff);
  }

  public ImageComparator getImageComparator() {
    return _comparator;
  }

  public List<Pixel> scanMany(String filename, BufferedImage screen, boolean click) throws RobotInterruptedException,
      IOException, AWTException {

    ImageData imageData = getImageData(filename);
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    return scanMany(imageData, screen, click);
  }

  public List<Pixel> scanManyFast(String filename, BufferedImage screen, boolean click)
      throws RobotInterruptedException, IOException, AWTException {

    ImageData imageData = getImageData(filename);
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    return scanMany(imageData, screen, click);
  }

  public List<Pixel> scanMany(ImageData imageData, BufferedImage screen, boolean click)
      throws RobotInterruptedException, IOException, AWTException {
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    Rectangle area = imageData.getDefaultArea();
    if (screen == null)
      screen = new Robot().createScreenCapture(area);
    List<Pixel> matches = _matcher.findMatches(imageData.getImage(), screen, imageData.getColorToBypass());
    if (!matches.isEmpty()) {
      Collections.sort(matches);
      Collections.reverse(matches);

      // filter similar
      if (matches.size() > 1) {
        for (int i = matches.size() - 1; i > 0; --i) {
          for (int j = i - 1; j >= 0; --j) {
            Pixel p1 = matches.get(i);
            Pixel p2 = matches.get(j);
            if (Math.abs(p1.x - p2.x) <= 3 && Math.abs(p1.y - p2.y) <= 3) {
              // too close to each other
              // remove one
              matches.remove(j);
              --i;
            }
          }
        }
      }

      for (Pixel pixel : matches) {
        pixel.x += (area.x + imageData.get_xOff());
        pixel.y += (area.y + imageData.get_yOff());
        if (click)
          _mouse.click(pixel.x, pixel.y);
      }
    }
    return matches;
  }

  public List<Pixel> scanManyFast(ImageData imageData, BufferedImage screen, boolean click)
      throws RobotInterruptedException, IOException, AWTException {
    if (imageData == null)
      return new ArrayList<Pixel>(0);
    Rectangle area = imageData.getDefaultArea();
    if (screen == null)
      screen = new Robot().createScreenCapture(area);
    List<Pixel> matches = _matcher.findMatches(imageData.getImage(), screen, imageData.getColorToBypass());
    if (!matches.isEmpty()) {
      Collections.sort(matches);
      Collections.reverse(matches);

      // filter similar
      if (matches.size() > 1) {
        for (int i = matches.size() - 1; i > 0; --i) {
          for (int j = i - 1; j >= 0; --j) {
            Pixel p1 = matches.get(i);
            Pixel p2 = matches.get(j);
            if (Math.abs(p1.x - p2.x) <= 3 && Math.abs(p1.y - p2.y) <= 3) {
              // too close to each other
              // remove one
              matches.remove(j);
              --i;
            }
          }
        }
      }

      for (Pixel pixel : matches) {
        pixel.x += (area.x + imageData.get_xOff());
        pixel.y += (area.y + imageData.get_yOff());
        if (click)
          _mouse.click(pixel.x, pixel.y);
      }
    }
    return matches;
  }

  // HMM
  public Pixel scanPrecise(ImageData imageData, Rectangle area) throws AWTException, RobotInterruptedException {

    if (area == null) {
      area = imageData.getDefaultArea();
    }
    BufferedImage screen = new Robot().createScreenCapture(area);
    // writeImage2(area, "area.bmp");

    FastBitmap fbID = new FastBitmap(imageData.getImage());
    FastBitmap fbAREA = new FastBitmap(screen);

    // COLOR FILTERING
    ColorFiltering colorFiltering = new ColorFiltering(new IntRange(255, 255), new IntRange(255, 255), new IntRange(
        255, 255));
    colorFiltering.applyInPlace(fbID);
    colorFiltering.applyInPlace(fbAREA);

    Pixel pixel = _matcher.findMatch(fbID.toBufferedImage(), fbAREA.toBufferedImage(), null);
    LOGGER
        .fine("LOOKING FOR " + imageData.getName() + "  screen: " + area + " BYPASS: " + imageData.getColorToBypass());

    long start = System.currentTimeMillis();
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found : " + imageData.getName() + pixel + " " + (System.currentTimeMillis() - start));
    }
    return pixel;

  }

  public Pixel scanOneBW(ImageData imageData, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException {
    return scanOne(imageData, area, click, null, true);
  }

  public Pixel scanOne(ImageData imageData, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException {
    return scanOne(imageData, area, click, null, false);
  }

  public Pixel scanOne(ImageData imageData, Rectangle area, boolean click, Color colorToBypass, boolean bwMode)
      throws AWTException {
    if (area == null) {
      area = imageData.getDefaultArea();
    }
    BufferedImage screen = new Robot().createScreenCapture(area);

    if (imageData.getFilename().endsWith("F.bmp")) {
      FastBitmap fb = new FastBitmap(screen);
      fb.toGrayscale();
      new Threshold(200).applyInPlace(fb);
      fb.toRGB();
      // fb.saveAsBMP("ship_area.bmp");
    }
    Pixel pixel = _matcher.findMatch(imageData.getImage(), screen,
        colorToBypass != null ? colorToBypass : imageData.getColorToBypass());
    LOGGER
        .fine("LOOKING FOR " + imageData.getName() + "  screen: " + area + " BYPASS: " + imageData.getColorToBypass());

    long start = System.currentTimeMillis();
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found : " + imageData.getName() + pixel + " " + (System.currentTimeMillis() - start));
      if (click) {
        _mouse.click(pixel.x, pixel.y);
      }
    }
    return pixel;

  }

  public Pixel scanOne(String filename, Rectangle area, Color colorToBypass) throws RobotInterruptedException,
      IOException, AWTException {
    ImageData imageData = getImageData(filename);
    if (imageData == null)
      return null;
    if (area == null)
      area = imageData.getDefaultArea();
    if (area == null)
      area = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());

    BufferedImage screen = new Robot().createScreenCapture(area);
    if (_debugMode)
      writeImage(screen, imageData.getName() + "_area.png");
    long start = System.currentTimeMillis();
    if (colorToBypass == null)
      colorToBypass = imageData.getColorToBypass();
    Pixel pixel = _matcher.findMatch(imageData.getImage(), screen, colorToBypass);
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found: " + imageData.getName() + pixel + " " + (System.currentTimeMillis() - start));
    }
    return pixel;
  }

  public Pixel scanOneFast(ImageData imageData, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException {
    if (area == null) {
      area = imageData.getDefaultArea();
      if (area == null) {
        // not recommended
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        area = new Rectangle(0, 0, d.width - 1, d.height - 1);
      }
    }
    BufferedImage screen = new Robot().createScreenCapture(area);
    if (_debugMode) {
      writeImage(screen, imageData.getName() + "_area.png");
    }
    long start = System.currentTimeMillis();
    Pixel pixel = _comparator.findImage(imageData.getImage(), screen, imageData.getColorToBypass());
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found: " + imageData.getName() + pixel + " " + (System.currentTimeMillis() - start));
      if (click) {
        _mouse.click(pixel.x, pixel.y);
      }
    }
    return pixel;

  }

  public Pixel scanOneFast(String filename, Rectangle area, Color colorToBypass, boolean bwMode)
      throws RobotInterruptedException, IOException, AWTException {

    ImageData imageData = getImageData(filename);
    if (imageData == null)
      return null;

    BufferedImage image = imageData.getImage();

    if (colorToBypass == null)
      colorToBypass = imageData.getColorToBypass();

    if (bwMode) {
      image = getImageBW(filename, colorToBypass);
    }

    if (area == null)
      area = imageData.getDefaultArea();
    if (area == null)
      area = new Rectangle(new Point(0, 0), Toolkit.getDefaultToolkit().getScreenSize());

    BufferedImage screen = new Robot().createScreenCapture(area);
    if (bwMode) {
      screen = convertToBW(screen, null);
    }

    long start = System.currentTimeMillis();

    Pixel pixel = _comparator.findImage(image, screen, colorToBypass);
    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found: " + imageData.getName() + pixel + " " + (System.currentTimeMillis() - start));
    }
    return pixel;
  }

  public TemplateMatcher getMatcher() {
    return _matcher;
  }

  public void setMatcher(TemplateMatcher matcher) {
    _matcher = matcher;
  }

  public MouseRobot getMouse() {
    return _mouse;
  }

  public void reduceThreshold() {
    _matcher.setSimilarityThreshold(.85d);
  }

  public void restoreThreshold() {
    _matcher.setSimilarityThreshold(.95d);

  }

  public boolean isDebugMode() {
    return _debugMode;
  }

  public void setDebugMode(boolean debugMode) {
    _debugMode = debugMode;
  }

  public void setOptimized(boolean fullyOptimized) {
    _optimized = fullyOptimized;
  }

  public Pixel scanPrecise(String filename, Rectangle area) throws AWTException, IOException, RobotInterruptedException {
    return scanPrecise(getImageData(filename), area);
  }

  // ///////////////////////////////////////////////////////
  // ///////////////////////////////////////////////////////

  public void deleteOlder(String prefix, int amountFiles) {
    File f = new File(".");
    File[] files = f.listFiles();
    List<File> targetFiles = new ArrayList<File>(6);
    int cnt = 0;
    for (File file : files) {
      if (!file.isDirectory() && file.getName().startsWith(prefix)) {
        targetFiles.add(file);
        cnt++;
      }
    }

    if (cnt > amountFiles) {
      // delete some files
      Collections.sort(targetFiles, new Comparator<File>() {
        public int compare(File o1, File o2) {
          if (o1.lastModified() > o2.lastModified())
            return 1;
          else if (o1.lastModified() < o2.lastModified())
            return -1;
          return 0;
        };
      });

      int c = cnt - 5;
      for (int i = 0; i < c; i++) {
        File fd = targetFiles.get(i);
        fd.delete();
      }
    }
  }

  public void captureScreen(String filenamePrefix, boolean timestamp) {
    captureArea(null, filenamePrefix, timestamp);
  }

  public void captureArea(Rectangle area, String filenamePrefix, boolean timestamp) {
    if (filenamePrefix == null)
      filenamePrefix = "ping ";
    if (area == null) {
      final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      area = new Rectangle(0, 0, screenSize.width, screenSize.height);
    }
    String filename = filenamePrefix;
    if (timestamp)
      filename += DateUtils.formatDateForFile(System.currentTimeMillis());
    filename += ".jpg";
    writeArea(area, filename);
    if (!_settings.getBoolean("ping.keep", false))
      deleteOlder("ping", _settings.getInt("ping.cnt", 12));

  }

  public void captureGameAreaDT() {
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd  HH-mm-ss-SSS");
    String date = sdf.format(Calendar.getInstance().getTime());
    String filename = "popup " + date + ".jpg";
    captureGameArea(filename);
  }

  public void captureGameArea(String filename) {
    writeArea(new Rectangle(new Point(_tl.x, _tl.y), new Dimension(getGameWidth(), getGameHeight())), filename);
  }

  public void writeArea(Rectangle rect, String filename) {
    MyImageIO.writeArea(rect, filename);
  }

  public void writeAreaTS(Rectangle rect, String filename) {
    MyImageIO.writeAreaTS(rect, filename);
  }

  public void writeImageTS(BufferedImage image, String filename) {
    MyImageIO.writeImageTS(image, filename);
  }

  public void writeImage(BufferedImage image, String filename) {
    MyImageIO.writeImage(image, filename);
  }

  public Threshold getThreshold() {
    return _threshold;
  }

  public void setThreshold(Threshold threshold) {
    this._threshold = threshold;
  }

  public static void main(String[] args) {
    try {
      BufferedImage image = ImageManager.loadImage("C:\\prj\\repos\\Mickey2\\images\\journey.bmp");
      FastBitmap fb = new FastBitmap(image);
      fb.saveAsBMP("C:/work/haha.bmp");
      BaseScreenScanner scanner = new BaseScreenScanner(null);
      image = scanner.convertToBWSmart(image, Color.RED);
      fb = new FastBitmap(image);
      fb.saveAsBMP("C:/work/haha2.bmp");
      System.err.println("done");
      
      
      
      Pixel p = scanner.scanOneFast("C:\\prj\\repos\\Mickey2\\images\\journey.bmp", null, Color.RED, true);
      System.err.println(p);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (RobotInterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (AWTException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
