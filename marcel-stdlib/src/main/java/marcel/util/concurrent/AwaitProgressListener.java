package marcel.util.concurrent;

public interface AwaitProgressListener {

  /**
   * Listen for the progress of Threadmill
   * @param finishedTasks the number of finished (completed or failed) tasks
   * @param total the total number of tasks
   */
  void onProgress(int finishedTasks, int total);

}
