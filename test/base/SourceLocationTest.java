package base;

import ee.ttu.pld.apricot.DetectionException;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Anton Chepurov
 */
public class SourceLocationTest {

	private File sourceFile = new File("FileName.txt");
	private File sourceFile2 = new File("another.txt");

	@Test(expected = IllegalArgumentException.class)
	public void rejectNullSourceFile() {
		new SourceLocation(null, Arrays.asList(2, 1, 2431));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectNullVhdlLines() {
		new SourceLocation(sourceFile, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectEmptyVhdlLines() {
		new SourceLocation(sourceFile, Collections.<Integer>emptyList());
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectNegativeVhdlLine() {
		new SourceLocation(sourceFile, Arrays.asList(10, 15, -16));
	}

	@Test
	public void additionSkipsNulls() throws DetectionException {
		SourceLocation firstSource = new SourceLocation(sourceFile, Arrays.asList(10, 15, 16));
		assertSame(firstSource, firstSource.addSource(null));
	}

	@Test
	public void additionPreservesImmutability() throws DetectionException {
		SourceLocation firstSource = new SourceLocation(sourceFile, Arrays.asList(10, 15, 16));
		SourceLocation secondSource = new SourceLocation(sourceFile, Arrays.asList(10));

		SourceLocation sum = firstSource.addSource(secondSource);
		assertTrue(sum != firstSource);
		assertNotSame(sum, firstSource);
		assertEquals(firstSource.toString(), sum.toString());
	}

	@Test
	public void correctAddition() throws DetectionException {
		SourceLocation firstSource = new SourceLocation(sourceFile, Arrays.asList(10, 15, 16));
		SourceLocation secondSource = new SourceLocation(sourceFile, Arrays.asList(12, 3));

		SourceLocation sum = firstSource.addSource(secondSource);
		assertEquals("FileName.txt 3, 10, 12, 15, 16", sum.toString());

		SourceLocation thirdSource = new SourceLocation(sourceFile2, Arrays.asList(111, 91));
		sum = sum.addSource(thirdSource);
		assertEquals("another.txt 91, 111; FileName.txt 3, 10, 12, 15, 16", sum.toString());
	}

	@Test
	public void correctFirstLine() throws HLDDException {
		assertEquals(new Integer(10), new SourceLocation(sourceFile, Arrays.asList(10, 15, 16)).getFirstLine());
		assertEquals(new Integer(10), new SourceLocation(sourceFile, Arrays.asList(16, 10, 15)).getFirstLine());
		assertEquals(new Integer(15626), new SourceLocation(sourceFile, Arrays.asList(15626)).getFirstLine());
		assertEquals(new Integer(1), new SourceLocation(sourceFile, Arrays.asList(1, 15, 124124)).getFirstLine());
	}

	@Test
	public void correctToString() {
		File sourceFile = new File("FileName.txt");
		assertEquals("FileName.txt", sourceFile.getName());
		assertEquals("FileName.txt 10, 15, 16", new SourceLocation(sourceFile, Arrays.asList(10, 15, 16)).toString());
		assertEquals("FileName.txt 10", new SourceLocation(sourceFile, Arrays.asList(10)).toString());
	}

	@Test
	public void correctCreateFrom() {
		SourceLocation firstSource = new SourceLocation(sourceFile, Arrays.asList(10, 15, 16));
		SourceLocation secondSource = new SourceLocation(sourceFile, Arrays.asList(12, 3));
		SourceLocation thirdSource = new SourceLocation(sourceFile, Arrays.asList(100010011));

		SourceLocation sum = SourceLocation.createFrom(Arrays.asList(firstSource));
		assertEquals(firstSource.toString(), sum.toString());

		sum = SourceLocation.createFrom(Arrays.asList(firstSource, secondSource));
		assertEquals("FileName.txt 3, 10, 12, 15, 16", sum.toString());

		sum = SourceLocation.createFrom(Arrays.asList(firstSource, secondSource, thirdSource));
		assertEquals("FileName.txt 3, 10, 12, 15, 16, 100010011", sum.toString());
	}
}
