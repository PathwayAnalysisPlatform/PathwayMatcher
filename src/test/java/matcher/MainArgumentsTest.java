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

    private final String HEADER = "\r\n PathwayMatcher 1.9.0\r\n";
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
        assertTrue(outContent.toString().startsWith("PathwayMatcher 1.9.0"), "Wrong output to the console for version command.");
    }

    @Test
    void Matcher_givenLongVersionArgument_printsVersion_Test() {
        String[] args = {"--version"};
        Main.main(args);
        assertTrue(outContent.toString().startsWith("PathwayMatcher 1.9.0"), "Wrong output to the console for version command.");
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

    // Gene
    @Test
    void Matcher_givenSubcommandMatchGenes_setsInputTypeToGene_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "--input", "src/test/resources/Genes/Diabetes.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(InputType.GENE, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void Matcher_givenSubcommandMatchGenes_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "--input", "src/test/resources/Genes/Diabetes.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchGenes matchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(matchGenesCommand.isWasExecuted(), "Failed to execute match-genes command");
    }

    @Test
    void Matcher_givenSubcommandMatchGenesWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-genes"};
        Main.main(args);
        Main.MatchGenes matchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertFalse(matchGenesCommand.isWasExecuted(), "Execute match-genes command by without arguments");
    }

    @Test
    void Matcher_givenSubcommandMatchGenesUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-GENES", "--input", "src/test/resources/Genes/Diabetes.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchGenes matchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertFalse(matchGenesCommand.isWasExecuted(), "Executed the match-genes command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-GENES, --input, src/test/resources/Genes/Diabetes.txt, -o, Matcher_givenSubcommandMatchGenesUppercaps_doesNotRecognizeTheCommand_Test/\r\n" +
                "Did you mean: match-genes"));
    }

    // ChrBp
    static final String fileChrBp = "src/test/resources/GeneticVariants/Chr_Bp/Diabetes.txt";

    @Test
    void Matcher_givenSubcommandMatchChrBp_setsInputTypeToChrBp_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "--input", fileChrBp, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(InputType.CHRBP, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void Matcher_givenSubcommandMatchChrBp_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp", "--input", fileChrBp, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchChrBp matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isWasExecuted(), "Failed to execute match-ChrBp command");
    }

    @Test
    void Matcher_givenSubcommandMatchChrBpWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-chrbp"};
        Main.main(args);
        Main.MatchChrBp matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertFalse(matchChrBpCommand.isWasExecuted(), "Execute match-chrbp command by without arguments");
    }

    @Test
    void Matcher_givenSubcommandMatchChrBpUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-ChrBp", "--input", fileChrBp, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchChrBp matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertFalse(matchChrBpCommand.isWasExecuted(), "Executed the match-ChrBp command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-ChrBp, --input, " + fileChrBp + ", -o, " + testInfo.getTestMethod().get().getName() + "/\r\n" +
                "Did you mean: match-chrbp"));
    }

    // VCF
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
        Main.MatchVCF matchVcfCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(matchVcfCommand.isWasExecuted(), "Failed to execute match-vcf command");
    }

    @Test
    void Matcher_givenSubcommandMatchVcfWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-vcf"};
        Main.main(args);
        Main.MatchVCF matchVcfCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertFalse(matchVcfCommand.isWasExecuted(), "Executed match-vcf command by without arguments");
    }

    @Test
    void Matcher_givenSubcommandMatchVcfUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-VCF", "--input", fileVcf, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchVCF matchVcfCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertFalse(matchVcfCommand.isWasExecuted(), "Executed the match-vcf command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-VCF, --input, " + fileVcf + ", -o, " + testInfo.getTestMethod().get().getName() + "/\r\n" +
                "Did you mean: match-vcf"));
    }

    // Rsids
    static final String fileRsids = "src/test/resources/GeneticVariants/RsId/SingleSnp.txt";

    @Test
    void Matcher_givenSubcommandMatchRsids_setsInputTypeToRsid_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "--input", fileRsids, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(InputType.RSID, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void Matcher_givenSubcommandMatchRsids_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-rsids", "--input", fileRsids, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchRsIds matchRsidsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsidsCommand.isWasExecuted(), "Failed to execute match-Rsids command");
    }

    @Test
    void Matcher_givenSubcommandMatchRsidsWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-rsids"};
        Main.main(args);
        Main.MatchRsIds matchRsidsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertFalse(matchRsidsCommand.isWasExecuted(), "Executed match-rsids command by without arguments");
    }

    @Test
    void Matcher_givenSubcommandMatchRsidsUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-RSIDS", "--input", fileRsids, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchRsIds matchRsidsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertFalse(matchRsidsCommand.isWasExecuted(), "Executed the match-rsids command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-RSIDS, --input, " + fileRsids + ", -o, " + testInfo.getTestMethod().get().getName() + "/\r\n" +
                "Did you mean: match-rsids"));
    }

    // Proteoforms
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
        Main.MatchProteoforms matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isWasExecuted(), "Failed to execute match-proteoforms command");
    }

    @Test
    void Matcher_givenSubcommandMatchProteoformsWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-proteoforms"};
        Main.main(args);
        Main.MatchProteoforms matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertFalse(matchProteoformsCommand.isWasExecuted(), "Executed match-proteoforms command by without arguments");
    }

    @Test
    void Matcher_givenSubcommandMatchProteoformsUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-PROTEOFORMS", "--input", fileProteoforms, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchProteoforms matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertFalse(matchProteoformsCommand.isWasExecuted(), "Executed the match-proteoforms command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-PROTEOFORMS, --input, " + fileProteoforms + ", -o, " + testInfo.getTestMethod().get().getName() + "/\r\n" +
                "Did you mean: match-proteoforms"));
    }

    // Ensembl
    static final String fileEnsembl = "src/test/resources/Proteins/Ensembl/Diabetes.txt";

    @Test
    void Matcher_givenSubcommandMatchEnsembl_setsInputTypeToEnsembl_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "--input", fileEnsembl, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(InputType.ENSEMBL, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void Matcher_givenSubcommandMatchEnsembl_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl", "--input", fileEnsembl, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchEnsembl matchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(matchEnsemblCommand.isWasExecuted(), "Failed to execute match-ensembl command");
    }

    @Test
    void Matcher_givenSubcommandMatchEnsemblWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-ensembl"};
        Main.main(args);
        Main.MatchEnsembl matchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertFalse(matchEnsemblCommand.isWasExecuted(), "Executed match-ensembl command by without arguments");
    }

    @Test
    void Matcher_givenSubcommandMatchEnsemblUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-ENSEMBL", "--input", fileEnsembl, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchEnsembl matchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertFalse(matchEnsemblCommand.isWasExecuted(), "Executed the match-ensembl command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-ENSEMBL, --input, " + fileEnsembl + ", -o, " + testInfo.getTestMethod().get().getName() + "/\r\n" +
                "Did you mean: match-ensembl"));
    }

    // Peptides
    static final String filePeptides = "src/test/resources/Peptides/singlePeptide.txt";
    static final String fileFasta = "src/test/resources/Peptides/single_Protein_Fasta.fasta";

    @Test
    void Matcher_givenSubcommandMatchPeptides_setsInputTypeToPeptide_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "--input", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.main(args);
        assertEquals(InputType.PEPTIDE, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void Matcher_givenSubcommandMatchPeptides_executesIt_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "--input", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "-f", fileFasta};
        Main.main(args);
        Main.MatchPeptides matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertTrue(matchPeptidesCommand.isWasExecuted(), "Failed to execute match-peptides command");
    }

    @Test
    void Matcher_givenSubcommandMatchPeptidesWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo) {
        String[] args = {"match-peptides"};
        Main.main(args);
        Main.MatchPeptides matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertFalse(matchPeptidesCommand.isWasExecuted(), "Executed match-peptides command by without arguments");
    }

    @Test
    void Matcher_givenSubcommandMatchPeptidesUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-PEPTIDES", "--input", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "-f", fileFasta};
        Main.main(args);
        Main.MatchPeptides matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertFalse(matchPeptidesCommand.isWasExecuted(), "Executed the match-peptides command by mistake");
        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-PEPTIDES, --input, " + filePeptides + ", -o, " + testInfo.getTestMethod().get().getName()));
    }

    @Test
    void Matcher_givenSubcommandMatchPeptidesAndShortInputArgument_readsInputPath_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-i", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher()); // This two lines are to parse and not execute
        Main.commandLine.parse(args);
        Main.MatchPeptides matchPeptidesCommand = Main.commandLine.getSubcommands().get("match-peptides").getCommand();
        assertEquals(filePeptides, matchPeptidesCommand.getInput_path(), "Did not set the input path correctly.");
    }

    @Test
    void Matcher_givenSubcommandMatchPeptidesAndMissingInput_requireInput_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-o", testInfo.getTestMethod().get().getName() + "/", "--fasta", fileFasta};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request input file.");
    }

    @Test
    void Matcher_givenSubcommandMatchPeptidesAndMissingFasta_requireFasta_Test(TestInfo testInfo) {
        String[] args = {"match-peptides", "-i", filePeptides, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--fasta=<fasta_path>'"), "Must request fasta file.");
    }
}