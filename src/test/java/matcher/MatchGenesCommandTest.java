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

class MatchGenesCommandTest {
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

    static final String fileGenes = "src/test/resources/Genes/Diabetes.txt";

    @Test
    void MatchGenes_setsInputTypeToGene_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "--input", fileGenes, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(InputType.GENE, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void MatchGenes_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "--input", fileGenes, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchGenesCommand matchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(matchGenesCommand.isWasExecuted(), "Failed to execute match-genes command");
    }

    @Test
    void Matcher_givenSubcommandMatchGenesWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-genes"};
        Main.main(args);
        Main.MatchGenesCommand matchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertFalse(matchGenesCommand.isWasExecuted(), "Execute match-genes command by without arguments");
    }

    @Test
    void Matcher_givenSubcommandMatchGenesUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-GENES", "--input", fileGenes, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchGenesCommand matchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertFalse(matchGenesCommand.isWasExecuted(), "Executed the match-genes command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-GENES, --input, " + fileGenes + ", -o, " + testInfo.getTestMethod().get().getName() + "/\r\n" +
                "Did you mean: match-genes"));
    }

    @Test
    void ShortInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "-i", fileGenes, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertEquals(fileGenes, MatchGenesCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void LongInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "--input", fileGenes, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertEquals(fileGenes, MatchGenesCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void MissingInput_requireInput_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request input file.");
    }

    @Test
    void NonExistentInput_showErrorMessage_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "-i", "blabla.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("The input file: blabla.txt was not found."), "Must not find input file.");
    }

    @Test
    void InputArgumentBroken_requiresInput_Test() {
        String[] args = {
                "match-genes",
                "- i", "blabla.csv",
                "-o", "output/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request the input.");
    }

    @Test
    void ShortOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "-i", fileGenes, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", MatchGenesCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void LongOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "-i", fileGenes, "--output", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", MatchGenesCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void OutputArgumentWithoutPositionalArgument_sendsError_Test(TestInfo testInfo) {
        String[] args = {"match-genes",
                "-i", fileGenes,
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
        String[] args = {"match-genes", "-i", fileGenes};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertFalse(MatchGenesCommand.isShowTopLevelPathways(), "It should have false as default value for flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysShortArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-genes", "-i", fileGenes, "-T"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(MatchGenesCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysLongArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-genes", "-i", fileGenes, "--topLevelPathways"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(MatchGenesCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void DoDefaultGraphShortArgument_setsDoGeneGraph_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "-i", fileGenes, "-g", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(MatchGenesCommand.isDoGeneGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void DoDefaultGraphLongArgument_setsDoGeneGraph_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "-i", fileGenes, "--graph", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(MatchGenesCommand.isDoGeneGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void GeneGraphShortArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-genes", "-i", fileGenes, "-gg"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(MatchGenesCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void GeneGraphLongArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-genes", "-i", fileGenes, "--graphGene"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(MatchGenesCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void NoGeneGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-genes", "-i", fileGenes};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertFalse(MatchGenesCommand.isDoGeneGraph(), "It should have false as default value for do gene graph.");
    }

    @Test
    void UniprotGraphShortArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-genes", "-i", fileGenes, "-gu"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand matchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(matchGenesCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void UniprotGraphLongArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-genes", "-i", fileGenes, "--graphUniprot"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand matchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(matchGenesCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void NoUniprotGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-genes", "-i", fileGenes};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand matchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertFalse(matchGenesCommand.isDoUniprotGraph(), "It should have false as default value for do uniprot graph.");
    }

    @Test
    void ProteoformGraphShortArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-genes", "-i", fileGenes, "-gp"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(MatchGenesCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void ProteoformGraphLongArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-genes", "-i", fileGenes, "--graphProteoform"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(MatchGenesCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void NoProteoformGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-genes", "-i", fileGenes};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertFalse(MatchGenesCommand.isDoProteoformGraph(), "It should have false for do the Proteoform graph argument.");
    }

    @Test
    void SetDefaultPopulationSizeToProteoformSize_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "-i", fileGenes, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchGenesCommand MatchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();

        assertEquals(((ImmutableSetMultimap<String, String>) getSerializedObject("proteinsToReactions.gz")).keySet().size(),
                MatchGenesCommand.getPopulationSize(),
                "Default population size for analysis should be total number of proteins.");
    }
}