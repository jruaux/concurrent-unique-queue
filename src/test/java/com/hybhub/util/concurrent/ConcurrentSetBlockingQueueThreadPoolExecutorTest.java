package com.hybhub.util.concurrent;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConcurrentSetBlockingQueueThreadPoolExecutorTest {

    /**
     * IMPORTANT : The ThreadPoolExecutor won't add the first Runnable inside the queue, that's the reason why I execute it with Integer 1 three times first.
     * @throws InterruptedException
     */
    @Test
    public void testBLockingQueueInsideThreadPoolExecutor() throws InterruptedException {
        //Arrange
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1,1,20, TimeUnit.SECONDS,
                new ConcurrentSetBlockingQueue<>(100), new ThreadPoolExecutor.DiscardPolicy());

        //Act
        threadPoolExecutor.execute(new SimpleRunnable(1));
        final int shouldBeZero = threadPoolExecutor.getQueue().size();

        threadPoolExecutor.execute(new SimpleRunnable(1));
        final int shouldBeOne = threadPoolExecutor.getQueue().size();

        threadPoolExecutor.execute(new SimpleRunnable(1));
        final int shouldBeOneStill = threadPoolExecutor.getQueue().size();

        threadPoolExecutor.execute(new SimpleRunnable(2));
        final int shouldBeTwo = threadPoolExecutor.getQueue().size();

        threadPoolExecutor.execute(new SimpleRunnable(1));
        final int shouldBeTwoStill = threadPoolExecutor.getQueue().size();

        threadPoolExecutor.awaitTermination(4, TimeUnit.SECONDS);
        Thread.sleep(2_000);
        final long shouldBeThree = threadPoolExecutor.getCompletedTaskCount();

        //Test
        Assertions.assertEquals(0, shouldBeZero);
        Assertions.assertEquals(1, shouldBeOne);
        Assertions.assertEquals(1, shouldBeOneStill);
        Assertions.assertEquals(2, shouldBeTwo);
        Assertions.assertEquals(2, shouldBeTwoStill);
        Assertions.assertEquals(3, shouldBeThree);
    }

    private class SimpleRunnable implements Runnable {

        private Integer uid;

        public SimpleRunnable(final Integer uid) {
            this.uid = uid;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int hashCode() {
            return this.uid.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof SimpleRunnable){
                return this.uid.equals(((SimpleRunnable) obj).uid);
            }
            return false;
        }
    }

}
