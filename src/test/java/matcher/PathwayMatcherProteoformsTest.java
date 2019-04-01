package matcher;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static matcher.tools.ListDiff.anyContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PathwayMatcherProteoformsTest {

    static String searchFile = "search.tsv";
    static String analysisFile = "analysis.tsv";

    @AfterEach
    void deleteOutput(TestInfo testInfo) {
        // Delete the output directory if exists:
        try {
            File directory = new File(testInfo.getTestMethod().get().getName() + "/");
            FileUtils.deleteDirectory(directory);
            Assertions.assertFalse(directory.exists());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void insulinTest(TestInfo testInfo) throws IOException {
        String[] args = {"match-proteoforms",
                "-i", "src/test/resources/Proteoforms/Simple/Insulin.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/"
        };
        Main.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(102, search.size());
        assertTrue(anyContains("P01308", search));
        assertTrue(anyContains("P01308;00798:95,00798:96,00798:100,00798:109", search));
        assertTrue(anyContains("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109", search));

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(21, analysis.size());
    }

    @Test
    public void insulinTlpTest(TestInfo testInfo) throws IOException {
        String[] args = {"match-proteoforms",
                "-i", "src/test/resources/Proteoforms/Simple/Insulin.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"};
        Main.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(112, search.size());
        assertTrue(anyContains("P01308", search));
        assertTrue(anyContains("P01308;00798:95,00798:96,00798:100,00798:109", search));
        assertTrue(anyContains("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109", search));

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(21, analysis.size());
    }

    @Test
    public void insulinSupersetMatchingTest(TestInfo testInfo) throws IOException {
        String[] args = {"match-proteoforms",
                "-i", "src/test/resources/Proteoforms/Simple/Insulin.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "superset",
                "-T"};
        Main.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(118, search.size());
        assertTrue(anyContains("P01308", search));
        assertTrue(anyContains("P01308;00798:95,00798:96,00798:100,00798:109", search));
        assertFalse(anyContains("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109", search));

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(15, analysis.size());
    }

    @Test
    public void insulinWithMODTest(TestInfo testInfo) throws IOException {
        String[] args = {"match-proteoforms",
                "-i", "src/test/resources/Proteoforms/Simple/InsulinWithMOD.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"};
        Main.main(args);

        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(112, search.size());

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(21, analysis.size());
    }

    @Test
    public void allProteoformsTest(TestInfo testInfo) throws IOException {
        String[] args = {"match-proteoforms",
                "-i", "src/test/resources/Proteoforms/Reactome/all_proteoforms.csv",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"};
        Main.main(args);

        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(378799, search.size());

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(1905, analysis.size());
    }

    @Test
    public void set1Test(TestInfo testInfo) throws IOException {
        String[] args = {"match-proteoforms",
                "-i", "src/test/resources/Proteoforms/SIMPLE/Set1.csv",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"};
        Main.main(args);

        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(177, search.size());

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(22, analysis.size());
    }

    @Test
    public void set2SubsetTest(TestInfo testInfo) throws IOException {
        String[] args = {"match-proteoforms",
                "-i", "src/test/resources/Proteoforms/SIMPLE/Set2.csv",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"};
        Main.main(args);

        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(1, search.size());

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(1, analysis.size());
    }

    @Test
    public void set2SupersetTest(TestInfo testInfo) throws IOException {
        String[] args = {"match-proteoforms",
                "-i", "src/test/resources/Proteoforms/SIMPLE/Set2.csv",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "SUPERSET",
                "-T"};
        Main.main(args);

        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(66, search.size());

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(15, analysis.size());
    }

    @Test
    public void set3Test(TestInfo testInfo) throws IOException {
        String[] args = {"match-proteoforms",
                "-i", "src/test/resources/Proteoforms/SIMPLE/Set3.csv",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T"};
        Main.main(args);

        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(732, search.size());

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(131, analysis.size());
    }

    @Test
    public void singleProteoformSearchSupersetTest(TestInfo testInfo) throws IOException {
        String[] args = {"match-proteoforms",
                "-i", "src/test/resources/Proteoforms/Simple/SingleProteoform.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T",
                "-m", "superset"};
        Main.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(115, search.size());
        search.remove(0);
        for (String line : search) {
            assertTrue(line.startsWith("O43561-2;\tO43561") || line.startsWith("O43561-2;00048:127,00048:132,00048:171,00048:191,00048:226\tO43561"));
        }

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(12, analysis.size());
    }

    @Test
    public void singleProteoformSearchStrictTest(TestInfo testInfo) throws IOException {
        String[] args = {"match-proteoforms",
                "-i", "src/test/resources/Proteoforms/Simple/SingleProteoform.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-T",
                "-m", "strict"};
        Main.main(args);

        //Check the output file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(108, search.size());
        search.remove(0);
        for (String line : search) {
            assertFalse(line.startsWith("O43561-2;\tO43561"));
            assertTrue(line.startsWith("O43561-2;00048:127,00048:132,00048:171,00048:191,00048:226\tO43561"));
        }

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(12, analysis.size());
    }

    @Test
    public void proteoformsCysticFibrosisTest(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-proteoforms",
                "-i", "src/test/resources/Proteoforms/Simple/CysticFibrosis.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-g",
                "-T"};
        Main.main(args);

        // Check the search file
        List<String> search = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(553, search.size()); // Its 98 records + header

        List<String> analysis = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + analysisFile), Charset.defaultCharset());
        assertEquals(105, analysis.size()); // Its 98 records + header
    }

}