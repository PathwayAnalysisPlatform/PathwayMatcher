package matcher;

import com.google.common.io.Files;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PathwayMatcherProteinsTest {

    private static String searchFile = "output/search.tsv";
    private static String analysisFile = "output/analysis.tsv";

    @BeforeAll
    static void setUp() {
    }

    @Test
    public void uniProtCysticFibrosisTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/UniProt/CysticFibrosis.txt",
                "-o", "output/",
                "-tlp"};
        Main.main(args);

        // Check the search file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(539, search.size()); // Its 98 records + header

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(105, analysis.size()); // Its 98 records + header
    }

    @Test
    public void ensemblCysticFibrosisTest() throws IOException {
        String[] args = {
                "-t", "ensembl",
                "-i", "src/test/resources/Proteins/Ensembl/CysticFibrosis.txt",
                "-o", "output/",
                "-tlp"};
        Main.main(args);

        // Check the search file
        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(539, search.size()); // Its 98 records + header

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(105, analysis.size()); // Its 98 records + header
    }

    @Test
    public void singleProteinWithoutTopLevelPathwaysTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "output/"};
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(106, output.size()); // Its 98 records + header
    }

    @Test
    public void singleProteinWithTopLevelPathwaysTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "output/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(116, output.size());
    }

    @Test
    public void singleProteinWithIsoformTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProteinWithIsoform.txt",
                "-o", "output/"
        };
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(11, output.size());
    }

    @Test
    public void singleProteinWithIsoformAndTopLevelPathwaysTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProteinWithIsoform.txt",
                "-o", "output/",
                "-tlp"
        };
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(11, output.size());
    }

    @Test
    public void multipleProteinsTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/correctList.txt",
                "-o", "output/",
                "-tlp"
        };
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(420, output.size());
    }

    @Test
    public void multipleProteinsWithTopLevelPathwaysTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/correctList.txt",
                "-o", "output/",
                "-tlp"};
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(420, output.size());
    }

    @Test
    public void hypoglycemiaProteinsTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/UniProt/Hypoglycemia.txt",
                "-o", "output/",
                "-m", "somethingweird",
                "-r", "3"};
        Main.main(args);

        // Check the output file
        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(394, output.size());
    }

    @Test
    void ensemblDiabetesInYouthTest() throws IOException {
        String[] args = {
                "-t", "ensembl",
                "-i", "src/test/resources/Proteins/Ensembl/DiabetesInYouth.txt",
                "-o", "output/",
                "-tlp"};
        Main.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(174, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(10, analysis.size());
    }

    @Test
    void insulinRelatedSignalingTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/UniProt/insulinRelatedSignalProteins.txt",
                "-o", "output/",
                "-tlp"
        };
        Main.main(args);

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(125, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(26, analysis.size());
    }
}