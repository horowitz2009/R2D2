package com.horowitz.macros;

import java.awt.AWTException;
import java.io.IOException;

import com.horowitz.commons.GameErrorException;
import com.horowitz.commons.RobotInterruptedException;

public abstract class AbstractGameProtocol implements GameProtocol {
  private boolean interrupted = false;
  private long lastActivity;
  private int sleep;

  public abstract void execute() throws RobotInterruptedException, GameErrorException;

  public void doProtocol() throws RobotInterruptedException, GameErrorException {
    long now = System.currentTimeMillis();
    if (sleep == 0 || now - lastActivity > sleep) {
      sleep = 0;
      if (!interrupted) {
        execute();
        lastActivity = System.currentTimeMillis();
      }
    }
  }

  /**
   * @return true if needs update. Then manager will call update method.
   */
  public boolean preExecute() throws AWTException, IOException, RobotInterruptedException {
    return false;
  }

  public void update() {

  }

  public void sleep(int mills) {
    sleep = mills;
  }

  @Override
  public void interrupt() {
    interrupted = true;
  }

  public boolean isInterrupted() {
    return interrupted;
  }

  public void setInterrupted(boolean interrupted) {
    this.interrupted = interrupted;
  }

  public boolean isNotInterrupted() {
    return !interrupted;
  }

  @Override
  public void reset() {
    interrupted = false;
  }
}
