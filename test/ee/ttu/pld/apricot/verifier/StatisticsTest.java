package ee.ttu.pld.apricot.verifier;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 31.05.2010
 * <br>Time: 15:55:17
 */
public class StatisticsTest {

	@Test public void checkCounters() {

		Statistics statistics = Statistics.createByteArrayStatistics();

		doSomeActions(statistics);

		assertEquals("Statistics: should count skipped files correctly.", 3, statistics.getSkippedCount());
		assertEquals("Statistics: should count failed files correctly.", 2, statistics.getFailedCount());
		assertEquals("Statistics: should count passed files correctly.", 5, statistics.getPassCount());
		assertEquals("Statistics: should count passed map files correctly.", 2, statistics.getMapPassCount());
		assertEquals("Statistics: should count failed map files correctly.", 1, statistics.getMapFailedCount());
	}

	@Test public void checkInfo() {
		String firstMessage = "First Message!!!";
		String secondMessage = "Second Message.";
		String failedMessage = "Failed file message goes here.";
		String nl = System.getProperty("line.separator");

		Statistics statistics = Statistics.createByteArrayStatistics();
		statistics.info(firstMessage);
		statistics.info(secondMessage);

		assertEquals("Statistics: should handle info messages correctly.", firstMessage + nl + secondMessage,
				statistics.getMessage());
		assertEquals("Statistics: should handle info messages correctly.", firstMessage + nl + secondMessage,
				statistics.getMessage());
		assertEquals("Statistics: should handle info messages correctly.", firstMessage + nl + secondMessage,
				statistics.getMessage());

		statistics.fail(failedMessage);
		assertEquals("Statistics: should handle info messages correctly.", firstMessage + nl + secondMessage + nl
				+ Statistics.CHANGED_FILE + failedMessage ,
				statistics.getMessage());
	}

	@Test public void correctToString() {

		Statistics statistics = Statistics.createByteArrayStatistics();
		doSomeActions(statistics);

		assertEquals("2 CHANGED files (out of 7). 3 SKIPPED.", statistics.toString());

		statistics = Statistics.createByteArrayStatistics();
		pass(statistics, 4);
		statistics.fail("");

		assertEquals("1 CHANGED file (out of 5).", statistics.toString());

		statistics = Statistics.createByteArrayStatistics();
		statistics.pass();
		assertEquals("All files PASSED. Total: 1 file.", statistics.toString());
		pass(statistics, 3);

		assertEquals("All files PASSED. Total: 4 files.", statistics.toString());

		skip(statistics);

		assertEquals("4 files PASSED. 3 SKIPPED.", statistics.toString());

		statistics = Statistics.createByteArrayStatistics();
		statistics.pass();
		skip(statistics);
		
		assertEquals("1 file PASSED. 3 SKIPPED.", statistics.toString());

		statistics = Statistics.createByteArrayStatistics();
		statistics.fail("");
		assertEquals("All files CHANGED. Total: 1 file.", statistics.toString());
		statistics.fail("");
		statistics.fail("");
		assertEquals("All files CHANGED. Total: 3 files.", statistics.toString());

		statistics = Statistics.createByteArrayStatistics();
		skip(statistics);
		assertEquals("All files SKIPPED. Total: 3 files.", statistics.toString());
		skip(statistics);
		assertEquals("All files SKIPPED. Total: 6 files.", statistics.toString());

	}
	@Test public void correctMapToString() {

		Statistics statistics = Statistics.createByteArrayStatistics();
		assertEquals("No MAP files.", statistics.mapToString());
		doSomeActions(statistics);
		assertEquals("1 CHANGED MAP file (out of 3).", statistics.mapToString());

		statistics = Statistics.createByteArrayStatistics();
		passMap(statistics, 4);
		assertEquals("All MAP files PASSED. Total: 4 files.", statistics.mapToString());

		statistics = Statistics.createByteArrayStatistics();
		statistics.passMap();
		assertEquals("All MAP files PASSED. Total: 1 file.", statistics.mapToString());
		passMap(statistics, 3);
		assertEquals("All MAP files PASSED. Total: 4 files.", statistics.mapToString());

		statistics = Statistics.createByteArrayStatistics();
		statistics.failMap("");
		assertEquals("All MAP files CHANGED. Total: 1 file.", statistics.mapToString());
		statistics.failMap("");
		statistics.failMap("");
		assertEquals("All MAP files CHANGED. Total: 3 files.", statistics.mapToString());
	}

	private void pass(Statistics statistics, int count) {
		for (int i = 0; i < count; i++) {
			statistics.pass();
		}
	}
	private void passMap(Statistics statistics, int count) {
		for (int i = 0; i < count; i++) {
			statistics.passMap();
		}
	}

	private void doSomeActions(Statistics statistics) {
		skip(statistics);
		statistics.fail("");
		statistics.fail("");
		pass(statistics, 5);
		statistics.passMap();
		statistics.passMap();
		statistics.failMap("");
	}

	private void skip(Statistics statistics) {
		statistics.skipped("");
		statistics.skippedNonExistent("");
		statistics.skippedWithoutVHDL("");
	}
}
