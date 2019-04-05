package matcher;

import model.InputType;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MainArgumentsTest {

    private static String searchFile = "search.tsv";

    private final String HEADER = "\r\n PathwayMatcher 1.9.1\r\n";
    private final String EXPECTED_HELP_MESSAGE_START = HEADER + "\r\nUsage: java -jar PathwayMatcher.jar [-hv] [COMMAND]";

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUpStreams(TestInfo testInfo) {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams(TestInfo testInfo) {
        System.setOut(originalOut);
        System.setErr(originalErr);

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
    void Matcher_givenShortVersionArgument_printsVersion_Test() {
        String[] args = {"-v"};
        Main.main(args);
        assertTrue(outContent.toString().startsWith("PathwayMatcher 1.9.1"), "Wrong output to the console for version command.");
    }

    @Test
    void Matcher_givenLongVersionArgument_printsVersion_Test() {
        String[] args = {"--version"};
        Main.main(args);
        assertTrue(outContent.toString().startsWith("PathwayMatcher 1.9.1"), "Wrong output to the console for version command.");
    }

    @Test
    void Matcher_givenInvalidOneDashVersionArgument_Fails_Test() {
        String[] args = {"-version"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Unknown option: -ersion"), "Should show error message failing to recognize the option.");
    }

    @Test
    void Matcher_subcommandsRegistered_Test(TestInfo testInfo) {
        CommandLine commandLine = new CommandLine(new Main.PathwayMatcher());
        Map<String, CommandLine> commandMap = commandLine.getSubcommands();
        assertEquals(10, commandMap.size());
    }

    @Test
    void Matcher_matchUniprotSubcommandRegistered_Test() {
        CommandLine commandLine = new CommandLine(new Main.PathwayMatcher());
        Map<String, CommandLine> commandMap = commandLine.getSubcommands();
        assertTrue(commandMap.get("match-uniprot").getCommand() instanceof Main.MatchUniprotCommand, "match-uniprot");
    }

    @Test
    void Matcher_NoArguments_showsUsageText_test() {
        String[] args = {};
        Main.main(args);
        assertTrue(outContent.toString().startsWith(EXPECTED_HELP_MESSAGE_START), "Should show the general usage text");
    }

    @Test
    void Matcher_shortHelpArgument_printsHelpMessage_Test() {
        String[] args = {
                "-h"
        };
        Main.main(args);
        assertTrue(outContent.toString().startsWith(EXPECTED_HELP_MESSAGE_START), "Help message was not shown.");
    }

    @Test
    void Matcher_longHelpArgument_printsHelpMessage_Test() {
        String[] args = {
                "--help"
        };
        Main.main(args);
        assertTrue(outContent.toString().startsWith(EXPECTED_HELP_MESSAGE_START), "Help message was not shown.");
    }

    @Test
    void Matcher_helpArgumentWithOtherArguments_printsUnkownArgumentsMessage_Test() {
        String[] args = {
                "--help",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "/???",
                "-T",
                "--graph"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Unknown options: -i, "), "Help message was not shown.");
    }

    @Test
    void Matcher_argumentsFirstWithHelpSecond_printsUnknownArgumentsMessage_Test() {
        String[] args = {
                "-i", "file.txt",
                "--help",
                "-T",
                "--graph"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Unknown options: -i, "), "Help message was not shown.");
    }

    @Test
    void Matcher_shortHelpArgumentWithOtherArguments_printsUnknownArgumentMessage_Test() {
        String[] args = {
                "-h",
                "-o", "/???",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-T",
                "--graph"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Unknown options: -o, /???"), "Help message was not shown.");
    }

    @Test
    void MatcherHelp_showsReactomeVersion_Test(){
        String args[] = {};
        Main.main(args);
        assertTrue(outContent.toString().contains("Includes mapping from Reactome v68"));
    }

    static final String fileGenes = "src/test/resources/Genes/Diabetes.txt";

    // The next tests apply for any Command descendant of the class MatchSubcommand
    @Test
    void withOutputPrefixOnlyFile_createsFiles_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        String[] args = {"match-genes", "-i", fileGenes, "-o", testName + "_"};
        Main.main(args);
        File fileSearch = new File(testName + "_search.tsv");
        File fileAnalysis= new File(testName + "_analysis.tsv");
        assertTrue(fileSearch.exists(), "Did not create the search file.");
        assertTrue(fileAnalysis.exists(), "Did not create the analysis file.");
        fileSearch.delete();
        fileAnalysis.delete();
    }

    @Test
    void withOutputPrefixFileAndPath_createsFiles_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        String[] args = {"match-genes", "-i", fileGenes, "-o", testName + "/" + testName + "_"};
        Main.main(args);
        assertTrue(new File(testName + "/" + testName + "_" + "search.tsv").exists(), "Did not create the search file.");
    }

    @Test
    void withOutputPrefixOnlyPath_createsFiles_Test(TestInfo testInfo){
        String testName = testInfo.getTestMethod().get().getName();
        String [] args = {"match-genes", "-i", fileGenes, "-o", testName + "/"};
        Main.main(args);
        assertTrue(new File(testName + "/search.tsv").exists(), "Did not create the search file.");
    }

    @Test
    void withMappingArgumentToEmptyDirectory_showMissingFilesMessage_Test(TestInfo testInfo){
        String testName = testInfo.getTestMethod().get().getName();
        File directory = new File(testName + "/");
        directory.mkdirs();
        String []args = {"match-genes", "-i", fileGenes, "--mapping", testName + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Could not find the file: genesToProteins.gz at the location: " + testName +"/" ));

    }
}