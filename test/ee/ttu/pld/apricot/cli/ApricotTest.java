package ee.ttu.pld.apricot.cli;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

/**
 * @author Anton Chepurov
 */
public class ApricotTest {

	@Test
	public void testAssertainExample() throws IOException {

		String xmlPath = ".\\classes\\test\\VertigoInterfaceParsers\\ee\\ttu\\pld\\apricot\\cli\\Assertain_example_in.xml";
		File xmlFile = new File(new File(xmlPath).toURI().getPath());
		File copyXmlFile = new File(xmlFile.getParent(), "Copy Assertain_example_in.xml");

		try {
			FileUtils.copyFile(xmlFile, copyXmlFile);

			long oldLength = copyXmlFile.length();
			new Apricot(new String[]{copyXmlFile.getAbsolutePath(), "./lib/"});
			long newLength = copyXmlFile.length();

			assertTrue(newLength > oldLength);
		} finally {
			assertTrue(copyXmlFile.delete());
		}
	}
}
