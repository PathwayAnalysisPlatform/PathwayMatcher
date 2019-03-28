package matcher;

import com.google.common.io.Files;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static matcher.tools.ListDiff.anyContains;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

//@Disabled
class PathwayMatcherGeneticVariantsTest {

    @Test
    @Disabled
    void GIANTTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-o", "output/",
                "-i", "src/test/resources/GeneticVariants/RsId/GIANT.csv"};
        Main.main(args);

        // Check the output file
        //List<String> search = Files.readLines(new File("output/search.tsv"), Charset.defaultCharset());
        //assertEquals(18014941, search.size());

        List<String> analysis = Files.readLines(new File("output/analysis.tsv"), Charset.defaultCharset());
        assertEquals(1899, analysis.size());
    }

    @Test
    void cysticFibrosisTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "src/test/resources/GeneticVariants/RsId/CysticFibrosis.txt",
                "-o", "output/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.tsv"), Charset.defaultCharset());
        assertEquals(5240, search.size());

        List<String> statistics = Files.readLines(new File("output/analysis.tsv"), Charset.defaultCharset());
        assertEquals(179, statistics.size());
    }

    @Test
    void cysticFibrosisWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "CHRBPS",
                "-i", "src/test/resources/GeneticVariants/Chr_Bp/CysticFibrosis.txt",
                "-o", "output/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/search.tsv"), Charset.defaultCharset());
        assertEquals(5240, search.size());

        List<String> statistics = Files.readLines(new File("output/analysis.tsv"), Charset.defaultCharset());
        assertEquals(179, statistics.size());
    }

    @Test
    void cysticFibrosisWithVCFTest() throws IOException {
        String[] args = {"-t", "vcf",
                "-i", "src/test/resources/GeneticVariants/VCF/CysticFibrosis.txt",
                "-o", "output/cysticFibrosisWithVCFTest/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/cysticFibrosisWithVCFTest/search.tsv"), Charset.defaultCharset());
        assertEquals(5240, search.size());

        List<String> statistics = Files.readLines(new File("output/cysticFibrosisWithVCFTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(179, statistics.size());
    }

    @Test
    void diabetesTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "src/test/resources/GeneticVariants/RsId/Diabetes.txt",
                "-o", "output/diabetesTest/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/diabetesTest/search.tsv"), Charset.defaultCharset());
        assertEquals(10072, search.size());

        List<String> statistics = Files.readLines(new File("output/diabetesTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(676, statistics.size());
    }

    @Test
    void diabetesWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "chrbp",
                "-i", "src/test/resources/GeneticVariants/Chr_Bp/Diabetes.txt",
                "-o", "output/diabetesWithChrAndBpTest/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/diabetesWithChrAndBpTest/search.tsv"), Charset.defaultCharset());
        assertEquals(9873, search.size());

        List<String> statistics = Files.readLines(new File("output/diabetesWithChrAndBpTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(675, statistics.size());
    }

    @Test
    void diabetesInYouthTest() throws IOException {
        // Execute the full pathway matcher
        String[] args = {"-t", "rsids",
                "-i", "src/test/resources/GeneticVariants/RsId/DiabetesInYouth.txt",
                "-o", "output/diabetesInYouthTest/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/diabetesInYouthTest/search.tsv"), Charset.defaultCharset());
        assertEquals(179, search.size());
        assertTrue(anyContains("Q9NQB0", search));
        assertFalse(anyContains("P07550", search));

        List<String> statistics = Files.readLines(new File("output/diabetesInYouthTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(16, statistics.size());
    }

    @Test
    void diabetesInYouthWithChrAndBpTest() throws IOException {
        // Execute the full pathway matcher
        String[] args = {"-t", "chrbps",
                "-i", "src/test/resources/GeneticVariants/Chr_Bp/DiabetesInYouth.txt",
                "-o", "output/diabetesInYouthWithChrAndBpTest/",
                "-tlp"};
        Main.main(args);


        // Check the output file
        List<String> search = Files.readLines(new File("output/diabetesInYouthWithChrAndBpTest/search.tsv"), Charset.defaultCharset());
        assertEquals(179, search.size());
        assertTrue(anyContains("Q9NQB0", search));

        List<String> statistics = Files.readLines(new File("output/diabetesInYouthWithChrAndBpTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(16, statistics.size());
    }

    @Test
    void huntingtonsDiseaseTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "src/test/resources/GeneticVariants/RsId/HuntingtonsDisease.txt",
                "-o", "output/huntingtonsDiseaseTest/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/huntingtonsDiseaseTest/search.tsv"), Charset.defaultCharset());
        assertEquals(491, search.size());

        List<String> statistics = Files.readLines(new File("output/huntingtonsDiseaseTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(131, statistics.size());
    }

    @Test
    void huntingtonsDiseaseWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "chrbps",
                "-i", "src/test/resources/GeneticVariants/Chr_Bp/HuntingtonsDisease.txt",
                "-o", "output/huntingtonsDiseaseWithChrAndBpTest/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/huntingtonsDiseaseWithChrAndBpTest/search.tsv"), Charset.defaultCharset());
        assertEquals(386, search.size());

        List<String> statistics = Files.readLines(new File("output/huntingtonsDiseaseWithChrAndBpTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(120, statistics.size());
    }

    @Test
    void HypoglycemiaTest() throws IOException {
        String[] args = {"-t", "rsids",
                "-i", "src/test/resources/GeneticVariants/RsId/Hypoglycemia.txt",
                "-o", "output/HypoglycemiaTest/",
                "-tlp"};
        Main.main(args);


        // Check the output file
        List<String> search = Files.readLines(new File("output/HypoglycemiaTest/search.tsv"), Charset.defaultCharset());
        assertEquals(458, search.size());
        assertTrue(anyContains("P07550", search));
        assertTrue(anyContains("P23786", search));

        List<String> statistics = Files.readLines(new File("output/HypoglycemiaTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(78, statistics.size());
    }

    @Test
    void HypoglycemiaWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "chrbps",
                "-i", "src/test/resources/GeneticVariants/Chr_Bp/Hypoglycemia.txt",
                "-o", "output/HypoglycemiaWithChrAndBpTest/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/HypoglycemiaWithChrAndBpTest/search.tsv"), Charset.defaultCharset());
        assertEquals(458, search.size());
        assertTrue(anyContains("P07550", search));
        assertTrue(anyContains("P23786", search));

        List<String> statistics = Files.readLines(new File("output/HypoglycemiaWithChrAndBpTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(78, statistics.size());
    }

    @Test
    void UlcerativeColitisTest() throws IOException {
        String[] args = {"-t", "rsid",
                "-i", "src/test/resources/GeneticVariants/RsId/UlcerativeColitis.txt",
                "-o", "output/UlcerativeColitisTest/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/UlcerativeColitisTest/search.tsv"), Charset.defaultCharset());
        assertEquals(11586, search.size());

        List<String> statistics = Files.readLines(new File("output/UlcerativeColitisTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(602, statistics.size());
    }

    @Test
    void UlcerativeColitisWithChrAndBpTest() throws IOException {
        String[] args = {"-t", "chrbps",
                "-i", "src/test/resources/GeneticVariants/Chr_Bp/UlcerativeColitis.txt",
                "-o", "output/UlcerativeColitisWithChrAndBpTest/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File("output/UlcerativeColitisWithChrAndBpTest/search.tsv"), Charset.defaultCharset());
        assertEquals(11548, search.size());

        List<String> statistics = Files.readLines(new File("output/UlcerativeColitisWithChrAndBpTest/analysis.tsv"), Charset.defaultCharset());
        assertEquals(601, statistics.size());
    }
}