package com.horowitz.commons;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageData implements Serializable {

  private static final long serialVersionUID = 1665414091139220640L;

  private String _filename;
  private String _name;

  private transient Map<Integer, Color[]> _colors;
  private transient Pixel[] _mask;
  private transient Color _colorToBypass = null;

  private Rectangle _defaultArea;

  private transient BufferedImage _image;
  private transient ImageComparator _comparator;
  private int _xOff = 0;

  private int _yOff = 0;

  public ImageData(String filename, Rectangle defaultArea, ImageComparator comparator, int xOff, int yOff)
      throws IOException {
    super();
    this._filename = filename;
    this._defaultArea = defaultArea;
    this._comparator = comparator;

    loadImage(filename);

    _xOff = xOff;
    _yOff = yOff;
  }

  private void loadImage(String filename) throws IOException {
    _image = ImageManager.loadImage(filename);
    ImageMask imageMask = new ImageMask(filename);
    _colorToBypass = lookForColorToBypass(filename);
    _mask = imageMask.getMask();
    _colors = imageMask.getColors();
  }

  private Color lookForColorToBypass(String name) {
    Color c = null;

    InputStream stream;
    try {
      stream = new FileInputStream(name + ".colorToBypass.txt");
    } catch (FileNotFoundException e1) {
      stream = ImageMask.class.getClassLoader().getResourceAsStream(name + ".colorToBypass.txt");
    } 
    
    if (stream != null) {
      DataInputStream reader = new DataInputStream(stream);
      BufferedReader br = new BufferedReader(new InputStreamReader(reader));
      try {
        String line = null;
        while ((line = br.readLine()) != null) {
          // System.err.println(line);
          line = line.trim();
          if (line.length() > 0) {
            String[] ss = line.split(",");
            int r = Integer.parseInt(ss[0].trim());
            int g = Integer.parseInt(ss[1].trim());
            int b = Integer.parseInt(ss[2].trim());

            c = new Color(r, g, b);
            break;
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      } finally {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return c;
  }

  public Pixel findImage() {
    return findImage((Rectangle) null);
  }

  public Pixel findImage(BufferedImage screen) {
    for (int i = 0; i <= (screen.getWidth() - _image.getWidth()); i++) {
      for (int j = 0; j <= (screen.getHeight() - _image.getHeight()); j++) {
        final BufferedImage subimage = screen.getSubimage(i, j, _image.getWidth(), _image.getHeight());
        writeImage(subimage, 201);
        if (_comparator.compare(_image, subimage, _colors, _mask)) {
          return new Pixel(i, j);
        }
      }
    }
    return null;
  }

  public Pixel findImageExclude(BufferedImage screen) {
    return _comparator.findImage(_image, screen, _colorToBypass);
  }

  private void writeImage(BufferedImage image, int n) {
    if (false)
      try {
        MyImageIO.write(image, "PNG", new File("subimage" + n + ".png"));
      } catch (IOException e) {
        e.printStackTrace();
      }
  }

  public Pixel findImage(Rectangle areaIn) {
    Rectangle area = areaIn != null ? areaIn : _defaultArea;
    Pixel p = null;

    try {
      BufferedImage screen = new Robot().createScreenCapture(area);
      if (_colorToBypass != null)
        p = findImageExclude(screen);
      else
        p = findImage(screen);

      if (p != null) {
        p.x += area.x;
        p.y += area.y;
        p.x += _xOff;
        p.y += _yOff;
      }
    } catch (AWTException e) {
      e.printStackTrace();
    }
    return p;
  }

  public int get_xOff() {
    return _xOff;
  }

  public void set_xOff(int _xOff) {
    this._xOff = _xOff;
  }

  public int get_yOff() {
    return _yOff;
  }

  public void set_yOff(int _yOff) {
    this._yOff = _yOff;
  }

  public String getName() {
    if (_name == null) {
      _name = _filename.substring(0, _filename.length() - 4);
      String[] ss = _name.split("/");
      _name = ss[ss.length - 1];
    }
    return _name;
  }

  public void setName(String _name) {
    this._name = _name;
  }

  public Rectangle getDefaultArea() {
    return _defaultArea;
  }

  public void setDefaultArea(Rectangle _defaultArea) {
    this._defaultArea = _defaultArea;
  }

  public String getFilename() {
    return _filename;
  }

  public BufferedImage getImage() {
    return _image;
  }

  public Pixel[] getMask() {
    return _mask;
  }

  public ImageComparator getComparator() {
    return _comparator;
  }

  public void setComparator(ImageComparator comparator) {
    _comparator = comparator;
  }

  public Color getColorToBypass() {
    return _colorToBypass;
  }

  public void setColorToBypass(Color colorToBypass) {
    _colorToBypass = colorToBypass;
  }

  public void setImage(BufferedImage image) {
    _image = image;
  }

}
