package marcel.util.concurrent;

public interface AwaitProgressListener {

  /**
   * Listen for the progress of Threadmill
   * @param completedTasks the number of completed tasks
   * @param total the total number of tasks
   */
  void onProgress(int completedTasks, int total);

}
