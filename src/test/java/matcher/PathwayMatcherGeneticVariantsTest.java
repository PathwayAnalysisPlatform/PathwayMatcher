package matcher;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
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

//@Disabled
class PathwayMatcherGeneticVariantsTest {

    String[] args = {"match-rsids", "-i", "", "-o", "", "-T"};

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
    @Disabled
    void GIANTTest(TestInfo testInfo) throws IOException {
        args[0] = "match-rsids";
        args[2] = "src/test/resources/GeneticVariants/RsId/GIANT.csv";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        //List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        //assertEquals(18014941, search.size());

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(1899, analysis.size());
    }

    @Test
    void cysticFibrosisTest(TestInfo testInfo) throws IOException {
        args[0] = "match-rsids";
        args[2] = "src/test/resources/GeneticVariants/RsId/CysticFibrosis.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(5204, search.size());

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(179, statistics.size());
    }

    @Test
    void cysticFibrosisWithChrAndBpTest(TestInfo testInfo) throws IOException {
        args[0] = "match-chrbp";
        args[2] = "src/test/resources/GeneticVariants/Chr_Bp/CysticFibrosis.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(5204, search.size());

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(179, statistics.size());
    }

    @Test
    void cysticFibrosisWithVCFTest(TestInfo testInfo) throws IOException {
        args[0] = "match-vcf";
        args[2] = "src/test/resources/GeneticVariants/VCF/CysticFibrosis.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(5204, search.size());

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(179, statistics.size());
    }

    @Test
    void diabetesTest(TestInfo testInfo) throws IOException {
        args[0] = "match-rsids";
        args[2] = "src/test/resources/GeneticVariants/RsId/Diabetes.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(10324, search.size());

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(676, statistics.size());
    }

    @Test
    void diabetesWithChrAndBpTest(TestInfo testInfo) throws IOException {
        args[0] = "match-chrbp";
        args[2] = "src/test/resources/GeneticVariants/Chr_Bp/Diabetes.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(10125, search.size());

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(675, statistics.size());
    }

    @Test
    void diabetesInYouthTest(TestInfo testInfo) throws IOException {
        args[0] = "match-rsids";
        args[2] = "src/test/resources/GeneticVariants/RsId/DiabetesInYouth.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(179, search.size());
        assertTrue(anyContains("Q9NQB0", search));
        assertFalse(anyContains("P07550", search));

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(16, statistics.size());
    }

    @Test
    void diabetesInYouthWithChrAndBpTest(TestInfo testInfo) throws IOException {
        args[0] = "match-chrbp";
        args[2] = "src/test/resources/GeneticVariants/Chr_Bp/DiabetesInYouth.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);


        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(179, search.size());
        assertTrue(anyContains("Q9NQB0", search));

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(16, statistics.size());
    }

    @Test
    void huntingtonsDiseaseTest(TestInfo testInfo) throws IOException {
        args[0] = "match-rsids";
        args[2] = "src/test/resources/GeneticVariants/RsId/HuntingtonsDisease.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(598, search.size());

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(131, statistics.size());
    }

    @Test
    void huntingtonsDiseaseWithChrAndBpTest(TestInfo testInfo) throws IOException {
        args[0] = "match-chrbp";
        args[2] = "src/test/resources/GeneticVariants/Chr_Bp/HuntingtonsDisease.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(493, search.size());

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(120, statistics.size());
    }

    @Test
    void HypoglycemiaTest(TestInfo testInfo) throws IOException {
        args[0] = "match-rsids";
        args[2] = "src/test/resources/GeneticVariants/RsId/Hypoglycemia.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);


        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(506, search.size());
        assertTrue(anyContains("P07550", search));
        assertTrue(anyContains("P23786", search));

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(78, statistics.size());
    }

    @Test
    void HypoglycemiaWithChrAndBpTest(TestInfo testInfo) throws IOException {
        args[0] = "match-chrbp";
        args[2] = "src/test/resources/GeneticVariants/Chr_Bp/Hypoglycemia.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(506, search.size());
        assertTrue(anyContains("P07550", search));
        assertTrue(anyContains("P23786", search));

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(78, statistics.size());
    }

    @Test
    void UlcerativeColitisTest(TestInfo testInfo) throws IOException {
        args[0] = "match-rsids";
        args[2] = "src/test/resources/GeneticVariants/RsId/UlcerativeColitis.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(11858, search.size());

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(602, statistics.size());
    }

    @Test
    void UlcerativeColitisWithChrAndBpTest(TestInfo testInfo) throws IOException {
        args[0] = "match-chrbp";
        args[2] = "src/test/resources/GeneticVariants/Chr_Bp/UlcerativeColitis.txt";
        args[4] = testInfo.getTestMethod().get().getName() + "/";
        Main.main(args);

        // Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/search.tsv"), Charset.defaultCharset());
        assertEquals(11748, search.size());

        List<String> statistics = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/analysis.tsv"), Charset.defaultCharset());
        assertEquals(601, statistics.size());
    }
}