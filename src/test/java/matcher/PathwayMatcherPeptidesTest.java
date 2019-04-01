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

import static matcher.tools.ListDiff.anyContains;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathwayMatcherPeptidesTest {

    private static String searchFile = "search.tsv";
    private static String analysisFile = "analysis.tsv";
    private static String fastaFile = "src/main/resources/uniprot-all.fasta";

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
    void insulinTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-peptides",
                "-i", "src/test/resources/Peptides/insulinSignalPeptide.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T",
                "-f", fastaFile
        };
        Main.main(args);

        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(116, output.size());
        assertFalse(anyContains("F8WCM5", output));
        assertTrue(anyContains("P01308", output));

        List<String> stats = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(22, stats.size());
    }

    @Test
    void insulinRelatedSignalPeptidesTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-peptides",
                "-i", "src/test/resources/Peptides/insulinRelatedSignalPeptides.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T",
                "-g",
                "-f", fastaFile
        };
        Main.main(args);

        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(125, output.size());
        assertTrue(anyContains("Q16270", output));
        assertTrue(anyContains("P35858", output));
        assertTrue(anyContains("P17936", output));
        assertFalse(anyContains("P17936-2", output));
        assertTrue(anyContains("P08069", output));
        assertFalse(anyContains("Q16270-2", output));

        List<String> stats = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(26, stats.size());
    }

    @Test
    void searchWithPeptideFillHitsTest1(TestInfo testInfo) throws IOException {

        String[] args = {
                "match-peptides",
                "-i", "src/test/resources/Peptides/singlePeptide.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T",
                "-g",
                "-f", fastaFile
        };
        Main.main(args);

        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertTrue(anyContains("P01308", output));
        assertTrue(anyContains("R-HSA-264876", output));
        assertTrue(anyContains("R-HSA-74749", output));
    }

    @Test
    void searchWithPeptidesFillHitsTest2(TestInfo testInfo) throws IOException {

        String[] args = {
                "match-peptides",
                "-i", "src/test/resources/Peptides/peptideList2.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T",
                "-g",
                "-f", fastaFile
        };
        Main.main(args);

        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(361, output.size());
        assertTrue(anyContains("P01137", output));
        assertTrue(anyContains("R-HSA-2672351", output));
        assertTrue(anyContains("R-HSA-76002", output));

        List<String> stats = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(71, stats.size());

    }

}