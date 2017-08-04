package com.horowitz.macros;

import java.awt.AWTException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.horowitz.commons.GameErrorException;
import com.horowitz.commons.MouseRobot;
import com.horowitz.commons.RobotInterruptedException;

public class TaskManager {
  private final static Logger LOGGER = Logger.getLogger("MAIN");
  private List<Task> tasks;
  private boolean stopThread;
  private MouseRobot mouse;

  public TaskManager(MouseRobot mouse) {
    this.mouse = mouse;
    tasks = new ArrayList<Task>();
  }
  
  public void addTask(Task task) {
    tasks.add(task);
  }
  

  public void updateAll() {
    for (Task task : tasks) {
      task.getProtocol().reset();
      if (task.isEnabled())
        task.update();
    }

  }

  public void executeAll() throws RobotInterruptedException, GameErrorException {
    LOGGER.info(tasks.size() + " tasks...");
    int i = 0;
    for (Task task : tasks) {
      i++;
      if (task.isEnabled() && !stopThread) {
        try {
          mouse.checkUserMovement();
          task.preExecute();
          mouse.checkUserMovement();
          if (!stopThread) {
            //LOGGER.info(i + ". " + task.getName() + " START");
            task.execute();
            //LOGGER.info(i + ". " + task.getName() + " END");
          }
        } catch (AWTException e) {
          LOGGER.info("FAILED TO execute task: " + task.getName());
        } catch (IOException e) {
          LOGGER.info("FAILED TO execute task: " + task.getName());
        }
      }
    }

  }
}
