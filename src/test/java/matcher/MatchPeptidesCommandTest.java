package matcher;

import com.google.common.collect.ImmutableSetMultimap;
import model.InputType;
import model.Proteoform;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import picocli.CommandLine;

import java.io.*;

import static model.Mapping.getSerializedObject;
import static org.junit.jupiter.api.Assertions.*;

class MatchPeptidesCommandTest {
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

    static final String filePeptides = "src/test/resources/Peptides/singlePeptide.txt";
    static final String fileFasta = "src/test/resources/Peptides/single_Protein_Fasta.fasta";

    @Test
    void givenSubcommandMatchPeptides_setsInputTypeToPeptide_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "--input", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.main(args);
        assertEquals(InputType.PEPTIDE, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void givenSubcommandMatchPeptides_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "--input", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "-f", fileFasta};
        Main.main(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isWasExecuted(), "Failed to execute match-peptides command");
    }

    @Test
    void givenSubcommandMatchPeptidesWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-peptides"};
        Main.main(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertFalse(matchPeptidesCommand.isWasExecuted(), "Executed match-peptides command by without arguments");
    }

    @Test
    void givenSubcommandMatchPeptidesUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-PEPTIDES", "--input", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "-f", fileFasta};
        Main.main(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertFalse(matchPeptidesCommand.isWasExecuted(), "Executed the match-peptides command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-PEPTIDES, --input, " + filePeptides + ", -o, " + testInfo.getTestMethod().get().getName()));
    }

    @Test
    void givenSubcommandMatchPeptidesAndShortInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-i", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher()); 
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertEquals(filePeptides, matchPeptidesCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void givenSubcommandMatchPeptidesAndMissingInput_requireInput_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request input file.");
    }

    @Test
    void givenSubcommandMatchPeptidesAndMissingFasta_requireFasta_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-i", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--fasta=<fasta_path>'"), "Must request fasta file.");
    }

    @Test
    void ShortInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-i", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertEquals(filePeptides, matchPeptidesCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void LongInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "--input", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertEquals(filePeptides, matchPeptidesCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void MissingInput_requireInput_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request input file.");
    }

    @Test
    void NonExistentInput_showErrorMessage_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-i", "blabla.txt", "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("The input file: blabla.txt was not found."), "Must not find the input file.");
    }

    @Test
    void InputArgumentBroken_requiresInput_Test() {
        String[] args = {
                "match-peptides",
                "- i", "blabla.csv",
                "-o", "output/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required options [--input=<input_path>, --fasta=<fasta_path>]"), "Must request the input.");
    }

    @Test
    void ShortOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-i", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", matchPeptidesCommand.getOutput_prefix(), "Did not set the output path correctly.");
    }

    @Test
    void LongOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-i", filePeptides, "--output", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", matchPeptidesCommand.getOutput_prefix(), "Did not set the output path correctly.");
    }

    @Test
    void OutputArgumentWithoutPositionalArgument_sendsError_Test(TestInfo testInfo) {
        String[] args = {"match-peptides",
                "--fasta", fileFasta,
                "-i", filePeptides,
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
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertFalse(matchPeptidesCommand.isShowTopLevelPathways(), "It should have false as default value for flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysShortArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta, "-T"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysLongArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta, "--topLevelPathways"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void DoDefaultGraphShortArgument_setsDoProteoformGraph_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta, "-g", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isDoUniprotGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void DoDefaultGraphLongArgument_setsDoProteoformGraph_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta, "--graph", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isDoUniprotGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void GeneGraphShortArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta, "-gg"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void GeneGraphLongArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta, "--graphGene"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void NoGeneGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertFalse(matchPeptidesCommand.isDoGeneGraph(), "It should have false as default value for do gene graph.");
    }

    @Test
    void UniprotGraphShortArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta, "-gu"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void UniprotGraphLongArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta, "--graphUniprot"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void NoUniprotGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertFalse(matchPeptidesCommand.isDoUniprotGraph(), "It should have false as default value for do uniprot graph.");
    }

    @Test
    void ProteoformGraphShortArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta, "-gp"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void ProteoformGraphLongArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta, "--graphProteoform"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void NoProteoformGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertFalse(matchPeptidesCommand.isDoProteoformGraph(), "It should have false for do the Proteoform graph argument.");
    }

    @Test
    void SetDefaultPopulationSizeToProteoformSize_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-i", filePeptides, "-f", fileFasta, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();

        try {
            assertEquals(((ImmutableSetMultimap<Proteoform, String>) getSerializedObject("","proteinsToReactions.gz")).keySet().size(),
                    matchPeptidesCommand.getPopulationSize(),
                    "Default population size for analysis should be total number of proteins.");
        } catch (FileNotFoundException e) {
            fail("Should find the serialized file");
            e.printStackTrace();
        }
    }

    @Test
    void NoFastaArgument_requiresFasta_Test(TestInfo testInfo) {
        String[] args = {"match-peptides",
                "-i", filePeptides,
                "-o", testInfo.getTestMethod().get().getName() + "/"
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        try {
            Main.commandLine.parse(args);
            fail("Should send an exception for missing argument.");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().startsWith("Missing required option '--fasta=<fasta_path>'"), "Should require the fasta file");
        }
    }

    // Reads short fasta
    @Test
    void FastaShortArgument_setsFastaValue_Test(TestInfo testInfo) {
        String[] args = {"match-peptides",
                "-i", filePeptides,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-f", fileFasta
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        try {
            Main.commandLine.parse(args);
            Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
            assertEquals(fileFasta, matchPeptidesCommand.getFasta_path(), "Should set the provided value to the fasta path.");
        } catch (Exception ex) {
            fail("No exception is expected");
        }
    }

    @Test
    void FastaLongArgument_setsFastaValue_Test(TestInfo testInfo) {
        String[] args = {"match-peptides",
                "-i", filePeptides,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--fasta", fileFasta
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        try {
            Main.commandLine.parse(args);
            Main.MatchPeptidesCommand matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
            assertEquals(fileFasta, matchPeptidesCommand.getFasta_path(), "Should set the provided value to the fasta path.");
        } catch (Exception ex) {
            fail("No exception is expected");
        }
    }

    // Non existent fasta produces error
    @Test
    void InvalidFasta_sendsErrorMessage_Test(TestInfo testInfo) {
        String[] args = {
                "match-peptides",
                "-i", filePeptides,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--fasta", "blablabla"
        };
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Could not read the fasta file."));
    }
}