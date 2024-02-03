package com.tambapps.marcel.threadmill;

import java.util.concurrent.ExecutorService;

/**
 * Interface to create a new executor
 */
public interface ExecutorServiceFactory {

  /**
   * Creates a new ExecutorService
   * @return the newly created executor service
   */
  ExecutorService newInstance();
}
