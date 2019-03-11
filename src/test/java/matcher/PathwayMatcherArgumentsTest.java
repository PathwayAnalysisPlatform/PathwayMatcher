package matcher;

import com.google.common.io.Files;
import com.sun.org.glassfish.gmbal.Description;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

import static matcher.tools.ListDiff.anyMatches;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import model.Error;

public class PathwayMatcherArgumentsTest {

    private static String searchFile = "output/search.tsv";

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void mainWithNoArgumentsTest() {
        exit.expectSystemExitWithStatus(Error.NO_ARGUMENTS.getCode());
        PathwayMatcher.main(new String[0]);
        assertTrue(outContent.toString().startsWith("usage:"), "Help message was not shown.");
    }

    @Test
    public void missingRequiredOption_t_Test() {
        exit.expectSystemExitWithStatus(Error.MISSING_ARGUMENT.getCode());
        String[] args = {"-i", "input.txt"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_t_Test() {
        exit.expectSystemExitWithStatus(Error.MISSING_ARGUMENT.getCode());
        String[] args = {"-t", "-i", "input.txt"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingRequiredOption_i_Test() {
        // Fails because the input file can not be read, not because of configuration
        exit.expectSystemExitWithStatus(Error.MISSING_ARGUMENT.getCode());
        String[] args = {
                "-t", "uniprot", "-o", "output/"};
        PathwayMatcher.main(args);
    }

    @Test
    public void inputArgumentBroken_Test() {
        // Fails because the input file can not be read, not because of configuration
        exit.expectSystemExitWithStatus(Error.MISSING_ARGUMENT.getCode());
        String[] args = {
                "-", "t", "uniprot",
                "-i", "blabla.csv",
                "-o", "output/"};
        PathwayMatcher.main(args);
    }

    @Test
    public void inputFileNotFound_Test() {
        // Fails because the input file can not be read, not because of configuration
        exit.expectSystemExitWithStatus(Error.COULD_NOT_READ_INPUT_FILE.getCode());
        String[] args = {
                "-t", "uniprot",
                "-i", "blabla.csv",
                "-o", "output/"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_i_Test() {
        exit.expectSystemExitWithStatus(Error.MISSING_ARGUMENT.getCode());
        String[] args = {"-t", "rsidList", "-i"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_o_Test() {
        exit.expectSystemExitWithStatus(Error.MISSING_ARGUMENT.getCode());
        String[] args = {"-t", "rsidList", "-o", "-i", "src/test/resources/Proteins/UniProt/uniprot-all.list"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_r_Test() {
        exit.expectSystemExitWithStatus(Error.MISSING_ARGUMENT.getCode());
        String[] args = {"-t", "rsidList", "-r"};
        PathwayMatcher.main(args);
    }

    @Test
    public void matchingTypeTest() throws IOException {
        String[] args = {
                "-t", "proteoform",
                "-i", "src/test/resources/Proteoforms/Valid/multipleLinesWithIsoforms.txt",
                "-o", "output/",
                "-m", "superset"};
        PathwayMatcher.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());

        assertTrue(anyMatches("P08235-1;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));
        assertTrue(anyMatches("P08235-2;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));
        assertTrue(anyMatches("P08235-3;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));
        assertTrue(anyMatches("P08235-4;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));

        assertTrue(anyMatches("P02545-2;00046:395\tP02545\tR-HSA-5244669\t.+\tR-HSA-1640170", output));
        assertEquals(121, output.size());
    }

    @Test
    public void matchingTypeUpperCaseTest() throws IOException {
        String[] args = {
                "-t", "PROTEOFORM",
                "-i", "src/test/resources/Proteoforms/Valid/multipleLinesWithIsoforms.txt",
                "-o", "output/",
                "-m", "SUPERSET"};
        PathwayMatcher.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(121, output.size());
    }

    @Test
    public void invalidMatchingTypeTest() {
        exit.expectSystemExitWithStatus(Error.INVALID_MATCHING_TYPE.getCode());
        String[] args = {
                "-t", "proteoform",
                "-i", "src/test/resources/Proteoforms/multipleLinesWithIsoforms.txt",
                "-m", "blabla"};
        PathwayMatcher.main(args);
    }

    @Test
    public void couldNotWriteToOutputTest() {
        exit.expectSystemExitWithStatus(3);
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "/???",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);
    }

    @Test
    public void printHelpTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "-h"
        };
        PathwayMatcher.main(args);
    }

    @Test
    public void printHelpLongTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "--help",
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "/???",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);
    }

    @Test
    @Description("Should ignore the help request.")
    public void printHelpNotFirstArgumentTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "-t", "uniprot",
                "--help",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);
    }

    @Test
    public void printHelpWithOtherArgumentsTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "-h",
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "/???",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);
    }

    @Test
    public void printVersionShortTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "-v"
        };
        PathwayMatcher.main(args);
    }

    @Test
    public void printVersionLongTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "--version"
        };
        PathwayMatcher.main(args);
    }

    @Test
    public void printVersionLongFailTest() {
        exit.expectSystemExitWithStatus(0);
        String[] args = {
                "-version"
        };
        PathwayMatcher.main(args);
    }
}