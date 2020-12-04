package com.hybhub.util.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConcurrentSetBlockingQueueMultiThreadTest {

	private static class OfferGiver implements Callable<Boolean> {
		private BlockingQueue<UUID> queue;

		public OfferGiver(final BlockingQueue<UUID> queue) {
			this.queue = queue;
		}

		@Override
		public Boolean call() {
			for (int index = 0; index < 1000; index++) {
				queue.offer(UUID.randomUUID());
			}
			return Boolean.TRUE;
		}
	}

	private static class PutGiver implements Callable<Boolean> {
		private BlockingQueue<UUID> queue;

		public PutGiver(final BlockingQueue<UUID> queue) {
			this.queue = queue;
		}

		@Override
		public Boolean call() throws InterruptedException {
			for (int index = 0; index < 1000; index++) {
				queue.put(UUID.randomUUID());
			}
			return Boolean.TRUE;
		}
	}

	private static class Consumer implements Callable<Boolean> {
		private BlockingQueue<UUID> queue;

		public Consumer(final BlockingQueue<UUID> queue) {
			this.queue = queue;
		}

		@Override
		public Boolean call() throws InterruptedException {
			for (int index = 0; index < 8000; index++) {
				queue.take();
			}
			return Boolean.TRUE;
		}
	}

	@Test
	public void testTwoThreadsOfferTake() throws InterruptedException {
		// Arrange
		BlockingQueue<UUID> queue = new ConcurrentSetBlockingQueue<>();
		ExecutorService exec = Executors.newFixedThreadPool(16);

		// Act
		exec.invokeAll(
				(Collection<? extends Callable<Boolean>>) Stream
						.of(new OfferGiver(queue), new OfferGiver(queue), new OfferGiver(queue), new OfferGiver(queue),
								new OfferGiver(queue), new OfferGiver(queue), new OfferGiver(queue),
								new PutGiver(queue), new Consumer(queue))
						.collect(Collectors.toCollection(ArrayList::new)));

		exec.awaitTermination(5, TimeUnit.SECONDS);
		exec.shutdown();

		// Test
		Assertions.assertTrue(queue.isEmpty());
	}

	@Test
	public void testTenThreadsOffer() throws InterruptedException {
		// Arrange
		BlockingQueue<UUID> queue = new ConcurrentSetBlockingQueue<>();
		List<Callable<Object>> consumers = new ArrayList<>();
		for (int index = 0; index < 10; index++) {
			consumers.add(() -> {
				for (int index2 = 0; index2 < 15; index2++) {
					queue.offer(UUID.randomUUID());
				}
				return Boolean.TRUE;
			});
		}
		ExecutorService exec = Executors.newFixedThreadPool(5);

		// Act
		exec.invokeAll(consumers);
		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.SECONDS);

		// Test
		Assertions.assertEquals(150, queue.size());
		Assertions.assertFalse(queue.isEmpty());
	}

	@Test
	public void testOfferWithTimeout() throws InterruptedException {
		// Arrange
		BlockingQueue<UUID> queue = new ConcurrentSetBlockingQueue<>(1);
		List<Callable<Object>> consumers = new ArrayList<>();
		for (int index = 0; index < 20; index++) {
			consumers.add(() -> queue.offer(UUID.randomUUID(), 10, TimeUnit.SECONDS)
					&& queue.offer(UUID.randomUUID(), 10, TimeUnit.SECONDS));
		}
		consumers.add(() -> {
			IntStream.range(0, 40).parallel().forEach((i) -> {
				try {
					queue.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});
			return Boolean.TRUE;
		});
		ExecutorService exec = Executors.newFixedThreadPool(21);

		// Act
		exec.invokeAll(consumers);
		exec.shutdown();
		exec.awaitTermination(1, TimeUnit.SECONDS);

		// Test
		Assertions.assertEquals(0, queue.size());
		Assertions.assertTrue(queue.isEmpty());
	}

	@Test
	public void testFiftyThreadsPutPoll() throws InterruptedException {
		Logger log = Logger.getLogger(getClass().getName());
		// Arrange
		BlockingQueue<UUID> queue = new ConcurrentSetBlockingQueue<>(30);
		List<Callable<Object>> consumers = new ArrayList<>();
		// TODO: 20 made the test hang forever
		for (int index = 0; index < 15; index++) {
			consumers.add(() -> {
				queue.put(UUID.randomUUID());
				queue.put(UUID.randomUUID());
				return Boolean.TRUE;
			});
			consumers.add(queue::poll);
			consumers.add(queue::poll);
		}
		log.info("Creating executor");
		ExecutorService exec = Executors.newFixedThreadPool(50);
		log.info("Invoking all consumers");
		// Act
		final List<Future<Object>> results = exec.invokeAll(consumers);
		log.info("Shutting down executor");
		exec.shutdown();
		log.info("Awaiting executor termination");
		exec.awaitTermination(1, TimeUnit.SECONDS);

		// Test
		final long failedPolls = results.stream().filter((o) -> {
			try {
				return o.get() == null;
			} catch (Exception e) {
				return true;
			}
		}).count();
		Assertions.assertTrue((failedPolls == 0) == queue.isEmpty());
		Assertions.assertEquals(queue.size(), failedPolls);
	}

}
