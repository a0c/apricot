package base;

import ee.ttu.pld.apricot.DetectionException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 17.09.2010
 * <br>Time: 15:24:12
 */
public class SourceLocationTest {

	@Test (expected = DetectionException.class)
	public void exceptionForNullParameter() throws DetectionException {
		try {
			new SourceLocation(null);
		} catch (IllegalArgumentException e) {
			throw new DetectionException();
		}
	}
	@Test (expected = DetectionException.class)
	public void exceptionForEmptyParameter() throws DetectionException {
		try {
			new SourceLocation(Collections.<Integer>emptyList());
		} catch (IllegalArgumentException e) {
			throw new DetectionException();
		}
	}
	@Test (expected = DetectionException.class)
	public void exceptionForNegativeLine() throws DetectionException {
		try {
			new SourceLocation(Arrays.asList(10, 15, -16));
		} catch (IllegalArgumentException e) {
			throw new DetectionException();
		}
	}
	@Test public void additionSkipsNulls() throws DetectionException {
		SourceLocation firstSource = new SourceLocation(Arrays.asList(10, 15, 16));
		assertSame(firstSource, firstSource.addSource(null));
	}
	@Test public void additionPreservesImmutability() throws DetectionException {
		SourceLocation firstSource = new SourceLocation(Arrays.asList(10, 15, 16));
		SourceLocation secondSource = new SourceLocation(Arrays.asList(10));

		SourceLocation sum = firstSource.addSource(secondSource);
		assertTrue(sum != firstSource);
		assertNotSame(sum, firstSource);
		assertEquals(firstSource.toString(), sum.toString());
	}
	@Test public void correctAddition() throws DetectionException {
		SourceLocation firstSource = new SourceLocation(Arrays.asList(10, 15, 16));
		SourceLocation secondSource = new SourceLocation(Arrays.asList(12, 3));

		SourceLocation sum = firstSource.addSource(secondSource);
		assertEquals("3, 10, 12, 15, 16", sum.toString());
	}
	@Test public void correctFirstLine() {
		assertEquals(new Integer(10), new SourceLocation(Arrays.asList(10, 15, 16)).getFirstLine());
		assertEquals(new Integer(10), new SourceLocation(Arrays.asList(16, 10, 15)).getFirstLine());
		assertEquals(new Integer(15626), new SourceLocation(Arrays.asList(15626)).getFirstLine());
		assertEquals(new Integer(1), new SourceLocation(Arrays.asList(1, 15, 124124)).getFirstLine());
	}
	@Test public void correctToString() {
		assertEquals("10, 15, 16", new SourceLocation(Arrays.asList(10, 15, 16)).toString());
		assertEquals("10", new SourceLocation(Arrays.asList(10)).toString());
	}
	@Test public void correctCreateFrom() {
		SourceLocation firstSource = new SourceLocation(Arrays.asList(10, 15, 16));
		SourceLocation secondSource = new SourceLocation(Arrays.asList(12, 3));
		SourceLocation thirdSource = new SourceLocation(Arrays.asList(100010011));

		SourceLocation sum = SourceLocation.createFrom(Arrays.asList(firstSource));
		assertEquals(firstSource.toString(), sum.toString());

		sum = SourceLocation.createFrom(Arrays.asList(firstSource, secondSource));
		assertEquals("3, 10, 12, 15, 16", sum.toString());

		sum = SourceLocation.createFrom(Arrays.asList(firstSource, secondSource, thirdSource));
		assertEquals("3, 10, 12, 15, 16, 100010011", sum.toString());		
	}
}
