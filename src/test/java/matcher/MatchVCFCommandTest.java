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

class MatchVCFCommandTest {
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

    static final String fileVcf = "src/test/resources/GeneticVariants/VCF/CysticFibrosis.txt";

    @Test
    void Matcher_givenSubcommandMatchVcf_setsInputTypeToVcf_Test(TestInfo testInfo) {
        String[] args = {"match-vcf", "--input", fileVcf, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(InputType.VCF, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void Matcher_givenSubcommandMatchVcf_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-vcf", "--input", fileVcf, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchVCFCommand matchVcfCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(matchVcfCommand.isWasExecuted(), "Failed to execute match-vcf command");
    }

    @Test
    void Matcher_givenSubcommandMatchVcfWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-vcf"};
        Main.main(args);
        Main.MatchVCFCommand matchVcfCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertFalse(matchVcfCommand.isWasExecuted(), "Executed match-vcf command by without arguments");
    }

    @Test
    void Matcher_givenSubcommandMatchVcfUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-VCF", "--input", fileVcf, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchVCFCommand matchVcfCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertFalse(matchVcfCommand.isWasExecuted(), "Executed the match-vcf command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-VCF, --input, " + fileVcf + ", -o, " + testInfo.getTestMethod().get().getName() + "/\r\n" +
                "Did you mean: match-vcf"));
    }

    @Test
    void ShortInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-vcf", "-i", fileVcf, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertEquals(fileVcf, MatchVCFCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void LongInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-vcf", "--input", fileVcf, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertEquals(fileVcf, MatchVCFCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void MissingInput_requireInput_Test(TestInfo testInfo) {
        String[] args = {"match-vcf", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request input file.");
    }

    @Test
    void NonExistentInput_showErrorMessage_Test(TestInfo testInfo) {
        String[] args = {"match-vcf", "-i", "blabla.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("The input file: blabla.txt was not found."), "Must not find input file.");
    }

    @Test
    void InputArgumentBroken_requiresInput_Test() {
        String[] args = {
                "match-vcf",
                "- i", "blabla.csv",
                "-o", "output/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request the input.");
    }

    @Test
    void ShortOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-vcf", "-i", fileVcf, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", MatchVCFCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void LongOutputArgument_setsOutputPath_Test(TestInfo testInfo) {
        String[] args = {"match-vcf", "-i", fileVcf, "--output", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertEquals(testInfo.getTestMethod().get().getName() + "/", MatchVCFCommand.getOutput_path(), "Did not set the output path correctly.");
    }

    @Test
    void OutputArgumentWithoutPositionalArgument_sendsError_Test(TestInfo testInfo) {
        String[] args = {"match-vcf",
                "-i", fileVcf,
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
        String[] args = {"match-vcf", "-i", fileVcf};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertFalse(MatchVCFCommand.isShowTopLevelPathways(), "It should have false as default value for flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysShortArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-vcf", "-i", fileVcf, "-T"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(MatchVCFCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void TopLevelPathwaysLongArgument_setsShowTopLevelPathwways_Test() {
        String[] args = {"match-vcf", "-i", fileVcf, "--topLevelPathways"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(MatchVCFCommand.isShowTopLevelPathways(), "It didn't set the flag to show the top level pathways.");
    }

    @Test
    void DoDefaultGraphShortArgument_setsDoUniprotGraph_Test(TestInfo testInfo) {
        String[] args = {"match-vcf", "-i", fileVcf, "-g", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(MatchVCFCommand.isDoUniprotGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void DoDefaultGraphLongArgument_setsDoUniprotGraph_Test(TestInfo testInfo) {
        String[] args = {"match-vcf", "-i", fileVcf, "--graph", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(MatchVCFCommand.isDoUniprotGraph(), "It didn't set the flag to do the default graph.");
    }

    @Test
    void GeneGraphShortArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-vcf", "-i", fileVcf, "-gg"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(MatchVCFCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void GeneGraphLongArgument_setsDoGeneGraph_Test() {
        String[] args = {"match-vcf", "-i", fileVcf, "--graphGene"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(MatchVCFCommand.isDoGeneGraph(), "It didn't set the flag to do the gene graph.");
    }

    @Test
    void NoGeneGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-vcf", "-i", fileVcf};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertFalse(MatchVCFCommand.isDoGeneGraph(), "It should have false as default value for do gene graph.");
    }

    @Test
    void UniprotGraphShortArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-vcf", "-i", fileVcf, "-gu"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(MatchVCFCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void UniprotGraphLongArgument_setsDoUniprotGraph_Test() {
        String[] args = {"match-vcf", "-i", fileVcf, "--graphUniprot"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(MatchVCFCommand.isDoUniprotGraph(), "It didn't set the flag to do the uniprot graph.");
    }

    @Test
    void NoUniprotGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-vcf", "-i", fileVcf};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertFalse(MatchVCFCommand.isDoUniprotGraph(), "It should have false as default value for do uniprot graph.");
    }

    @Test
    void ProteoformGraphShortArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-vcf", "-i", fileVcf, "-gp"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(MatchVCFCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void ProteoformGraphLongArgument_setsDoProteoformGraph_Test() {
        String[] args = {"match-vcf", "-i", fileVcf, "--graphProteoform"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(MatchVCFCommand.isDoProteoformGraph(), "It didn't set the flag to do the Proteoform graph.");
    }

    @Test
    void NoProteoformGraphArgument_keepsDefaultValue_Test() {
        String[] args = {"match-vcf", "-i", fileVcf};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher());
        Main.commandLine.parse(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertFalse(MatchVCFCommand.isDoProteoformGraph(), "It should have false for do the Proteoform graph argument.");
    }

    @Test
    void SetDefaultPopulationSizeToProteoformSize_Test(TestInfo testInfo) {
        String[] args = {"match-vcf", "-i", fileVcf, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchVCFCommand MatchVCFCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();

        assertEquals(((ImmutableSetMultimap<String, String>) getSerializedObject("proteinsToReactions.gz")).keySet().size(),
                MatchVCFCommand.getPopulationSize(),
                "Default population size for analysis should be total number of proteins.");
    }
}