package io.scan;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * @author Anton Chepurov
 */
public class VHDLScannerTest {
	VHDLScanner vhdlScanner;
	VHDLToken token;

	@Test
	public void allowIncompleteTokensWorks() throws Exception {
		vhdlScanner = createScannerFrom(createLexemeComposerFrom("X\"10\""));
		VHDLToken token = vhdlScanner.next();
		assertNotNull(token);
		System.out.println(token.getValue());
	}

	@Test
	public void useTokenRecognized() throws Exception {
		String source = "use IEEE.std_logic_1164.all;\n" +
				"use IEEE.std_logic_unsigned.all;\n" +
				"use IEEE.std_logic_arith.all;\n" +
				"     \n" +
				"use work.types.all; \n" +
				"use work.crc_pkg.all;";
		vhdlScanner = createScannerFrom(createLexemeComposerFrom(source));
		while ((token = vhdlScanner.next()) != null) {
			assertTrue(VHDLToken.Type.USE_DECL + " token is not recognized: " + token.getValue(),
					token.getType() == VHDLToken.Type.USE_DECL);
		}
	}

	@Test
	public void packageTokensRecognized() throws Exception {
		String sourceDecl = "package crc_pkg is\npackage types is";
		String sourceBody = "package body crc_pkg is";
		vhdlScanner = createScannerFrom(createLexemeComposerFrom(sourceDecl));
		while ((token = vhdlScanner.next()) != null) {
			assertTrue(VHDLToken.Type.PACKAGE_DECL + " token is not recognized: " + token.getValue(),
					token.getType() == VHDLToken.Type.PACKAGE_DECL);
		}
		vhdlScanner = createScannerFrom(createLexemeComposerFrom(sourceBody));
		while ((token = vhdlScanner.next()) != null) {
			assertTrue(VHDLToken.Type.PACKAGE_BODY_DECL + " token is not recognized: " + token.getValue(),
					token.getType() == VHDLToken.Type.PACKAGE_BODY_DECL);
		}
	}


	private static LexemeComposer createLexemeComposerFrom(String sourceString) {
		return new LexemeComposer(sourceString);
	}

	private static VHDLScanner createScannerFrom(LexemeComposer lexemeComposer) {
		return new VHDLScanner(lexemeComposer);
	}

}
