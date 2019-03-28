package matcher;

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.assertEquals;

class PathwayMatcherGenesTest {

	String[] args = { "-t", "genes", "-i", "src/test/resources/Genes/____.txt", "-o", "output/", "-tlp"};
	String searchFile = "output/search.tsv";
	String analysisFile = "output/analysis.tsv";
	
	@Test
	void genesDiabetesInYouthTest() throws IOException {
		args[3] = "src/test/resources/Genes/DiabetesInYouth.txt";
		Main.main(args);

		List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
		assertEquals(34, search.size());

		List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
		assertEquals(10, analysis.size());
	}

	@Test
	public void genesCysticFibrosisTest() throws IOException {
		System.out.println(System.getProperty("user.dir"));
		args[3] = "src/test/resources/Genes/CysticFibrosis.txt";
		Main.main(args);

		// Check the search file
		List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
		assertEquals(539, search.size()); // Its 98 records + header

		List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
		assertEquals(105, analysis.size()); // Its 98 records + header
	}

}
