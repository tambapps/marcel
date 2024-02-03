package marcel.util.concurrent;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@RequiredArgsConstructor
class ThredmillContext {

  final ExecutorService executorService;
  final List<Future<?>> futures = new ArrayList<>();

}
