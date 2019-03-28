package matcher;

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static matcher.tools.ListDiff.anyContains;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PathwayMatcherPeptidesTest {

    private static String searchFile = "output/search.tsv";
    private static String analysisFile = "output/analysis.tsv";
    private static String fastaFile = "src/main/resources/uniprot-all.fasta";

    @Test
    void insulinTest() throws IOException {
        String[] args = {
                "-t", "peptide",
                "-i", "src/test/resources/Peptides/insulinSignalPeptide.txt",
                "-o", "output/",
                "-tlp",
                "-f", fastaFile
        };
        Main.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(116, output.size());
        assertFalse(anyContains("F8WCM5", output));
        assertTrue(anyContains("P01308", output));

        List<String> stats = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(22, stats.size());
    }

    @Test
    void insulinRelatedSignalPeptidesTest() throws IOException {
        String[] args = {
                "-t", "peptides",
                "-i", "src/test/resources/Peptides/insulinRelatedSignalPeptides.txt",
                "-o", "output/",
                "-tlp",
                "-g",
                "-f", fastaFile
        };
        Main.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(125, output.size());
        assertTrue(anyContains("Q16270", output));
        assertTrue(anyContains("P35858", output));
        assertTrue(anyContains("P17936", output));
        assertFalse(anyContains("P17936-2", output));
        assertTrue(anyContains("P08069", output));
        assertFalse(anyContains("Q16270-2", output));

        List<String> stats = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(26, stats.size());
    }

    @Test
    void searchWithPeptideFillHitsTest1() throws IOException {

        String[] args = {
                "-t", "peptides",
                "-i", "src/test/resources/Peptides/singlePeptide.txt",
                "-o", "output/",
                "-tlp",
                "-g",
                "-f", fastaFile
        };
        Main.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertTrue(anyContains("P01308", output));
        assertTrue(anyContains("R-HSA-264876", output));
        assertTrue(anyContains("R-HSA-74749", output));
    }

    @Test
    void searchWithPeptidesFillHitsTest2() throws IOException {

        String[] args = {
                "-t", "peptides",
                "-i", "src/test/resources/Peptides/peptideList2.txt",
                "-o", "output/",
                "-tlp",
                "-g",
                "-f", fastaFile
        };
        Main.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(539, output.size());
        assertTrue(anyContains("P37088", output));
        assertTrue(anyContains("P01137", output));
        assertTrue(anyContains("R-HSA-2672351", output));
        assertTrue(anyContains("R-HSA-76002", output));
        assertTrue(anyContains("R-HSA-449147", output));

        List<String> stats = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(105, stats.size());

    }

}