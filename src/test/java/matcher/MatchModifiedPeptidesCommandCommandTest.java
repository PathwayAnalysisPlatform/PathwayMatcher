package matcher;

import com.google.common.collect.ImmutableSetMultimap;
import model.InputType;
import model.MatchType;
import model.Proteoform;
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

class MatchModifiedPeptidesCommandCommandTest {

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

    static final String fileModifiedPeptides = "src/test/resources/ModifiedPeptides/SingleModifiedPeptide.txt";
    static final String fileFasta = "src/test/resources/Peptides/single_Protein_Fasta.fasta";

    @Test
    void MatchModifiedPeptides_setsInputTypeToPeptide_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "--input", fileModifiedPeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.main(args);
        assertEquals(InputType.MODIFIEDPEPTIDE, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void MatchModifiedPeptides_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "--input", fileModifiedPeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "-f", fileFasta};
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertTrue(matchModifiedPeptidesCommand.isWasExecuted(), "Failed to execute match-modified-peptides command");
    }

    @Test
    void MatchModifiedPeptidesWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides"};
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertFalse(matchModifiedPeptidesCommand.isWasExecuted(), "Executed match-modified-peptides command by without arguments");
    }

    @Test
    void MatchModifiedPeptidesUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-MODIFIED-PEPTIDES", "--input", fileModifiedPeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "-f", fileFasta};
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertFalse(matchModifiedPeptidesCommand.isWasExecuted(), "Executed the match-modified-peptides command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-MODIFIED-PEPTIDES, --input, " + fileModifiedPeptides + ", -o, " + testInfo.getTestMethod().get().getName()));
    }


    @Test
    void ShortInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(fileModifiedPeptides, matchModifiedPeptidesCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void LongInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "--input", fileModifiedPeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(fileModifiedPeptides, matchModifiedPeptidesCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void MissingInput_requireInput_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request input file.");
    }

    @Test
    void NonExistentInput_showErrorMessage_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "-i", "blabla.txt", "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("The input file: blabla.txt was not found."), "Must not find the input file.");
    }

    @Test
    void InputArgumentBroken_requiresInput_Test() {
        String[] args = {
                "match-modified-peptides",
                "- i", "blabla.csv",
                "-o", "output/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required options [--input=<input_path>, --fasta=<fasta_path>]"), "Must request the input.");
    }

    @Test
    void ShortOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", matchModifiedPeptidesCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void LongOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "--output", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", matchModifiedPeptidesCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void OutputArgumentWithoutPositionalArgument_sendsError_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "--fasta", fileFasta,
                "-i", fileModifiedPeptides,
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
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertFalse(matchModifiedPeptidesCommand.isShowTopLevelPathways(), "It should have false as default value for flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysShortArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "-T"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertTrue(matchModifiedPeptidesCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysLongArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "--topLevelPathways"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertTrue(matchModifiedPeptidesCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void DoDefaultGraphShortArgument_setsDoProteoformGraph_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "-g", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertTrue(matchModifiedPeptidesCommand.isDoProteoformGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void DoDefaultGraphLongArgument_setsDoProteoformGraph_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "--graph", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertTrue(matchModifiedPeptidesCommand.isDoProteoformGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void GeneGraphShortArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "-gg"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertTrue(matchModifiedPeptidesCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void GeneGraphLongArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "--graphGene"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertTrue(matchModifiedPeptidesCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void NoGeneGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertFalse(matchModifiedPeptidesCommand.isDoGeneGraph(), "It should have false as default value for do gene graph.");
    }

    @Test
    void UniprotGraphShortArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "-gu"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertTrue(matchModifiedPeptidesCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void UniprotGraphLongArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "--graphUniprot"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertTrue(matchModifiedPeptidesCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void NoUniprotGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertFalse(matchModifiedPeptidesCommand.isDoUniprotGraph(), "It should have false as default value for do uniprot graph.");
    }

    @Test
    void ProteoformGraphShortArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "-gp"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertTrue(matchModifiedPeptidesCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void ProteoformGraphLongArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "--graphProteoform"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertTrue(matchModifiedPeptidesCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void NoProteoformGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertFalse(matchModifiedPeptidesCommand.isDoProteoformGraph(), "It should have false for do the Proteoform graph argument.");
    }

    @Test
    void SetDefaultPopulationSizeToProteoformSize_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();

        assertEquals(((ImmutableSetMultimap<Proteoform, String>) getSerializedObject("proteoformsToReactions.gz")).keySet().size(),
                matchModifiedPeptidesCommand.getPopulationSize(),
                "Default population size for analysis should be total number of proteoforms.");
    }

    @Test
    void GivenHelpArgument_showsMatchTypeDefaultValue_Test(){
        String[] args = {"match-modified-peptides"};
        Main.main(args);
        assertTrue(errContent.toString().contains("Default: SUBSET"));
    }

    @Test
    void NoMatchTypeArgument_keepsDefaultValue_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides", "-i", fileModifiedPeptides, "-f", fileFasta, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.SUBSET, matchModifiedPeptidesCommand.getMatchType(), "The default value for match type should be subset");
    }

    // Reads each matchType strict
    @Test
    void MatchTypeShortStrictArgumentAndLowercaps_setsStrictMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "strict"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.STRICT, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to strict");
    }

    @Test
    void MatchTypeLongStrictArgumentAndUppercaps_setsStrictMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "STRICT"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.STRICT, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to strict");
    }

    @Test
    void MatchTypeShortSupersetArgumentAndLowercaps_setsSupersetMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "superset"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.SUPERSET, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to superset");
    }

    @Test
    void MatchTypeLongSupersetArgumentAndUppercaps_setsSupersetMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "SUPERSET"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.SUPERSET, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to superset");
    }

    @Test
    void MatchTypeShortSuperset_No_TypesArgumentAndLowercaps_setsSUPERSET_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "superset_no_types"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.SUPERSET_NO_TYPES, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to superset_no_types");
    }

    @Test
    void MatchTypeLongSUPERSET_NO_TYPESArgumentAndUppercaps_setsSUPERSET_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "SUPERSET_NO_TYPES"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.SUPERSET_NO_TYPES, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to superset_no_types");
    }

    @Test
    void MatchTypeShortSubsetArgumentAndLowercaps_setsSUBSETMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "subset"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.SUBSET, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to subset");
    }

    @Test
    void MatchTypeLongSUBSETArgumentAndUppercaps_setsSUBSETMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "SUBSET"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.SUBSET, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to subset");
    }

    @Test
    void MatchTypeShortSUBSET_NO_TYPESArgumentAndLowercaps_setsSUBSET_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "subset_no_types"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.SUBSET_NO_TYPES, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to subset_no_types");
    }

    @Test
    void MatchTypeLongSUBSET_NO_TYPESArgumentAndUppercaps_setsSUBSET_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "SUBSET_NO_TYPES"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.SUBSET_NO_TYPES, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to subset_no_types");
    }

    @Test
    void MatchTypeShortONEArgumentAndLowercaps_setsONEMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "one"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.ONE, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to one");
    }

    @Test
    void MatchTypeLongONEArgumentAndUppercaps_setsONEMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "ONE"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.ONE, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to one");
    }

    @Test
    void MatchTypeShortONE_NO_TYPESArgumentAndLowercaps_setsONE_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "one_no_types"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.ONE_NO_TYPES, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to one_no_types");
    }

    @Test
    void MatchTypeLongONE_NO_TYPESArgumentAndUppercaps_setsONE_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "ONE_NO_TYPES"
        };
        Main.main(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(MatchType.ONE_NO_TYPES, matchModifiedPeptidesCommand.getMatchType(), "Should set match type to one_no_types");
    }

    @Test
    void MatchTypeLongInvalidArgumentAndUppercaps_sendsErrorMessage_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "blabla"
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        try {
            Main.commandLine.parse(args);
            fail("Should throw and exception for wrong type.");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().startsWith("Invalid value for option '--matchType'"), "Should send an invalid match type message");
        }
    }

    // Default range value
    @Test
    void NoRangeArgument_setsDefaultValue_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/"
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(0, matchModifiedPeptidesCommand.getRange(), "Range default value should be 0");
    }

    @Test
    void ShortRangeArgument_setsRangeValue_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-r", "1"
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(1L, matchModifiedPeptidesCommand.getRange(), "Did not set the range to the specified value");
    }

    @Test
    void LongRangeArgument_setsRangeValue_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--range", "1"
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
        assertEquals(1L, matchModifiedPeptidesCommand.getRange(), "Did not set the range argument to the specified value");
    }

    @Test
    void InvalidRangeArgument_sendsInvalidMessage_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-f", fileFasta,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--range", "blabla"
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        try {
            Main.commandLine.parse(args);
            fail("Should throw an expection for wrong type.");
        } catch (Exception ex) {
            assertTrue(ex.getMessage().startsWith("Invalid value for option '--range'"), "Should send an invalid range type message");
        }
    }

    @Test
    void GivenHelpArgument_showsRangeDefaultValue_Test(){
        String[] args = {"match-modified-peptides"};
        Main.main(args);
        assertTrue(errContent.toString().contains("Default: 0"));
    }

    @Test
    void NoFastaArgument_requiresFasta_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
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
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-f", fileFasta
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        try {
            Main.commandLine.parse(args);
            Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
            assertEquals(fileFasta, matchModifiedPeptidesCommand.getFasta_path(), "Should set the provided value to the fasta path.");
        } catch (Exception ex) {
            fail("No exception is expected");
        }
    }

    @Test
    void FastaLongArgument_setsFastaValue_Test(TestInfo testInfo) {
        String[] args = {"match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--fasta", fileFasta
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        try {
            Main.commandLine.parse(args);
            Main.MatchModifiedPeptidesCommand matchModifiedPeptidesCommand = Main.commandLine.getSubcommands().get("match-modified-peptides").getCommand();
            assertEquals(fileFasta, matchModifiedPeptidesCommand.getFasta_path(), "Should set the provided value to the fasta path.");
        } catch (Exception ex) {
            fail("No exception is expected");
        }
    }

    // Non existent fasta produces error
    @Test
    void InvalidFasta_sendsErrorMessage_Test(TestInfo testInfo) {
        String[] args = {
                "match-modified-peptides",
                "-i", fileModifiedPeptides,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--fasta", "blablabla"
        };
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Could not read the fasta file."));
    }
}