package com.hybhub.util.concurrent;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConcurrentSetQueueUnitTest {

	@Test
	public void testFullCapacityOffer() throws InterruptedException {
		//Arrange
		final Queue<Integer> queue = new ConcurrentSetBlockingQueue<>(1);

		//Act
		final boolean acceptedOffer = queue.offer(1);
		final boolean rejectedOffer1 = queue.offer(2);
		final boolean rejectedOffer2 = queue.offer(2);
		boolean rejectedException = false;
		try{
			queue.add(2);
		}
		catch (IllegalStateException ex){
			rejectedException = true;
		}

		//Test
		Assertions.assertTrue(acceptedOffer);
		Assertions.assertFalse(rejectedOffer1);
		Assertions.assertFalse(rejectedOffer2);
		Assertions.assertTrue(rejectedException);
	}

	@Test
	public void testOfferThenPoll(){
		//Arrange
		final Queue<String> queue = new ConcurrentSetBlockingQueue<>();
		final List<String> dataSet = Arrays.asList("1","3","6","10","4");

		//Act
		dataSet.forEach(queue::offer);

		//Test
		for(String s : dataSet){
			Assertions.assertEquals(queue.poll(), s, "Polled String is incorrect");
		}
		Assertions.assertTrue(queue.isEmpty());
	}

	@Test
	public void testOfferDuplicates(){
		//Arrange
		final Queue<Integer> queue = new ConcurrentSetBlockingQueue<>();

		//Act
		final boolean firstOffer = queue.offer(1);
		final boolean secondOffer = queue.offer(1);

		//Test
		Assertions.assertTrue(firstOffer);
		Assertions.assertFalse(secondOffer);
	}
	
	@Test
	public void testPeek(){
		//Arrange
		final Queue<Integer> queue = new ConcurrentSetBlockingQueue<>();

		//Act
		final Integer shouldBeNull = queue.peek();
		final boolean shouldBeTrue = queue.offer(1);
		final Integer shouldBeNonNull = queue.peek();
		final int shouldBeOne = queue.size();

		//Test
		Assertions.assertNull(shouldBeNull);
		Assertions.assertTrue(shouldBeTrue);
		Assertions.assertNotNull(shouldBeNonNull);
		Assertions.assertEquals(1, shouldBeOne);
	}

	@Test
	public void testRemove(){
		//Arrange
		final Queue<Integer> queue = new ConcurrentSetBlockingQueue<>();

		//Act
		Exception expectedException = null;
		try {
			queue.remove();
		}
		catch (NoSuchElementException noSuchElementException){
			expectedException = noSuchElementException;
		}
		final boolean addedFirst = queue.offer(3);
		final boolean addedSecond = queue.offer(1);
		final boolean addedThird = queue.offer(2);
		final Integer integerRemoved = queue.remove();
		final int finalQueueSize = queue.size();

		//Test
		Assertions.assertNotNull(expectedException);
		Assertions.assertTrue(addedFirst);
		Assertions.assertTrue(addedSecond);
		Assertions.assertTrue(addedThird);
		Assertions.assertEquals(3, integerRemoved);
		Assertions.assertEquals(finalQueueSize, 2);
	}

	@Test
	public void testElement(){
		//Arrange
		final Queue<Integer> queue = new ConcurrentSetBlockingQueue<>();

		//Act
		Exception expectedException = null;
		try {
			queue.element();
		}
		catch (NoSuchElementException noSuchElementException){
			expectedException = noSuchElementException;
		}
		final boolean addedFirst = queue.offer(4);
		final boolean addedSecond = queue.offer(1);
		final Integer shouldBeNonNull = queue.element();
		final int shouldBeTwo = queue.size();

		//Test
		Assertions.assertNotNull(expectedException);
		Assertions.assertTrue(addedFirst);
		Assertions.assertTrue(addedSecond);
		Assertions.assertNotNull(shouldBeNonNull);
		Assertions.assertEquals(2, shouldBeTwo);
	}
}
