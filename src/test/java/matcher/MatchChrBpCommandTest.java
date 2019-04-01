package matcher;

import com.google.common.collect.ImmutableSetMultimap;
import model.InputType;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static model.Mapping.getSerializedObject;
import static org.junit.jupiter.api.Assertions.*;

class MatchChrBpCommandTest {
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUpStreams() {
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

    static final String fileChrBp = "src/test/resources/GeneticVariants/Chr_Bp/Diabetes.txt";

    @Test
    void givenSubcommandMatchChrBp_setsInputTypeToChrBp_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "--input", fileChrBp, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(InputType.CHRBP, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void givenSubcommandMatchChrBp_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "--input", fileChrBp, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isWasExecuted(), "Failed to execute match-ChrBp command");
    }

    @Test
    void givenSubcommandMatchChrBpWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp"};
        Main.main(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertFalse(matchChrBpCommand.isWasExecuted(), "Execute match-chrbp command by without arguments");
    }

    @Test
    void givenSubcommandMatchChrBpUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-ChrBp", "--input", fileChrBp, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertFalse(matchChrBpCommand.isWasExecuted(), "Executed the match-ChrBp command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-ChrBp, --input, " + fileChrBp + ", -o, " + testInfo.getTestMethod().get().getName() + "/\r\n" +
                "Did you mean: match-chrbp"));
    }

    @Test
    void ShortInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "-i", fileChrBp, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertEquals(fileChrBp, matchChrBpCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void LongInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "--input", fileChrBp, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertEquals(fileChrBp, matchChrBpCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void MissingInput_requireInput_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request input file.");
    }

    @Test
    void NonExistentInput_showErrorMessage_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "-i", "blabla.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("The input file: blabla.txt was not found."), "Must not find input file.");
    }

    @Test
    void InputArgumentBroken_requiresInput_Test() {
        String[] args = {
                "match-chrbp",
                "- i", "blabla.csv",
                "-o", "output/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request the input.");
    }

    @Test
    void ShortOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "-i", fileChrBp, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", matchChrBpCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void LongOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "-i", fileChrBp, "--output", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", matchChrBpCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void OutputArgumentWithoutPositionalArgument_sendsError_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp",
                "-i", fileChrBp,
                "--output"
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        try{
            Main.commandLine.parse(args);
            fail("Should throw an exception");
        } catch(Exception ex){
            assertTrue(ex.getMessage().startsWith("Missing required parameter for option '--output'"));
        }
    }

    @Test
    void NoTopLevelPathwaysArgument_setsDefaultValueFalse_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertFalse(matchChrBpCommand.isShowTopLevelPathways(), "It should have false as default value for flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysShortArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp, "-T"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysLongArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp, "--topLevelPathways"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void DoDefaultGraphShortArgument_setsDoUniprotGraph_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "-i", fileChrBp, "-g", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isDoUniprotGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void DoDefaultGraphLongArgument_setsDoUniprotGraph_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "-i", fileChrBp, "--graph", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isDoUniprotGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void GeneGraphShortArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp, "-gg"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void GeneGraphLongArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp, "--graphGene"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void NoGeneGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertFalse(matchChrBpCommand.isDoGeneGraph(), "It should have false as default value for do gene graph.");
    }

    @Test
    void UniprotGraphShortArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp, "-gu"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void UniprotGraphLongArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp, "--graphUniprot"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void NoUniprotGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertFalse(matchChrBpCommand.isDoUniprotGraph(), "It should have false as default value for do uniprot graph.");
    }

    @Test
    void ProteoformGraphShortArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp, "-gp"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void ProteoformGraphLongArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp, "--graphProteoform"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void NoProteoformGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-chrbp", "-i", fileChrBp};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertFalse(matchChrBpCommand.isDoProteoformGraph(), "It should have false for do the Proteoform graph argument.");
    }

    @Test
    void SetDefaultPopulationSizeToProteoformSize_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "-i", fileChrBp, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchChrBpCommand matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();

        assertEquals(((ImmutableSetMultimap<String, String>) getSerializedObject("proteinsToReactions.gz")).keySet().size(),
                matchChrBpCommand.getPopulationSize(),
                "Default population size for analysis should be total number of proteins.");
    }
}