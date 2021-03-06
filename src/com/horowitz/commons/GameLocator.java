package com.horowitz.commons;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GameLocator {

  private final static Logger LOGGER = Logger.getLogger("MAIN");

  public final static boolean DEBUG = false;

  private ImageComparator _comparator;

  private Pixel _br = null;
  private Pixel _tl = null;

  private boolean _fullyOptimized;

  public GameLocator() {
    _comparator = new SimilarityImageComparator(0.04, 2000);

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    _tl = new Pixel(0, 0);
    _br = new Pixel(screenSize.width - 3, screenSize.height - 3);
    _fullyOptimized = false;
  }

  public boolean locateGameArea(ImageData topLeft, ImageData bottomRight, boolean scroll) throws AWTException,
      IOException, RobotInterruptedException {
    LOGGER.fine("Locating game area ... ");

    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int w = 200, h = 150;
    int wm = 20, hm = 20;

    List<Rectangle> rects = new ArrayList<Rectangle>(5);
    rects.add(new Rectangle(0, 80, 106, 256));
    rects.add(new Rectangle(0, 170, 50, 180));
    rects.add(new Rectangle(0, 0, 300, 550));
    int hturns = 1 + (screenSize.width - 600) / (w - wm);
    int vturns = 1 + (screenSize.height - 500) / (h - hm);
    int x = 0, y = 0;
    for (int row = 0; row < vturns; row++) {
      x = 0;
      for (int col = 0; col < hturns; col++) {
        Rectangle r = new Rectangle(x, y, w, h);
        rects.add(r);
        x += w - wm;
      }
      y += h - hm;
    }

    Rectangle[] areaTL = rects.toArray(new Rectangle[0]);

    // scroll a bit up
    boolean done = false;
    int turns = 0;
    do {
      turns++;
      {
        if (topLeft != null)
          _tl = locateImageCoords(topLeft, areaTL);
        else
          _tl = new Pixel(0, 0);
        
        if (_tl != null) {
          if (_tl.x < 0)
            _tl.x = 0;
          Rectangle[] areaBR = new Rectangle[] {
              new Rectangle(screenSize.width - 379, screenSize.height - 270, 113, 100),
              new Rectangle(_tl.x + 684, _tl.y + 543, 300, 100),
              new Rectangle(_tl.x + 684, _tl.y + 543, screenSize.width - 270 - 684, 100),
              new Rectangle(684, 607, screenSize.width - 270 - 684, screenSize.height - 607), new Rectangle(screenSize) };
          LOGGER.info("Found top left corner...");
          // good, we're still in the game
          _br = locateImageCoords(bottomRight, areaBR);
          if (_br != null) {
            LOGGER.info("Found bottom right corner...");
            done = true;
          } else {
            if (scroll) {
              LOGGER.info("Move a bit and try again...");
              Pixel p = new Pixel(_tl.x, _tl.y - 2);
              MouseRobot mouse = new MouseRobot();
              mouse.mouseMove(p.x, p.y);
              mouse.saveCurrentPosition();
              mouse.click();
              mouse.delay(200);
              Robot robot = new Robot();

              robot.keyPress(40);// arrow down
              mouse.delay(1000);
            } else {
              // uh oh
              return false;
            }
          }
        }
      }
    } while (!done && turns < 3);

    if (_br != null && _tl != null) {
      LOGGER.info("Top left    : " + _tl);
      LOGGER.info("Bottom Right: " + _br);
      return done;
    } else {
      _tl = new Pixel(0, 0);
      _br = new Pixel(screenSize.width - 1, screenSize.height - 61);
    }
    return false;
  }

  public Pixel locateImageCoords(ImageData imageData, Rectangle[] area) throws AWTException, IOException,
      RobotInterruptedException {
    int turn = 0;
    while (turn < area.length) {
      Pixel p = imageData.findImage(area[turn]);
      if (p != null) {
        return p;
      }
      turn++;
    }
    return null;
  }

  public boolean isOptimized() {
    return _fullyOptimized && _br != null && _tl != null;
  }

  private List<Pixel> findEdge(final BufferedImage targetImage, final BufferedImage area, ImageComparator comparator,
      Map<Integer, Color[]> colors, Pixel[] indices) {
    if (DEBUG)
      try {
        MyImageIO.write(area, "PNG", new File("C:/area.png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
    List<Pixel> result = new ArrayList<Pixel>(8);
    for (int i = 0; i < (area.getWidth() - targetImage.getWidth()); i++) {
      for (int j = 0; j < (area.getHeight() - targetImage.getHeight()); j++) {
        final BufferedImage subimage = area.getSubimage(i, j, targetImage.getWidth(), targetImage.getHeight());
        if (DEBUG)
          try {
            MyImageIO.write(subimage, "PNG", new File("C:/subimage.png"));
          } catch (IOException e) {
            e.printStackTrace();
          }
        if (comparator.compare(targetImage, subimage, colors, indices)) {
          result.add(new Pixel(i, j));
          if (result.size() > 0) {// increase in case of trouble
            break;
          }
        }
      }
    }
    return result;
  }

  public Pixel getBottomRight() {
    return _br;
  }

  public Pixel getTopLeft() {
    return _tl;
  }

  public void setTopLeft(Pixel tl) {
    _tl = tl;
  }

}
