package org.ngrinder.tracking;

/**
 * Interface for logging adapter. You can hook up log4j, System.out or any other loggers you want.
 *
 * @author : Siddique Hameed
 * @version : 0.1
 */

public interface LoggingAdapter {

  public void logError(String errorMessage);

  public void logMessage(String message);

}
