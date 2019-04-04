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

class MatchProteoformsCommandTest {
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

    static final String fileProteoforms = "src/test/resources/Proteoforms/Simple/SingleProteoform.txt";

    @Test
    void Matcher_givenSubcommandMatchProteoforms_setsInputTypeToProteoform_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "--input", fileProteoforms, "-o", testInfo.getTestMethod().get().getName() + "/", "-m", "STRICT"};
        Main.main(args);
        assertEquals(InputType.PROTEOFORM, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void Matcher_givenSubcommandMatchProteoforms_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "--input", fileProteoforms, "-o", testInfo.getTestMethod().get().getName() + "/", "-m", "STRICT"};
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isWasExecuted(), "Failed to execute match-proteoforms command");
    }

    @Test
    void Matcher_givenSubcommandMatchProteoformsWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms"};
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertFalse(matchProteoformsCommand.isWasExecuted(), "Executed match-proteoforms command by without arguments");
    }

    @Test
    void Matcher_givenSubcommandMatchProteoformsUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-PROTEOFORMS", "--input", fileProteoforms, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertFalse(matchProteoformsCommand.isWasExecuted(), "Executed the match-proteoforms command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-PROTEOFORMS, --input, " + fileProteoforms + ", -o, " + testInfo.getTestMethod().get().getName() + "/\r\n" +
                "Did you mean: match-proteoforms"));
    }

    @Test
    void ShortInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(fileProteoforms, matchProteoformsCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void LongInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "--input", fileProteoforms, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(fileProteoforms, matchProteoformsCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void MissingInput_requireInput_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request input file.");
    }

    @Test
    void NonExistentInput_showErrorMessage_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "-i", "blabla.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("The input file: blabla.txt was not found."), "Must not find the input file.");
    }

    @Test
    void InputArgumentBroken_requiresInput_Test() {
        String[] args = {
                "match-proteoforms",
                "- i", "blabla.csv",
                "-o", "output/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request the input.");
    }

    @Test
    void ShortOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", matchProteoformsCommand.getOutput_prefix(), "Did not set the output path correctly.");
    }

    @Test
    void LongOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "--output", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", matchProteoformsCommand.getOutput_prefix(), "Did not set the output path correctly.");
    }

    @Test
    void OutputArgumentWithoutPositionalArgument_sendsError_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
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
        String[] args = {"match-proteoforms", "-i", fileProteoforms};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertFalse(matchProteoformsCommand.isShowTopLevelPathways(), "It should have false as default value for flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysShortArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "-T"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysLongArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "--topLevelPathways"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void DoDefaultGraphShortArgument_setsDoProteoformGraph_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "-g", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isDoProteoformGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void DoDefaultGraphLongArgument_setsDoProteoformGraph_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "--graph", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isDoProteoformGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void GeneGraphShortArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "-gg"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void GeneGraphLongArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "--graphGene"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void NoGeneGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-proteoforms", "-i", fileProteoforms};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertFalse(matchProteoformsCommand.isDoGeneGraph(), "It should have false as default value for do gene graph.");
    }

    @Test
    void UniprotGraphShortArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "-gu"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void UniprotGraphLongArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "--graphUniprot"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void NoUniprotGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-proteoforms", "-i", fileProteoforms};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertFalse(matchProteoformsCommand.isDoUniprotGraph(), "It should have false as default value for do uniprot graph.");
    }

    @Test
    void ProteoformGraphShortArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "-gp"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void ProteoformGraphLongArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "--graphProteoform"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void NoProteoformGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-proteoforms", "-i", fileProteoforms};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertFalse(matchProteoformsCommand.isDoProteoformGraph(), "It should have false for do the Proteoform graph argument.");
    }

    @Test
    void SetDefaultPopulationSizeToProteoformSize_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();

        assertEquals(((ImmutableSetMultimap<Proteoform, String>) getSerializedObject("proteoformsToReactions.gz")).keySet().size(),
                matchProteoformsCommand.getPopulationSize(),
                "Default population size for analysis should be total number of proteoforms.");
    }

    @Test
    void GivenHelpArgument_showsMatchTypeDefaultValue_Test(){
        String[] args = {"match-proteoforms"};
        Main.main(args);
        assertTrue(errContent.toString().contains("Default: SUBSET"));
    }

    @Test
    void NoMatchTypeArgument_keepsDefaultValue_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms", "-i", fileProteoforms, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.SUBSET, matchProteoformsCommand.getMatchType(), "The default value for match type should be subset");
    }

    // Reads each matchType strict
    @Test
    void MatchTypeShortStrictArgumentAndLowercaps_setsStrictMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "strict"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.STRICT, matchProteoformsCommand.getMatchType(), "Should set match type to strict");
    }

    @Test
    void MatchTypeLongStrictArgumentAndUppercaps_setsStrictMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "STRICT"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.STRICT, matchProteoformsCommand.getMatchType(), "Should set match type to strict");
    }

    @Test
    void MatchTypeShortSupersetArgumentAndLowercaps_setsSupersetMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "superset"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.SUPERSET, matchProteoformsCommand.getMatchType(), "Should set match type to superset");
    }

    @Test
    void MatchTypeLongSupersetArgumentAndUppercaps_setsSupersetMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "SUPERSET"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.SUPERSET, matchProteoformsCommand.getMatchType(), "Should set match type to superset");
    }

    @Test
    void MatchTypeShortSuperset_No_TypesArgumentAndLowercaps_setsSUPERSET_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "superset_no_types"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.SUPERSET_NO_TYPES, matchProteoformsCommand.getMatchType(), "Should set match type to superset_no_types");
    }

    @Test
    void MatchTypeLongSUPERSET_NO_TYPESArgumentAndUppercaps_setsSUPERSET_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "SUPERSET_NO_TYPES"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.SUPERSET_NO_TYPES, matchProteoformsCommand.getMatchType(), "Should set match type to superset_no_types");
    }

    @Test
    void MatchTypeShortSubsetArgumentAndLowercaps_setsSUBSETMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "subset"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.SUBSET, matchProteoformsCommand.getMatchType(), "Should set match type to subset");
    }

    @Test
    void MatchTypeLongSUBSETArgumentAndUppercaps_setsSUBSETMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "SUBSET"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.SUBSET, matchProteoformsCommand.getMatchType(), "Should set match type to subset");
    }

    @Test
    void MatchTypeShortSUBSET_NO_TYPESArgumentAndLowercaps_setsSUBSET_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "subset_no_types"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.SUBSET_NO_TYPES, matchProteoformsCommand.getMatchType(), "Should set match type to subset_no_types");
    }

    @Test
    void MatchTypeLongSUBSET_NO_TYPESArgumentAndUppercaps_setsSUBSET_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "SUBSET_NO_TYPES"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.SUBSET_NO_TYPES, matchProteoformsCommand.getMatchType(), "Should set match type to subset_no_types");
    }

    @Test
    void MatchTypeShortONEArgumentAndLowercaps_setsONEMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "one"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.ONE, matchProteoformsCommand.getMatchType(), "Should set match type to one");
    }

    @Test
    void MatchTypeLongONEArgumentAndUppercaps_setsONEMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "ONE"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.ONE, matchProteoformsCommand.getMatchType(), "Should set match type to one");
    }

    @Test
    void MatchTypeShortONE_NO_TYPESArgumentAndLowercaps_setsONE_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "one_no_types"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.ONE_NO_TYPES, matchProteoformsCommand.getMatchType(), "Should set match type to one_no_types");
    }

    @Test
    void MatchTypeLongONE_NO_TYPESArgumentAndUppercaps_setsONE_NO_TYPESMatchType_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--matchType", "ONE_NO_TYPES"
        };
        Main.main(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(MatchType.ONE_NO_TYPES, matchProteoformsCommand.getMatchType(), "Should set match type to one_no_types");
    }

    @Test
    void MatchTypeLongInvalidArgumentAndUppercaps_sendsErrorMessage_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
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

    @Test
    void GivenHelpArgument_showsRangeDefaultValue_Test(){
        String[] args = {"match-proteoforms"};
        Main.main(args);
        assertTrue(errContent.toString().contains("Default: 0"));
    }

    // Default range value
    @Test
    void NoRangeArgument_setsDefaultValue_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/"
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(0, matchProteoformsCommand.getRange(), "Range default value should be 0");
    }

    @Test
    void ShortRangeArgument_setsRangeValue_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-r", "1"
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(1L, matchProteoformsCommand.getRange(), "Did not set the range to the specified value");
    }

    @Test
    void LongRangeArgument_setsRangeValue_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "--range", "1"
        };
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchProteoformsCommand matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertEquals(1L, matchProteoformsCommand.getRange(), "Did not set the range argument to the specified value");
    }

    @Test
    void InvalidRangeArgument_sendsInvalidMessage_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms",
                "-i", fileProteoforms,
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
}