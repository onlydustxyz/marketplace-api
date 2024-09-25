package onlydust.com.marketplace.api.helper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentTesting {

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

    public static void runConcurrently(java.lang.Runnable... taskPerThread) throws InterruptedException {
        final ExecutorService service = Executors.newFixedThreadPool(taskPerThread.length);
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch endLatch = new CountDownLatch(taskPerThread.length);

        for (final var task : taskPerThread) {
            service.execute(() -> {
                try {
                    startLatch.await();
                    task.run();
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

    @FunctionalInterface
    public interface Runnable {
        public abstract void run(int threadId);
    }

    public static class MutableObject<T> {
        private T value;

        public synchronized T getValue() {
            return value;
        }

        public synchronized void setValue(T value) {
            this.value = value;
        }

        public synchronized boolean nullOrSetValue(T value) {
            if (this.value == null) {
                this.value = value;
                return true;
            }
            return value.equals(this.value);
        }
    }
}
