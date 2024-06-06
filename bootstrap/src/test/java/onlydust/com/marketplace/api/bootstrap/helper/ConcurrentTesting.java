package onlydust.com.marketplace.api.bootstrap.helper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentTesting {

    @FunctionalInterface
    public interface Runnable {
        public abstract void run(int threadId);
    }

    public static void runConcurrently(int numberOfThreads, Runnable task) throws InterruptedException {
        final ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        for (int t = 0; t < numberOfThreads; t++) {
            final int threadId = t;
            service.execute(() -> {
                try {
                    startLatch.await();
                    task.run(threadId);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        endLatch.await();
    }
}
