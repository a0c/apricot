package ee.ttu.pld.apricot.verifier;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.*;

/**
 * @author Anton Chepurov
 */
public class AutomaticConverterVerifierTest {

	@Test
	public void testGetMD5() throws IOException, NoSuchAlgorithmException {

		String simpleString = "Some very simple string";
		String md5 = AutomaticConverterVerifier.getMD5(new ByteArrayInputStream(simpleString.getBytes()));
		assertEquals("0c720accdd049ba51ac2216d8a657709", md5);
		md5 = AutomaticConverterVerifier.getMD5(new ByteArrayInputStream(simpleString.getBytes()));
		assertEquals("0c720accdd049ba51ac2216d8a657709", md5);

		simpleString = "sdf;salkjr213098auspd;oifj12938413284187238470shde;laskdph23p84y   098	y09328y;lesih p  " +
				"0987098 710928345 34\n\n\nasdfspoiup2iru";
		md5 = AutomaticConverterVerifier.getMD5(new ByteArrayInputStream(simpleString.getBytes()));
		assertEquals("09b0c19f2cf78d8de10e8c2deb34b469", md5);


	}

	@Test
	public void testEqualStrings() throws NoSuchAlgorithmException, IOException {

		String simpleString = "Some very simple string";

		ByteArrayInputStream is1 = new ByteArrayInputStream(simpleString.getBytes());
		ByteArrayInputStream is2 = new ByteArrayInputStream(simpleString.getBytes());

		assertTrue(AutomaticConverterVerifier.areEqual(is1, is2));

		String anotherSimpleString = "132491-3984-193284-9i321s;ek;lkwqnr;ewnrqwe0--- c qwerlij ;lkqjew;r --2-3 4\n\nas";

		is1 = new ByteArrayInputStream(simpleString.getBytes());
		is2 = new ByteArrayInputStream(anotherSimpleString.getBytes());

		assertFalse(AutomaticConverterVerifier.areEqual(is1, is2));

	}

	@Test
	public void testVerify() {

		Statistics statistics = Statistics.createByteArrayStatistics();
		AutomaticConverterVerifier converterVerifier = new AutomaticConverterVerifier();
		converterVerifier.verify(statistics);

		assertEquals("Verification failed", "All files PASSED. Total: 81 files. All MAP files PASSED. Total: 45 files.", statistics.getMessage());

	}
}
