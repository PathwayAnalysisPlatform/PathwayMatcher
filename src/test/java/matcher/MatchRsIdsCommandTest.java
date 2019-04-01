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

class MatchRsIdsCommandTest {
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

    static final String fileRsids = "src/test/resources/GeneticVariants/RsId/SingleSnp.txt";

    @Test
    void givenSubcommandMatchRsids_setsInputTypeToRsid_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "--input", fileRsids, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(InputType.RSID, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void givenSubcommandMatchRsids_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "--input", fileRsids, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchRsIdsCommand matchRsidsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsidsCommand.isWasExecuted(), "Failed to execute match-Rsids command");
    }

    @Test
    void givenSubcommandMatchRsidsWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-rsids"};
        Main.main(args);
        Main.MatchRsIdsCommand matchRsidsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertFalse(matchRsidsCommand.isWasExecuted(), "Executed match-rsids command by without arguments");
    }

    @Test
    void givenSubcommandMatchRsidsUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-RSIDS", "--input", fileRsids, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchRsIdsCommand matchRsidsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertFalse(matchRsidsCommand.isWasExecuted(), "Executed the match-rsids command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-RSIDS, --input, " + fileRsids + ", -o, " + testInfo.getTestMethod().get().getName() + "/\r\n" +
                "Did you mean: match-rsids"));
    }

    @Test
    void ShortInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "-i", fileRsids, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertEquals(fileRsids, matchRsIdsCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void LongInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "--input", fileRsids, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertEquals(fileRsids, matchRsIdsCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void MissingInput_requireInput_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request input file.");
    }

    @Test
    void NonExistentInput_showErrorMessage_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "-i", "blabla.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("The input file: blabla.txt was not found."), "Must not find input file.");
    }

    @Test
    void InputArgumentBroken_requiresInput_Test() {
        String[] args = {
                "match-rsids",
                "- i", "blabla.csv",
                "-o", "output/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request the input.");
    }

    @Test
    void ShortOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "-i", fileRsids, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", matchRsIdsCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void LongOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "-i", fileRsids, "--output", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", matchRsIdsCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void OutputArgumentWithoutPositionalArgument_sendsError_Test(TestInfo testInfo) {
        String[] args = {"match-rsids",
                "-i", fileRsids,
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
        String[] args = {"match-rsids", "-i", fileRsids};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertFalse(matchRsIdsCommand.isShowTopLevelPathways(), "It should have false as default value for flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysShortArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-rsids", "-i", fileRsids, "-T"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsIdsCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysLongArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-rsids", "-i", fileRsids, "--topLevelPathways"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsIdsCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void DoDefaultGraphShortArgument_setsDoUniprotGraph_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "-i", fileRsids, "-g", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsIdsCommand.isDoUniprotGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void DoDefaultGraphLongArgument_setsDoUniprotGraph_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "-i", fileRsids, "--graph", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsIdsCommand.isDoUniprotGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void GeneGraphShortArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-rsids", "-i", fileRsids, "-gg"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsIdsCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void GeneGraphLongArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-rsids", "-i", fileRsids, "--graphGene"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsIdsCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void NoGeneGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-rsids", "-i", fileRsids};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertFalse(matchRsIdsCommand.isDoGeneGraph(), "It should have false as default value for do gene graph.");
    }

    @Test
    void UniprotGraphShortArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-rsids", "-i", fileRsids, "-gu"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsIdsCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void UniprotGraphLongArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-rsids", "-i", fileRsids, "--graphUniprot"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsIdsCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void NoUniprotGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-rsids", "-i", fileRsids};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertFalse(matchRsIdsCommand.isDoUniprotGraph(), "It should have false as default value for do uniprot graph.");
    }

    @Test
    void ProteoformGraphShortArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-rsids", "-i", fileRsids, "-gp"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsIdsCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void ProteoformGraphLongArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-rsids", "-i", fileRsids, "--graphProteoform"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsIdsCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void NoProteoformGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-rsids", "-i", fileRsids};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertFalse(matchRsIdsCommand.isDoProteoformGraph(), "It should have false for do the Proteoform graph argument.");
    }

    @Test
    void SetDefaultPopulationSizeToProteoformSize_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "-i", fileRsids, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchRsIdsCommand matchRsIdsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();

        assertEquals(((ImmutableSetMultimap<String, String>) getSerializedObject("proteinsToReactions.gz")).keySet().size(),
                matchRsIdsCommand.getPopulationSize(),
                "Default population size for analysis should be total number of proteins.");
    }
}