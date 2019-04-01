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

class MatchEnsemblCommandTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
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

    static final String fileEnsembl = "src/test/resources/Proteins/Ensembl/Diabetes.txt";

    @Test
    void MatchEnsembl_setsInputTypeToEnsembl_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "--input", fileEnsembl, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(InputType.ENSEMBL, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void MatchEnsembl_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "--input", fileEnsembl, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchEnsemblCommand matchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(matchEnsemblCommand.isWasExecuted(), "Failed to execute match-ensembl command");
    }

    @Test
    void MatchEnsemblWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl"};
        Main.main(args);
        Main.MatchEnsemblCommand matchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertFalse(matchEnsemblCommand.isWasExecuted(), "Execute match-ensembl command by without arguments");
    }

    @Test
    void MatchEnsemblUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-ENSEMBL", "--input", fileEnsembl, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchEnsemblCommand matchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertFalse(matchEnsemblCommand.isWasExecuted(), "Executed the match-ensembl command by mistake");

        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertFalse(matchProteoformsCommand.isWasExecuted(), "Executed the match-proteoforms command by mistake.");

        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-ENSEMBL, --input, " + fileEnsembl + ", -o, " + testInfo.getTestMethod().get().getName() + "/\r\n" +
                "Did you mean: match-ensembl"));
    }

    @Test
    void ShortInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertEquals(fileEnsembl, MatchEnsemblCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void LongInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "--input", fileEnsembl, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertEquals(fileEnsembl, MatchEnsemblCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void MissingInput_requireInput_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request input file.");
    }

    @Test
    void NonExistentInput_showErrorMessage_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "-i", "blabla.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("The input file: blabla.txt was not found."), "Must not find input file.");
    }

    @Test
    void InputArgumentBroken_requiresInput_Test() {
        String[] args = {
                "match-ensembl",
                "- i", "blabla.csv",
                "-o", "output/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request the input.");
    }

    @Test
    void ShortOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", MatchEnsemblCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void LongOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "--output", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", MatchEnsemblCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void OutputArgumentWithoutPositionalArgument_sendsError_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl",
                "-i", fileEnsembl,
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
        String[] args = {"match-ensembl", "-i", fileEnsembl};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertFalse(MatchEnsemblCommand.isShowTopLevelPathways(), "It should have false as default value for flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysShortArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "-T"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(MatchEnsemblCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysLongArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "--topLevelPathways"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(MatchEnsemblCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void DoDefaultGraphShortArgument_setsDoUniprotGraph_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "-g", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(MatchEnsemblCommand.isDoUniprotGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void DoDefaultGraphLongArgument_setsDoUniprotGraph_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "--graph", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(MatchEnsemblCommand.isDoUniprotGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void GeneGraphShortArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "-gg"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(MatchEnsemblCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void GeneGraphLongArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "--graphGene"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(MatchEnsemblCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void NoGeneGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-ensembl", "-i", fileEnsembl};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertFalse(MatchEnsemblCommand.isDoGeneGraph(), "It should have false as default value for do gene graph.");
    }

    @Test
    void EnsemblGraphShortArgument_setsDoEnsemblGraph_Test() {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "-gu"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(MatchEnsemblCommand.isDoUniprotGraph(), "It didn't set the flag to do the ensembl graph.");
    }

    @Test
    void EnsemblGraphLongArgument_setsDoEnsemblGraph_Test() {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "--graphUniprot"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(MatchEnsemblCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void NoEnsemblGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-ensembl", "-i", fileEnsembl};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertFalse(MatchEnsemblCommand.isDoUniprotGraph(), "It should have false as default value for do uniprot graph.");
    }

    @Test
    void ProteoformGraphShortArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "-gp"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(MatchEnsemblCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void ProteoformGraphLongArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "--graphProteoform"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(MatchEnsemblCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void NoProteoformGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-ensembl", "-i", fileEnsembl};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertFalse(MatchEnsemblCommand.isDoProteoformGraph(), "It should have false for do the Proteoform graph argument.");
    }

    @Test
    void SetDefaultPopulationSizeToProteoformSize_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "-i", fileEnsembl, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchEnsemblCommand MatchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();

        assertEquals(((ImmutableSetMultimap<String, String>) getSerializedObject("proteinsToReactions.gz")).keySet().size(),
                MatchEnsemblCommand.getPopulationSize(),
                "Default population size for analysis should be total number of proteins.");
    }
}