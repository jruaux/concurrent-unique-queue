package com.hybhub.util.concurrent;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConcurrentSetBlockingQueueUnitTest {

	@Test
	public void testRemainingCapacity() throws InterruptedException {
		//Arrange
		final BlockingQueue<Integer> queue = new ConcurrentSetBlockingQueue<>(5);

		//Act
		final int initialCapacity = queue.remainingCapacity();
		queue.offer(1);
		final int capacityOne = queue.remainingCapacity();
		queue.offer(1);
		final int capacityOneRepeated = queue.remainingCapacity();
		queue.offer(2);
		final int capacityTwo = queue.remainingCapacity();
		queue.peek();
		final int capacityThree = queue.remainingCapacity();
		queue.put(3);
		final int capacityFour = queue.remainingCapacity();
		queue.remove(3);
		final int capacityFive = queue.remainingCapacity();
		queue.add(4);
		final int capacitySix = queue.remainingCapacity();
		queue.element();
		final int capacitySeven = queue.remainingCapacity();
		queue.remove();
		final int capacityEight = queue.remainingCapacity();
		queue.take();
		final int capacityNine = queue.remainingCapacity();
		queue.offer(5, 1, TimeUnit.SECONDS);
		final int capacityTen = queue.remainingCapacity();
		queue.drainTo(new ArrayList<>());
		final int capacityEleven = queue.remainingCapacity();

		//Test
		Assertions.assertEquals(5, initialCapacity);
		Assertions.assertEquals(4, capacityOne);
		Assertions.assertEquals(4, capacityOneRepeated);
		Assertions.assertEquals(3, capacityTwo);
		Assertions.assertEquals(3, capacityThree);
		Assertions.assertEquals(2, capacityFour);
		Assertions.assertEquals(3, capacityFive);
		Assertions.assertEquals(2, capacitySix);
		Assertions.assertEquals(2, capacitySeven);
		Assertions.assertEquals(3, capacityEight);
		Assertions.assertEquals(4, capacityNine);
		Assertions.assertEquals(3, capacityTen);
		Assertions.assertEquals(5, capacityEleven);
	}

}
