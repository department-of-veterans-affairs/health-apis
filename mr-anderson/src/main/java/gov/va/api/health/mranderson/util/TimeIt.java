package gov.va.api.health.mranderson.util;

import java.util.concurrent.Callable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

/** Utility class to log execution time of function calls. */
@Slf4j
public class TimeIt {

  /** Utility method to log execution time of method calls. */
  @SneakyThrows
  public static <T> T logTime(Callable<T> task, String taskName) {
    if (task == null) {
      return null;
    }
    if (taskName == null) {
      taskName = "unset";
    }
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    T call = task.call();
    stopWatch.stop();
    log.info("{} took: {} seconds", taskName, stopWatch.getLastTaskInfo().getTimeSeconds());
    return call;
  }
}
