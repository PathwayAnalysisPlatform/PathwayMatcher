package matcher;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PathwayMatcherProteinsTest {

    private static String searchFile = "search.tsv";
    private static String analysisFile = "analysis.tsv";

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
    public void uniProtCysticFibrosisTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-uniprot",
                "-i", "src/test/resources/Proteins/UniProt/CysticFibrosis.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"};
        Main.main(args);

        // Check the search file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(539, search.size()); // Its 98 records + header

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(105, analysis.size()); // Its 98 records + header
    }

    @Test
    public void ensemblCysticFibrosisTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-ensembl",
                "-i", "src/test/resources/Proteins/Ensembl/CysticFibrosis.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"};
        Main.main(args);

        // Check the search file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(539, search.size()); // Its 98 records + header

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(105, analysis.size()); // Its 98 records + header
    }

    @Test
    public void singleProteinWithoutTopLevelPathwaysTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(106, output.size()); // Its 98 records + header
    }

    @Test
    public void singleProteinWithTopLevelPathwaysTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"};
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(116, output.size());
    }

    @Test
    public void singleProteinWithIsoformTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProteinWithIsoform.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/"
        };
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(11, output.size());
    }

    @Test
    public void singleProteinWithIsoformAndTopLevelPathwaysTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProteinWithIsoform.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"
        };
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(11, output.size());
    }

    @Test
    public void multipleProteinsTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-uniprot",
                "-i", "src/test/resources/Proteins/Valid/correctList.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"
        };
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(420, output.size());
    }

    @Test
    public void multipleProteinsWithTopLevelPathwaysTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-uniprot",
                "-i", "src/test/resources/Proteins/Valid/correctList.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"};
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(420, output.size());
    }

    @Test
    public void hypoglycemiaProteinsTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-uniprot",
                "-i", "src/test/resources/Proteins/UniProt/Hypoglycemia.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(394, output.size());
    }

    @Test
    void ensemblDiabetesInYouthTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-ensembl",
                "-i", "src/test/resources/Proteins/Ensembl/DiabetesInYouth.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"};
        Main.main(args);

        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(174, search.size());

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(10, analysis.size());
    }

    @Test
    void insulinRelatedSignalingTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-uniprot",
                "-i", "src/test/resources/Proteins/UniProt/insulinRelatedSignalProteins.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"
        };
        Main.main(args);

        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(125, search.size());

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(26, analysis.size());
    }
}