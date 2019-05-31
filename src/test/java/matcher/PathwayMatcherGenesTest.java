package matcher;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PathwayMatcherGenesTest {

	String[] args = { "match-genes", "-i", "src/test/resources/Genes/____.txt", "-o", "", "-T"};

	@AfterEach
	void deleteOutput(TestInfo testInfo) {
		// Delete the output directory if exists:
		try {
			File directory = new File(testInfo.getTestMethod().get().getName() + "/");
			FileUtils.deleteDirectory(directory);
			assertFalse(directory.exists());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	void genesDiabetesInYouthTest(TestInfo testInfo) throws IOException {
		args[2] = "src/test/resources/Genes/DiabetesInYouth.txt";
		args[4] = testInfo.getTestMethod().get().getName() + "/";
		Main.main(args);

		List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
		assertEquals(38, search.size());

		List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
		assertEquals(10, analysis.size());
	}

	@Test
	public void genesCysticFibrosisTest(TestInfo testInfo) throws IOException {
		System.out.println(System.getProperty("user.dir"));
		args[2] = "src/test/resources/Genes/CysticFibrosis.txt";
		args[4] = testInfo.getTestMethod().get().getName() + "/";
		Main.main(args);

		// Check the search file
		List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
		assertEquals(507, search.size()); // Its 98 records + header

		List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
		assertEquals(105, analysis.size()); // Its 98 records + header
	}

}
