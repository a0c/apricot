package ee.ttu.pld.apricot.verifier;

import base.hldd.structure.models.BehModel;
import io.QuietCloser;
import ui.ConverterSettings;
import ui.utils.ConvertingWorker;

import java.io.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.concurrent.ExecutionException;

/**
 * @author Anton Chepurov
 */
public class AutomaticConverterVerifier {

	public void verify(Statistics statistics) {

		Collection<Design> designs = DesignCreator.create(statistics);

		for (Design design : designs) {
			boolean isDeleted = design.tryDeletingNewDesignDir();
			if (!isDeleted) {
				System.out.printf("### ERROR ###   Could not remove VERIF dirs");
				return;
			}
		}

		for (Design design : designs) {
			verify(design, statistics);
		}

		statistics.info("\n" + statistics + " " + statistics.mapToString());
	}

	private void verify(Design design, Statistics statistics) {
		File hlddFile = design.getHlddFile();
		ConverterSettings settings = design.getSettings();

		ByteArrayOutputStream modelStream = new ByteArrayOutputStream();
		ByteArrayOutputStream mapFileStream = new ByteArrayOutputStream();
		settings.setMapFileStream(mapFileStream);
		//todo: check different parsers...

		String hlddFilePath = hlddFile.getAbsolutePath();
		try {
			// Convert
			BehModel model = ConvertingWorker.convertAndWait(settings);
			// Compare
			model.toFile(modelStream, null, settings);
			if (areEqual(new FileInputStream(hlddFile), new ByteArrayInputStream(modelStream.toByteArray()))) {
				statistics.pass();
			} else {
				design.createNewDesignDir();
				model.toFile(new FileOutputStream(design.getNewDesignFile()), null, settings);
				statistics.fail(hlddFilePath);
			}
			// Compare MAP file
			File mapFile = settings.getMapFile();
			if (mapFile != null && mapFile.exists()) {
				if (areEqual(new FileInputStream(mapFile), new ByteArrayInputStream(mapFileStream.toByteArray()))) {
					statistics.passMap();
				} else {
					design.createNewDesignDir();
					writeToFile(mapFileStream, design.getNewMapFile());
					statistics.failMap(mapFile.getAbsolutePath());
				}
			}
		} catch (NoSuchAlgorithmException e) {
			statistics.info("Failed to verify " + hlddFilePath + ": couldn't obtain algorithm for comparison: " + e.getMessage());
		} catch (IOException e) {
			statistics.info("Failed to verify " + hlddFilePath + ": couldn't read original file or write the changed file: " + e.getMessage());
		} catch (InterruptedException e) {
			statistics.info("Failed to verify " + hlddFilePath + ": EventDispatchThread couldn't fall asleep " +
					"to wait for converter to finish:\n" + e.getMessage());
		} catch (ExecutionException e) {
			statistics.info("Failed to verify " + hlddFilePath + ": exception while converting file: " + e.getMessage());
		}
	}

	private void writeToFile(ByteArrayOutputStream mapFileStream, File newMapFile) throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(newMapFile));
		writer.write(mapFileStream.toString());
		writer.flush();
		QuietCloser.closeQuietly(writer);
	}

	static boolean areEqual(InputStream firstStream, InputStream secondStream) throws NoSuchAlgorithmException, IOException {

		return getMD5(firstStream).equals(getMD5(secondStream));

	}

	static String getMD5(InputStream inputStream) throws NoSuchAlgorithmException, IOException {
		// Collect digest
		MessageDigest md = MessageDigest.getInstance("MD5");
		inputStream = new DigestInputStream(new BufferedInputStream(inputStream), md);

		byte[] buffer = new byte[1024];
		try {
			while (inputStream.read(buffer) != -1) {
			}
		} finally {
			QuietCloser.closeQuietly(inputStream);
		}

		byte[] digest = md.digest();

		// As String
		StringBuilder builder = new StringBuilder();
		for (byte dig : digest) {
			builder.append(Integer.toString((dig & 0xff) + 0x100, 16).substring(1));
		}

		return builder.toString();
	}


	public static void main(String[] args) {

		AutomaticConverterVerifier converterVerifier = new AutomaticConverterVerifier();
		converterVerifier.verify(Statistics.createConsoleStatistics());

	}
}
