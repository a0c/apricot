package ui;

import org.junit.Test;

/**
 * <br><br>User: Anton Chepurov
 * <br>Date: 29.05.2010
 * <br>Time: 10:22:06
 */
public class ConverterSettingsTest {
	
	@Test (expected = ExtendedException.class)
	public void testValidateSourceAndPslFiles() throws ExtendedException {

		ConverterSettings settings = new ConverterSettings();
		settings.setSourceFile(null);
		settings.setPslFile(null);

		settings.validate();

	}
	@Test (expected = ExtendedException.class)
	public void testValidateBaseModelFile() throws ExtendedException {

		ConverterSettings settings = new ConverterSettings();
		settings.setBaseModelFile(null);

		settings.validate();

	}
}
