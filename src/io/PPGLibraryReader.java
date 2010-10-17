package io;

import base.hldd.structure.models.BehModel;
import base.hldd.structure.variables.utils.PPGLibraryGraphVariableCreator;
import base.psl.Regex;
import base.helpers.RegexFactory;
import base.helpers.MatchAndSplitRegexHolder;
import base.psl.structure.Range;
import base.psl.structure.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import io.helpers.PSLOperatorDataHolder;

/**
 * @author Anton Chepurov
 */
public class PPGLibraryReader {

	private PPGLibrary ppgLibrary;

	private PSLBufferedReader pslBufReader;

	public PPGLibraryReader(File ppgLibraryFile) throws Exception {
		try {

			pslBufReader = new PSLBufferedReader(ppgLibraryFile);

			/* Read the LIST of operators */
			readOperatorsList();
			/* Read PPGs for operators */
			readOperatorsPPGs();

			//todo: check all the operators to have a corresponding model

		} finally {
			if (pslBufReader != null) {
				try {
					pslBufReader.close();
				} catch (IOException e) {/* Do nothing. */}
			}
		}

	}

	private void readOperatorsList() throws Exception {
		String word;

		/* Trim 'OPERATORS' */
		word = pslBufReader.readWordMatchingRegexp(Regex.LITERAL_ENDS_WITH_WHITESPACE_OR_BRACKET);
		if (!word.equalsIgnoreCase("OPERATORS"))
			throw new Exception("Malformed PPG Library file: \'OPERATORS\' expected to identify the beginning of the list of operators" + pslBufReader.printLog());

		/* Read LIST OF OPERATORS */
		word = pslBufReader.readBlock('{', '}', true);
		if (word == null)
			throw new Exception("Malformed PPG Library file: list of operators is malformed or missing at all" + pslBufReader.printLog());

		/* Parse each operator */
		String[] opDeclarations = word.split(";");
		ArrayList<PSLOperator> pslOperators = new ArrayList<PSLOperator>();
		for (String opDeclaration : opDeclarations) {
			if (opDeclaration.trim().startsWith(PSLBufferedReader.DEFAULT_COMMENT)) continue;
			pslOperators.add(createPSLOperator(opDeclaration));
		}

		/* Create a PPG Library on the basis of read operators */
		ppgLibrary = new PPGLibrary(pslOperators.toArray(new PSLOperator[pslOperators.size()]));
	}

	static PSLOperator createPSLOperator(String opDeclaration) {
		PSLOperatorDataHolder data = parseOperationDeclaration(opDeclaration);

		return new PSLOperator(data.getOperatorName(), data.getMatchingRegexp(),
				data.getSplittingRegexp(), data.getSplitLimit(),
				data.getWindowPlaceholders(), data.getOperandIndexByName());
	}

	private void readOperatorsPPGs() throws Exception {
		String word;

		while (true) {
			/* Read PPG */
			boolean nextPPGFound = pslBufReader.trySkippingToRegexp(".*\\{$");
			if (!nextPPGFound) break;
//			word = pslBufReader.readWordMatchingRegexp(Regex.LITERAL_ENDS_WITH_WHITESPACE_OR_BRACKET);
//			word = pslBufReader.getLastReadWord();
//			if (word == null) break;

			/* PPG Name */
			String ppgName = pslBufReader.getLastReadWord();

			/* PPG block */
			word = pslBufReader.readBlock('{', '}', true);
			if (word == null || word.length() == 0)
				throw new Exception("PPG for operator \'" + ppgName + "\' has empty or no body" + pslBufReader.printLog());

			BehModel model = BehModel.parseHlddStructure(word, new PPGLibraryGraphVariableCreator());
			ppgLibrary.setModelToPPG(ppgName, model);
		}
	}

	static PSLOperatorDataHolder parseOperationDeclaration(String opDeclaration) {

		int operandCount = 0;
		String operatorName = opDeclaration = opDeclaration.trim();
		int splitLimit = 0;
		HashMap<String, Integer> operandIndexByName = new HashMap<String, Integer>();
		boolean withWindow = false;
		String[] windowPlaceholders = null;
		MatchAndSplitRegexHolder regexps = new MatchAndSplitRegexHolder(null, null); // Temporal filler
		/* Parse WINDOW */
		if (Range.isRangeDeclaration(opDeclaration)) {
			withWindow = true;
			windowPlaceholders = Range.parseRangeDeclaration(opDeclaration);

			opDeclaration = opDeclaration.replaceFirst("\\[.*\\]", "");
		}

		String[] words = splitToTokens(opDeclaration);

		for (int i = 0; i < words.length; i++) {
			String word = words[i].trim();
			if (!isWordUppercase(word)) {
				/* Words in LowerCase are DELIMITERS. PSLOperator REGEXes must be formed on their basis. */
//				operatorName = word;
				splitLimit = 2;
				regexps = RegexFactory.createMatchAndSplitRegexps(word, i == 0, withWindow);

			} else {

				/* Hash to map OPERAND_NAME and OPERAND_INDEX*/
				operandIndexByName.put(word, operandCount++);

			}
		}

		return new PSLOperatorDataHolder(operatorName, regexps.getMatchingRegexp(), regexps.getSplittingRegexp(), splitLimit, windowPlaceholders, operandIndexByName);

	}

	static String[] splitToTokens(String opDeclaration) {
		return opDeclaration.split("\\s");
	}

	private static boolean isWordUppercase(String word) {
		char[] chars = word.toCharArray();
		for (char aChar : chars) {
			if (!(Character.isLetter(aChar) && Character.isUpperCase(aChar) || Character.isDigit(aChar))) return false;
		}
		return true;
	}

	public PPGLibrary getPpgLibrary() {
		return ppgLibrary;
	}

}
