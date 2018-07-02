package com.horowitz.commons;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class MouseRobot {
  public static final int NORMAL = 0;
  public static final int SLOW = 1;
  public static final int VERY_SLOW = 2;

  private int _mode = NORMAL;

  private int _clickDelay;
  private int _doubleClickDelay;
  private int _delayBetweenActions;
  private Point _position;
  private Point _lastMousePos;
  private boolean pressed = false;
  private PropertyChangeSupport _support;

  private Robot getInstance() {
    try {
      Robot robot = new Robot();
      robot.setAutoWaitForIdle(true);
      robot.setAutoDelay(30);
      return robot;
    } catch (AWTException e) {
      e.printStackTrace();
      return null;
    }
  }

  public MouseRobot(int clickDelay, int doubleClickDelay, int delayBetweenActions) throws AWTException {
    _clickDelay = clickDelay;
    _doubleClickDelay = doubleClickDelay;
    _delayBetweenActions = delayBetweenActions;
    _support = new PropertyChangeSupport(this);
  }

  public MouseRobot() throws AWTException {
    this(0, 0, 0);// 40 30 200
  }

  public void mouseMove(Pixel p) {
    mouseMove(p.x, p.y);
  }

  public void mouseMove(int x, int y) {
    getInstance().mouseMove(x, y);
    saveCurrentPosition();
  }

  public void click() {
    Robot robot = getInstance();
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    robot.delay(_clickDelay);
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    robot.delay(_delayBetweenActions);
    pressed = false;
  }

  public void click(Pixel p) {
    click(p.x, p.y);
  }

  public void click(int x, int y) {
    mouseMove(x, y);
    click();
  }

  public void drag2(int x1, int y1, int x2, int y2) throws RobotInterruptedException {
    drag2(x1, y1, x2, y2, true, true);
  }

  public void drag2(int x1, int y1, int x2, int y2, boolean extraMove, boolean simBreaks)
      throws RobotInterruptedException {
    Robot robot = getInstance();
    mouseMove(x1, y1);
    saveCurrentPosition();
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    pressed = true;
    delay(400);
    // checkUserMovement();
    int x = x1;
    int y = y1;
    int maxStep = 10;
    int step = maxStep;

    if (x1 != x2) {// move horizontally with high precision
      int a = x2 - x1;
      int d = Math.abs(a);
      step = d <= maxStep ? d : maxStep;
      double turns = d / step;
      // case 1 - a > 0 => moving east
      if (extraMove) {
        x = x + (a > 0 ? 6 : -7);
        mouseMove(x, y);
        delay(200);
      }
      for (int i = 0; i < turns; i++) {
        x = x + (a > 0 ? step : -step);
        mouseMove(x, y);
        delay(70);
      }
      int rest = d % step;
      x = x + (a > 0 ? rest : -rest);
      mouseMove(x, y);
      delay(70);

      if (simBreaks) {
        // move a bit farther and then back
        x = x + (a > 0 ? 3 : -3);
        mouseMove(x, y);
        delay(470);
        x = x - (a > 0 ? 3 : -3);
        mouseMove(x, y);
        delay(1000);
      }
      delay(270);
    }

    if (y1 != y2) {// move vertically with high precision
      int b = y2 - y1;
      int d = Math.abs(b);
      step = d <= maxStep ? d : maxStep;
      double turns = d / step;
      for (int i = 0; i < turns; i++) {
        y = y + (b > 0 ? step : -step);
        mouseMove(x, y);
        delay(70);
      }
      int rest = d % step;
      y = y + (b > 0 ? rest : -rest);
      mouseMove(x, y);
      delay(70);

      if (simBreaks) {
        // move a bit farther and then back
        y = y + (b > 0 ? 5 : -5);
        mouseMove(x, y);
        delay(470);
        y = y - (b > 0 ? 5 : -5);
        mouseMove(x, y);
        delay(270);
      }
      delay(270);

    }

    delay(200);
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    pressed = false;
    saveCurrentPosition();
  }
  
  public void drag4(int x1, int y1, int x2, int y2, boolean extraMove, boolean simBreaks)
		  throws RobotInterruptedException {
	  Robot robot = getInstance();
	  mouseMove(x1, y1);
	  saveCurrentPosition();
	  robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
	  pressed = true;
	  delay(400);
	  // checkUserMovement();
	  int x = x1;
	  int y = y1;
	  int maxStep = 5;
	  int step = maxStep;
	  
	  if (y1 != y2) {// move vertically with high precision
		  int b = y2 - y1;
		  int d = Math.abs(b);
		  step = d <= maxStep ? d : maxStep;
		  double turns = d / step;
      if (extraMove) {
        y = y + (b > 0 ? 7 : -8);
        mouseMove(x, y);
        delay(200);
      }

		  for (int i = 0; i < turns; i++) {
			  y = y + (b > 0 ? step : -step);
			  mouseMove(x, y);
			  delay(70);
		  }
		  int rest = d % step;
		  y = y + (b > 0 ? rest : -rest);
		  mouseMove(x, y);
		  delay(70);
		  
		  if (simBreaks) {
			  // move a bit farther and then back
			  y = y + (b > 0 ? 3 : -3);
			  mouseMove(x, y);
			  delay(470);
			  y = y - (b > 0 ? 3 : -3);
			  mouseMove(x, y);
			  delay(270);
		  }
		  delay(270);
		  
	  }
	  if (x1 != x2) {// move horizontally with high precision
		  int a = x2 - x1;
		  int d = Math.abs(a);
		  step = d <= maxStep ? d : maxStep;
		  double turns = d / step;
		  // case 1 - a > 0 => moving east
//		  if (extraMove) {
//			  x = x + (a > 0 ? 7 : -8);
//			  mouseMove(x, y);
//			  delay(200);
//		  }
		  for (int i = 0; i < turns; i++) {
			  x = x + (a > 0 ? step : -step);
			  mouseMove(x, y);
			  delay(70);
		  }
		  int rest = d % step;
		  x = x + (a > 0 ? rest : -rest);
		  mouseMove(x, y);
		  delay(70);
		  
		  if (simBreaks) {
			  // move a bit farther and then back
			  x = x + (a > 0 ? 3 : -3);
			  mouseMove(x, y);
			  delay(470);
			  x = x - (a > 0 ? 3 : -3);
			  mouseMove(x, y);
			  delay(500);
		  }
		  delay(270);
	  }

	  robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	  pressed = false;
	  saveCurrentPosition();
  }

  public void dragFast(int x1, int y1, int x2, int y2, boolean extraMove, boolean simBreaks)
      throws RobotInterruptedException {
    Robot robot = getInstance();
    mouseMove(x1, y1);
    saveCurrentPosition();
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    pressed = true;
    delay(200);
    // checkUserMovement();
    int x = x1;
    int y = y1;
    int maxStep = 10;
    int step = maxStep;
    
    if (y1 != y2) {// move vertically with high precision
      int b = y2 - y1;
      int d = Math.abs(b);
      step = d <= maxStep ? d : maxStep;
      double turns = d / step;
      if (extraMove) {
        y = y + (b > 0 ? 7 : -8);
        mouseMove(x, y);
        delay(100);
      }
      
      for (int i = 0; i < turns; i++) {
        y = y + (b > 0 ? step : -step);
        mouseMove(x, y);
        delay(40);
      }
      int rest = d % step;
      y = y + (b > 0 ? rest : -rest);
      mouseMove(x, y);
      delay(40);
      
      if (simBreaks) {
        // move a bit farther and then back
        y = y + (b > 0 ? 3 : -3);
        mouseMove(x, y);
        delay(470);
        y = y - (b > 0 ? 3 : -3);
        mouseMove(x, y);
        delay(270);
      }
      delay(270);
      
    }
    if (x1 != x2) {// move horizontally with high precision
      int a = x2 - x1;
      int d = Math.abs(a);
      step = d <= maxStep ? d : maxStep;
      double turns = d / step;
      // case 1 - a > 0 => moving east
//		  if (extraMove) {
//			  x = x + (a > 0 ? 7 : -8);
//			  mouseMove(x, y);
//			  delay(200);
//		  }
      for (int i = 0; i < turns; i++) {
        x = x + (a > 0 ? step : -step);
        mouseMove(x, y);
        delay(40);
      }
      int rest = d % step;
      x = x + (a > 0 ? rest : -rest);
      mouseMove(x, y);
      delay(40);
      
      if (simBreaks) {
        // move a bit farther and then back
        x = x + (a > 0 ? 3 : -3);
        mouseMove(x, y);
        delay(470);
        x = x - (a > 0 ? 3 : -3);
        mouseMove(x, y);
        delay(500);
      }
      delay(170);
    }
    
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    pressed = false;
    saveCurrentPosition();
  }
  
  /**
   * This drag supports minimal dragging. Some environments don't start dragging
   * if mouse moved only 6-8 pixels. To prevent this drag starts dragging in the
   * opposite direction, then return back + the desired distance
   * 
   * @param x1
   * @param y1
   * @param x2
   * @param y2
   * @throws RobotInterruptedException
   */
  public void drag3(final int x1, final int y1, final int x2, final int y2) throws RobotInterruptedException {
    int oppositeStep = 12;

    if (x1 != x2) {// move horizontally with high precision
      int a = x2 - x1;
      int d = Math.abs(a);
      if (d < 11) {
        // too small distance
        int x1New = x1 + ((a > 0) ? oppositeStep : -oppositeStep);
        // move back
        drag2(x1New, y1, x1, y1, false, false);
        // then
        int x2New2 = x1 + ((a > 0) ? (oppositeStep + d) : -(oppositeStep + d));
        drag2(x1, y1, x2New2, y1, false, false);
      } else {
        // normal drag
        drag2(x1, y1, x2, y2, false, false);
      }
    }

    // ////////////////////////////////////////

    if (y1 != y2) {// move horizontally with high precision
      int a = y2 - y1;
      int d = Math.abs(a);
      if (d < 11) {
        // too small distance
        int y1New = y1 + ((a > 0) ? oppositeStep : -oppositeStep);
        // move back
        drag2(x1, y1New, x1, y1, false, false);
        // then
        int y2New2 = y1 + ((a > 0) ? (oppositeStep + d) : -(oppositeStep + d));
        drag2(x1, y1, x1, y2New2, false, false);
      } else {
        // normal drag
        drag2(x1, y1, x2, y2, false, false);
      }
    }

  }

  public void drag(int x1, int y1, int x2, int y2) throws RobotInterruptedException {
    Robot robot = getInstance();
    mouseMove(x1, y1);
    saveCurrentPosition();
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    pressed = true;
    delay(400);
    // checkUserMovement();
    int x = x1;
    int y = y1;
    int maxStep = 20;
    int step = maxStep;

    if (x1 != x2) {// move horizontally with high precision
      int a = x2 - x1;
      int d = Math.abs(a);
      step = d <= maxStep ? d : maxStep;
      double turns = d / step;
      // case 1 - a > 0 => moving east

      x = x + (a > 0 ? 7 : -7);
      mouseMove(x, y);
      delay(200);

      for (int i = 0; i < turns; i++) {
        x = x + (a > 0 ? step : -step);
        mouseMove(x, y);
        delay(100);
      }
      int rest = d % step;
      x = x + (a > 0 ? rest : -rest);
      mouseMove(x, y);
      delay(100);
    }

    if (y1 != y2) {// move vertically
      int b = y2 - y1;
      step = Math.abs(b) <= maxStep ? Math.abs(b) : maxStep;
      b = b / step;
      for (int i = 0; i < Math.abs(b); i++) {
        y = y + (b > 0 ? step : b == 0 ? 0 : -step);
        mouseMove(x, y);
        delay(100);
        // checkUserMovement();
      }
    }
    delay(200);
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    pressed = false;
    saveCurrentPosition();
  }

  public void doubleClick(int x, int y) {
    mouseMove(x, y);
    doubleClick();
  }

  public void doubleClick() {
    Robot robot = getInstance();
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    robot.delay(_clickDelay);
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    robot.delay(_doubleClickDelay);
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    robot.delay(_clickDelay);
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    pressed = false;
    robot.delay(_delayBetweenActions);
  }

  public void rightClick() {
    Robot robot = getInstance();
    robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
    robot.delay(_clickDelay);
    robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    pressed = false;
    robot.delay(_delayBetweenActions);
  }

  public void rightClick(int x, int y) {
    mouseMove(x, y);
    click();

  }

  public void delay(int ms) throws RobotInterruptedException {
    delay(ms, true);
  }

  public void delay(int ms, boolean checkUserMovement) throws RobotInterruptedException {
    if (checkUserMovement)
      saveCurrentPosition();
    if (ms > 10000) {
      int turns = ms / 10000;
      int remainder = ms - (turns * 10000);
      for (int i = 1; i <= turns; i++) {
        getInstance().delay(10000);
        _support.firePropertyChange("DELAY", 0, i * 10000);
        if (checkUserMovement)
          checkUserMovement();
      }
      if (remainder > 0) {
        getInstance().delay(remainder);
        _support.firePropertyChange("DELAY", 0, ms);
      }

    } else {
      getInstance().delay(ms);
      // _support.firePropertyChange("DELAY", 0, ms);
    }
    if (checkUserMovement)
      checkUserMovement();
  }

  public void savePosition() {
    _position = MouseInfo.getPointerInfo().getLocation();
  }

  public void restorePosition() {
    mouseMove(_position.x, _position.y);
  }

  public void checkUserMovement() throws RobotInterruptedException {
    Point currentPos = MouseInfo.getPointerInfo().getLocation();
    if (Math.abs(_lastMousePos.x - currentPos.x) > 4 || Math.abs(_lastMousePos.y - currentPos.y) > 4) {
    	
    	if (pressed) {
          getInstance().mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
          pressed = false;
    	}
      // mouseMove(_lastMousePos.x, _lastMousePos.y);
      saveCurrentPosition();
      throw new RobotInterruptedException();
    }
    saveCurrentPosition();
  }

  public void saveCurrentPosition() {
    _lastMousePos = MouseInfo.getPointerInfo().getLocation();
  }

  public Point getCurrentPosition() {
    return MouseInfo.getPointerInfo().getLocation();
  }

  public void wheelDown(int notches) {
    try {
      new Robot().mouseWheel(notches);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  public int getClickDelay() {
    return _clickDelay;
  }

  public void setClickDelay(int clickDelay) {
    _clickDelay = clickDelay;
  }

  public int getDoubleClickDelay() {
    return _doubleClickDelay;
  }

  public void setDoubleClickDelay(int doubleClickDelay) {
    _doubleClickDelay = doubleClickDelay;
  }

  public int getDelayBetweenActions() {
    return _delayBetweenActions;
  }

  public void setDelayBetweenActions(int delayBetweenActions) {
    _delayBetweenActions = delayBetweenActions;
  }

  public void hold(int msec) {
    Robot robot = getInstance();
    robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
    pressed = true;
    robot.delay(msec);
    robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    pressed = false;
    robot.delay(_delayBetweenActions);
  }

  public Point getPosition() {
    return MouseInfo.getPointerInfo().getLocation();
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    _support.addPropertyChangeListener(listener);
  }

  public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    _support.addPropertyChangeListener(propertyName, listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    _support.removePropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
    _support.removePropertyChangeListener(propertyName, listener);
  }

  public int getMode() {
    return _mode;
  }

  public void setMode(int mode) {
    _mode = mode;
  }

}
