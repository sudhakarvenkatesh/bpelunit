package net.bpelunit.utils.testsuitesplitter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.bpelunit.framework.xml.suite.XMLTestCasesSection;
import net.bpelunit.framework.xml.suite.XMLTestSuiteDocument;
import net.bpelunit.utils.testsuitesplitter.permutation.PermutationBuilder;

import org.apache.xmlbeans.XmlException;

/**
 * This is a small tool that splits up a test suite into all possible
 * combinations and writes them to separate files.
 * 
 * This tool is a quick hack. There are no tests nor is it guaranteed to work.
 * USE AT YOUR OWN RISK
 * 
 * @author Daniel Luebke
 */
public class SplitIntoPermutations {

	private static final String BPTS_FILENAME_SUFFIX = ".bpts";

	private static final PermutationBuilder permutationBuilder = new PermutationBuilder();
	
	public static void main(String[] args) throws IOException, XmlException {
		
		if(args.length == 0 || args[1].equals("--help")) {
			help();
		} 
		
		XMLTestSuiteDocument x = XMLTestSuiteDocument.Factory.parse(new File(
				args[0]));
		String prefix = getFileNameWithoutSuffix(args[0]);
		
		Set<Set<Integer>> permutationSet = permutationBuilder.getPermutationSet(x.getTestSuite()
				.getTestCases().getTestCaseList().size() - 1);

		for (Set<Integer> currentSet : permutationSet) {
			XMLTestSuiteDocument testSuiteDocumentCopy = (XMLTestSuiteDocument) x.copy();

			XMLTestCasesSection testCases = testSuiteDocumentCopy.getTestSuite().getTestCases();
			for (int i = testCases.sizeOfTestCaseArray() - 1; i >= 0; i--) {

				if (!currentSet.contains(i)) {
					testCases.removeTestCase(i);
				}
			}

			String suiteFileName = getSuiteFileName(prefix, currentSet);
			testSuiteDocumentCopy.getTestSuite().setName(suiteFileName);
			System.out.println("Writing " + suiteFileName + "...");
			testSuiteDocumentCopy.save(new File(suiteFileName));
		}
	}

	private static void help() {
		System.out.println("SplitIntoPermutations");
		System.out.println();
		System.out.println("SplitIntoPermutations file.bpts");
		System.out.println();
		System.out.println("Splits a BPELUnit test suite into several files. Every combination of test cases is generated.");
		System.out.println();
		
		System.exit(1);
	}

	private static String getFileNameWithoutSuffix(String completeFileName) {
		if(completeFileName == null) {
			return "";
		}
		
		File f = new File(completeFileName);
		
		String fileName = f.getName();
		
		if(fileName.endsWith(BPTS_FILENAME_SUFFIX)) {
			fileName = fileName.substring(0, fileName.length() - BPTS_FILENAME_SUFFIX.length());
		}
		
		return fileName;
	}

	private static String getSuiteFileName(String prefix, Collection<Integer> testcaseIndices) {
		List<Integer> sortedTestCaseIndices = sortSet(testcaseIndices);

		StringBuilder sb = new StringBuilder();
		sb.append(prefix);

		for (int i : sortedTestCaseIndices) {
			sb.append("-").append(i);
		}

		sb.append(".bpts");

		return sb.toString();
	}

	private static List<Integer> sortSet(Collection<Integer> testcaseIndices) {
		List<Integer> sortedTestCaseIndices = new ArrayList<Integer>(
				testcaseIndices);
		Collections.sort(sortedTestCaseIndices);
		return sortedTestCaseIndices;
	}
}
