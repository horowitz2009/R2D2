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

  protected final static Logger LOGGER = Logger.getLogger("MAIN");

  protected Settings _settings;
  protected ImageComparator _comparator;
  protected GameLocator _gameLocator;
  protected TemplateMatcher _matcher;
  protected MouseRobot _mouse;
  public Pixel _br = null;
  public Pixel _tl = null;
  protected boolean _optimized = false;
  protected boolean _debugMode = false;

  public Rectangle _fullArea = null;

  public Rectangle _fbArea;

  public Pixel _safePoint;
  public Pixel _parkingPoint;

  protected Map<String, ImageData> _imageDataCache;
  protected Map<String, BufferedImage> _imageBWCache;

  protected Threshold _threshold = new Threshold(200);

  public BaseScreenScanner(Settings settings) {
    _settings = settings;
    _comparator = new SimilarityImageComparator(0.04, 2000);
    _matcher = new TemplateMatcher();
    _gameLocator = new GameLocator();
    try {
      _mouse = new MouseRobot();
    } catch (AWTException e1) {
      e1.printStackTrace();
    }
    _imageDataCache = new Hashtable<String, ImageData>();
    _imageBWCache = new Hashtable<String, BufferedImage>();

    reset();
  }

  public void reset() {
    _optimized = false;

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    _tl = new Pixel(0, 0);
    _br = new Pixel(screenSize.width - 3, screenSize.height - 3);

    _fullArea = new Rectangle(_tl.x, _tl.y, getGameWidth(), getGameHeight());
    _parkingPoint = new Pixel(_br.x + 2, _tl.x + getGameHeight() / 2);
    _safePoint = new Pixel(_parkingPoint);

    _fbArea = new Rectangle(_fullArea);
    _fbArea.x += _fbArea.width / 2;
    _fbArea.width = _fbArea.width / 2;
    _fbArea.y += _fbArea.height / 2;
    _fbArea.height = _fbArea.height / 2;
  }

  protected void setKeyAreas() throws IOException, AWTException, RobotInterruptedException {
    setOptimized(true);
    _fullArea = new Rectangle(_tl.x, _tl.y, getGameWidth(), getGameHeight());
    _parkingPoint = new Pixel(_br.x + 2, _tl.x + getGameHeight() / 2);
    _safePoint = new Pixel(_parkingPoint);

    _fbArea = new Rectangle(_fullArea);
    _fbArea.x += _fbArea.width / 2;
    _fbArea.width = _fbArea.width / 2;
    _fbArea.y += _fbArea.height / 2;
    _fbArea.height = _fbArea.height / 2;
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
    if (_imageDataCache.containsKey(filename)) {
      return _imageDataCache.get(filename);
    } else {
      ImageData imageData = null;
      try {
        imageData = new ImageData(filename, null, _comparator, 0, 0);
      } catch (IOException e) {
        System.err.println(e);
        return null;
      }
      if (imageData != null)
        _imageDataCache.put(filename, imageData);
      return imageData;
    }
  }

  public ImageData getImageData(String filename, Rectangle defaultArea, int xOff, int yOff) throws IOException {
    if (_imageDataCache.containsKey(filename)) {
      ImageData id = _imageDataCache.get(filename);
      id.setDefaultArea(defaultArea);
      id.set_xOff(xOff);
      id.set_yOff(yOff);
      return id;
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

  public void clearCache() {
    _imageDataCache.clear();
    _imageBWCache.clear();
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

        Add add = new Add(colorToBypass.getRed(), colorToBypass.getGreen(), colorToBypass.getBlue());
        add.setOverlayImage(fbRed);
        add.applyInPlace(fb);
        return fb.toBufferedImage();
      } else
        return convertToBW(image);
    }
    return image;
  }

  public BufferedImage convertToBW(BufferedImage image) {
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
  public Pixel scanPrecise2(ImageData imageData, Rectangle area) throws AWTException, RobotInterruptedException {

    if (area == null) {
      area = imageData.getDefaultArea();
    }
    BufferedImage screen = new Robot().createScreenCapture(area);

    long start = System.currentTimeMillis();

    FastBitmap fbID = new FastBitmap(imageData.getImage());
    FastBitmap fbAREA = new FastBitmap(screen);

    // THRESHOLD FILTERING
    Threshold t = new Threshold(255);
    if (!fbID.isGrayscale())
      fbID.toGrayscale();
    fbAREA.toGrayscale();
    t.applyInPlace(fbID);
    t.applyInPlace(fbAREA);

    Pixel pixel = _matcher.findMatch(fbID.toBufferedImage(), fbAREA.toBufferedImage(), null);
    LOGGER
        .fine("LOOKING FOR " + imageData.getName() + "  screen: " + area + " BYPASS: " + imageData.getColorToBypass());

    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("scanPrecise2.found: " + imageData.getName() + " - " + pixel + " "
          + (System.currentTimeMillis() - start));
    }
    return pixel;

  }

  public Pixel scanPrecise(ImageData imageData, Rectangle area) throws AWTException, RobotInterruptedException {

    if (area == null) {
      area = imageData.getDefaultArea();
    }
    BufferedImage screen = new Robot().createScreenCapture(area);

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
      LOGGER.fine("scanPrecise2.found: " + imageData.getName() + " - " + pixel + " "
          + (System.currentTimeMillis() - start));
    }
    return pixel;

  }

  /**
   * ULTIMATE.
   * 
   * @param imageData
   * @param area
   * @param click
   * @param colorToBypass
   * @param bwMode
   * @return
   * @throws AWTException
   */
  public Pixel scanOne(ImageData imageData, Rectangle area, boolean click, Color colorToBypass, boolean bwMode)
      throws AWTException {
    if (imageData == null)
      return null;

    if (area == null) {
      area = imageData.getDefaultArea();
    }

    if (area == null) {
      // not recommended
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      area = new Rectangle(0, 0, d.width - 1, d.height - 1);
    }

    BufferedImage screen = new Robot().createScreenCapture(area);

    if (imageData.getFilename().endsWith("F.bmp") || bwMode) {
      screen = convertToBW(screen);
    }

    if (colorToBypass == null)
      colorToBypass = imageData.getColorToBypass();

    if (colorToBypass == null)
      colorToBypass = Color.RED;

    if (_debugMode) {
      writeImage(screen, imageData.getName() + "_area.png");
    }

    LOGGER.fine("LOOKING FOR " + imageData.getName() + "  screen: " + area + " BYPASS: " + colorToBypass);
    long start = System.currentTimeMillis();

    // The real deal
    Pixel pixel = _matcher.findMatch(imageData.getImage(), screen, colorToBypass);

    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found : " + imageData.getName() + " - " + pixel + " " + (System.currentTimeMillis() - start));
      if (click) {
        _mouse.click(pixel.x, pixel.y);
      }
    }
    return pixel;

  }

  // .scanOne("DuelsClock.bmp", new Rectangle(x - 70, y, 140, 67), false);

  public Pixel scanOne(String filename, Rectangle area, boolean click) throws AWTException, RobotInterruptedException,
      IOException {
    return scanOne(getImageData(filename), area, click, null, false);
  }

  public Pixel scanOne(String filename, Rectangle area, boolean click, boolean bwMode) throws AWTException,
      RobotInterruptedException, IOException {
    return scanOne(getImageData(filename), area, click, null, bwMode);
  }

  public Pixel scanOneBW(ImageData imageData, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException {
    return scanOne(imageData, area, click, null, true);
  }

  /**
   * This should be the most used method
   * 
   * @param imageData
   * @param area
   * @param click
   * @return
   * @throws AWTException
   * @throws RobotInterruptedException
   */
  public Pixel scanOne(ImageData imageData, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException {
    return scanOne(imageData, area, click, null, false);
  }

  public Pixel scanOne(String filename, Rectangle area, boolean click, Color colorToBypass, boolean bwMode)
      throws RobotInterruptedException, IOException, AWTException {
    return scanOne(getImageData(filename), area, click, colorToBypass, bwMode);
  }

  public Pixel scanOne(String filename, Rectangle area, Color colorToBypass) throws RobotInterruptedException,
      IOException, AWTException {
    return scanOne(getImageData(filename), area, false, colorToBypass, false);
  }

  /**
   * Use default area or whole screen
   * 
   * @param filename
   * @param click
   * @return
   * @throws AWTException
   * @throws RobotInterruptedException
   * @throws IOException
   */
  public Pixel scanOneFast(String filename, boolean click) throws AWTException, RobotInterruptedException, IOException {
    return scanOneFast(getImageData(filename), null, click, null, false, false);
  }

  /**
   * Use this area instead of default
   * 
   * @param filename
   * @param area
   * @param click
   * @return
   * @throws AWTException
   * @throws RobotInterruptedException
   * @throws IOException
   */
  public Pixel scanOneFast(String filename, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException, IOException {
    // use this area instead of default
    return scanOneFast(getImageData(filename), area, click, null, false, false);
  }

  public Pixel scanOneFast(ImageData imageData, Rectangle area, boolean click) throws AWTException,
      RobotInterruptedException {
    return scanOneFast(imageData, area, click, null, false, false);
  }

  /**
   * ULTIMATE Use this area instead of default. If area passed is null, use the whole screen. xOff and yOff should come
   * with imageData
   * 
   * @param imageData
   * @param area
   * @param click
   * @return
   * @throws AWTException
   * @throws RobotInterruptedException
   */
  public Pixel scanOneFast(ImageData imageData, Rectangle area, boolean click, Color colorToBypass, boolean bwMode,
      boolean convertImage) throws AWTException, RobotInterruptedException {

    if (imageData == null)
      return null;

    if (area == null) {
      area = imageData.getDefaultArea();
    }

    if (area == null) {
      // not recommended
      Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
      area = new Rectangle(0, 0, d.width - 1, d.height - 1);
    }

    BufferedImage screen = new Robot().createScreenCapture(area);

    if (imageData.getFilename().endsWith("F.bmp") || bwMode) {
      screen = convertToBW(screen);
    }

    if (colorToBypass == null)
      colorToBypass = imageData.getColorToBypass();

    if (colorToBypass == null)
      colorToBypass = Color.RED;

    if (_debugMode) {
      writeImage(screen, imageData.getName() + "_area.png");
    }

    LOGGER.fine("LOOKING FOR " + imageData.getName() + "  screen: " + area + " BYPASS: " + colorToBypass);
    long start = System.currentTimeMillis();

    BufferedImage image = imageData.getImage();
    if (convertImage)
      image = convertToBW(image);

    // The real deal
    Pixel pixel = _comparator.findImage(image, screen, colorToBypass);

    if (pixel != null) {
      pixel.x += (area.x + imageData.get_xOff());
      pixel.y += (area.y + imageData.get_yOff());
      LOGGER.fine("found : " + imageData.getName() + " - " + pixel + " " + (System.currentTimeMillis() - start));
      if (click) {
        _mouse.click(pixel.x, pixel.y);
      }
    }

    return pixel;
  }

  public Pixel findOneFast(BufferedImage image, BufferedImage screen, Color colorToBypass, boolean bwMode,
      boolean convertImage) throws AWTException {
    
    if (colorToBypass == null)
      colorToBypass = Color.RED;
    
    if (convertImage) {
      image = convertToBW(image);
      screen = convertToBW(screen);
    }
    return _comparator.findImage(image, screen, colorToBypass);
  }
  
  public Pixel scanOneFast(String filename, Rectangle area, boolean click, Color colorToBypass, boolean bwMode,
      boolean convertImage) throws AWTException, RobotInterruptedException, IOException {
    return scanOneFast(getImageData(filename), area, click, colorToBypass, bwMode, convertImage);
  }

  public Pixel scanOneFast(String filename, Rectangle area, boolean click, Color colorToBypass, boolean bwMode)
      throws AWTException, RobotInterruptedException, IOException {
    return scanOneFast(getImageData(filename), area, click, colorToBypass, bwMode, false);
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

  public void captureGameAreaDT(String prefixName) {
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd  HH-mm-ss-SSS");
    String date = sdf.format(Calendar.getInstance().getTime());
    String filename = prefixName + " " + date + ".jpg";
    captureGameArea(filename);
  }

  public void captureGameAreaDT() {
    captureGameAreaDT("popup");
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

      Pixel p = scanner
          .scanOneFast("C:\\prj\\repos\\Mickey2\\images\\journey.bmp", null, false, Color.RED, true, false);
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

  public boolean locateGameArea(ImageData topLeft, ImageData bottomRight, boolean scroll) throws AWTException,
      IOException, RobotInterruptedException {
    return _gameLocator.locateGameArea(topLeft, bottomRight, scroll);
  }

  public boolean isPixelInArea(Pixel p, Rectangle area) {
    return (p.x >= area.x && p.x <= (area.x + area.getWidth()) && p.y >= area.y && p.y <= (area.y + area.getHeight()));
  }

  public Pixel findMatch(BufferedImage template, BufferedImage image, Color colorToBypass) {
    return _matcher.findMatch(template, image, colorToBypass);
  }

  // Pixel pixel = _matcher.findMatch(fbID.toBufferedImage(), fbAREA.toBufferedImage(), null);

  public void handleFBMessages(boolean close) throws AWTException, RobotInterruptedException, IOException {
    // FB messages
    // writeAreaTS(_fbArea, "fbarea.bmp");
    boolean found = false;
    do {
      Pixel p = scanOneFast("lib/fbMessageBlue.bmp", _fbArea, false);
      if (p == null)
        p = scanOneFast("lib/fbMessageWhite.bmp", _fbArea, false);

      found = p != null;
      if (found) {
        if (close)
          _mouse.click(p.x + 29, p.y + 12);
        else
          _mouse.click(p.x - 80, p.y);
        _mouse.delay(200);
      }
    } while (found);
  }

  public Pixel getSafePoint() {
    return _safePoint;
  }

}
