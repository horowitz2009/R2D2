package com.horowitz.macros;

import java.awt.AWTException;
import java.io.IOException;

import com.horowitz.commons.GameErrorException;
import com.horowitz.commons.RobotInterruptedException;

public interface GameProtocol {

  public void update();

  public void execute() throws RobotInterruptedException, GameErrorException;

	public boolean preExecute() throws AWTException, IOException, RobotInterruptedException;
  
  public void interrupt();
  
  public void reset();
  
}
