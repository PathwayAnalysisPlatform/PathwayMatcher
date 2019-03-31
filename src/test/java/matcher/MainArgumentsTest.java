package matcher;

import com.google.common.io.Files;
import com.sun.org.glassfish.gmbal.Description;
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
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import static matcher.tools.ListDiff.anyMatches;
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
    void Matcher_exampleVerifyCommandArgumentValuesTest(TestInfo testInfo){
        String[] args = {"match-uniprot", "--input", "src\\test\\resources\\Proteins\\UniProt\\AKT1.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.commandLine = new CommandLine(new Main.PathwayMatcher()); // This two lines are to parse and not execute
        Main.commandLine.parse(args);

//        myMain.main(args); // To parse the arguments and execute
        Main.PathwayMatcher command = Main.commandLine.getCommand();
        System.out.println(((Main.MatchUniprot)Main.commandLine.getSubcommands().get("match-uniprot").getCommand()).getInput_path());
        Main.MatchUniprot matchUniprotSubcommand = Main.commandLine.getSubcommands().get("match-uniprot").getCommand();
        System.out.println("Input path for match-uniprot: " + matchUniprotSubcommand.getInput_path());
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
        assertTrue(commandMap.get("match-uniprot").getCommand() instanceof Main.MatchUniprot, "match-uniprot");
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


    // Uniprot
    @Test
    void Matcher_givenSubcommandMatchUniprot_setsInputTypeToUniprot_Test(TestInfo testInfo) {
        String[] args = {"match-uniprot", "--input", "src\\test\\resources\\Proteins\\UniProt\\AKT1.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(InputType.UNIPROT, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void Matcher_givenSubcommandMatchUniprot_executesIt_Test(TestInfo testInfo){
        String[] args = {"match-uniprot", "--input", "src\\test\\resources\\Proteins\\UniProt\\AKT1.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchUniprot matchUniprotCommand = Main.commandLine.getSubcommands().get("match-uniprot").getCommand();
        assertTrue(matchUniprotCommand.isWasExecuted(), "Failed to execute match-uniprot command");
    }

    @Test
    void Matcher_givenSubcommandMatchUniprotWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo){
        String[] args = {"match-uniprot"};
        Main.main(args);
        Main.MatchUniprot matchUniprotCommand = Main.commandLine.getSubcommands().get("match-uniprot").getCommand();
        assertFalse(matchUniprotCommand.isWasExecuted(), "Execute match-uniprot command by without arguments");
    }

    @Test
    void Matcher_givenSubcommandMatchUniprotUppercaps_doesNotRecognizeTheCommand_Test(TestInfo testInfo) {
        String[] args = {"MATCH-UNIPROT", "--input", "src\\test\\resources\\Proteins\\UniProt\\AKT1.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchUniprot matchUniprotCommand = Main.commandLine.getSubcommands().get("match-uniprot").getCommand();
        assertFalse(matchUniprotCommand.isWasExecuted(), "Executed the match-uniprot command by mistake");

        Main.MatchProteoforms matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertFalse(matchProteoformsCommand.isWasExecuted(), "Executed the match-proteoforms command by mistake.");

        assertTrue(errContent.toString().startsWith("Unmatched arguments: MATCH-UNIPROT, --input, src\\test\\resources\\Proteins\\UniProt\\AKT1.txt, -o, Matcher_givenSubcommandMatchUniprotUppercaps_doesNotRecognizeTheCommand_Test/\r\n" +
                "Did you mean: match-uniprot or match-proteoforms or match-vcf?"));
    }

    // Gene
    @Test
    void Matcher_givenSubcommandMatchGenes_setsInputTypeToGene_Test(TestInfo testInfo) {
        String[] args = {"match-genes", "--input", "src/test/resources/Genes/Diabetes.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(InputType.GENE, Main.inputType, "Failed to set the correct input type according to the subcommand.");
    }

    @Test
    void Matcher_givenSubcommandMatchGenes_executesIt_Test(TestInfo testInfo){
        String[] args = {"match-genes", "--input", "src/test/resources/Genes/Diabetes.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchGenes matchGenesCommand = Main.commandLine.getSubcommands().get("match-genes").getCommand();
        assertTrue(matchGenesCommand.isWasExecuted(), "Failed to execute match-genes command");
    }

    @Test
    void Matcher_givenSubcommandMatchGenesWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo){
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
    void Matcher_givenSubcommandMatchChrBp_executesIt_Test(TestInfo testInfo){
        String[] args = {"match-chrbp", "--input", fileChrBp, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchChrBp matchChrBpCommand = Main.commandLine.getSubcommands().get("match-chrbp").getCommand();
        assertTrue(matchChrBpCommand.isWasExecuted(), "Failed to execute match-ChrBp command");
    }

    @Test
    void Matcher_givenSubcommandMatchChrBpWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo){
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
    void Matcher_givenSubcommandMatchVcf_executesIt_Test(TestInfo testInfo){
        String[] args = {"match-vcf", "--input", fileVcf, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchVCF matchVcfCommand = Main.commandLine.getSubcommands().get("match-vcf").getCommand();
        assertTrue(matchVcfCommand.isWasExecuted(), "Failed to execute match-vcf command");
    }

    @Test
    void Matcher_givenSubcommandMatchVcfWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo){
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
    void Matcher_givenSubcommandMatchRsids_executesIt_Test(TestInfo testInfo){
        String[] args = {"match-rsids", "--input", fileRsids, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchRsIds matchRsidsCommand = Main.commandLine.getSubcommands().get("match-rsids").getCommand();
        assertTrue(matchRsidsCommand.isWasExecuted(), "Failed to execute match-Rsids command");
    }

    @Test
    void Matcher_givenSubcommandMatchRsidsWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo){
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
    void Matcher_givenSubcommandMatchProteoforms_executesIt_Test(TestInfo testInfo){
        String[] args = {"match-proteoforms", "--input", fileProteoforms, "-o", testInfo.getTestMethod().get().getName() + "/", "-m", "STRICT"};
        Main.main(args);
        Main.MatchProteoforms matchProteoformsCommand = Main.commandLine.getSubcommands().get("match-proteoforms").getCommand();
        assertTrue(matchProteoformsCommand.isWasExecuted(), "Failed to execute match-proteoforms command");
    }

    @Test
    void Matcher_givenSubcommandMatchProteoformsWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo){
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
    void Matcher_givenSubcommandMatchEnsembl_executesIt_Test(TestInfo testInfo){
        String[] args = {"match-ensembl", "--input", fileEnsembl, "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        Main.MatchEnsembl matchEnsemblCommand = Main.commandLine.getSubcommands().get("match-ensembl").getCommand();
        assertTrue(matchEnsemblCommand.isWasExecuted(), "Failed to execute match-ensembl command");
    }

    @Test
    void Matcher_givenSubcommandMatchEnsemblWithoutArguments_doesNotExecuteIt_Test(TestInfo testInfo){
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

    @Test
    void Matcher_receivesInputTypePeptideUpperCase_setsInputTypePeptide_Test(TestInfo testInfo) {
        String[] args = {"-t", "proteoform", "-i", "src/test/resources/Proteoforms/Simple/SingleProteoform.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(Main.inputType, InputType.PROTEOFORM, "Failed to read the correct input type PROTEOFORM");
    }

    @Test
    void Matcher_receivesInputTypeModifiedPeptideLowerCase_setsInputTypeModifiedPeptide_Test(TestInfo testInfo) {
        String[] args = {"-t", "modifiedpeptide", "-i", "src/test/resources/ModifiedPeptides/SingleModifiedPeptide.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertEquals(Main.inputType, InputType.MODIFIEDPEPTIDE, "Failed to read the correct input type MODIFIEDPEPTIDE");
    }

    @Test
    void Matcher_withInputTypePeptideAndMissingRequiredArgumentFasta_requireFasta_Test(TestInfo testInfo) {
        String[] args = {"-t", "peptide", "-i", "src/test/resources/Peptides/singlePeptide2.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--fasta=<fasta_path>'"), "Must request fasta file.");
    }

    @Test
    void Matcher_withInputTypeModifiedPeptideAndMissingRequiredArgumentFasta_requireFasta_Test(TestInfo testInfo) {
        String[] args = {"-t", "modifiedpeptide", "-i", "src/test/resources/ModifiedPeptides/SingleModifiedPeptide.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--fasta=<fasta_path>'"), "Must request fasta file.");
    }

    @Test
    void Matcher_missingRequiredOption_i_requiresOption_Test(TestInfo testInfo) {
        String[] args = {"-t", "uniprot", "-o", testInfo.getTestMethod().get().getName() + "/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request the input type.");
    }

    @Test
    void Matcher_inputTypeArgumentBroken_requiresInputType_Test() {
        String[] args = {
                "-", "t", "uniprot",
                "-i", "blabla.csv",
                "-o", "output/"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--inputType=<inputType>'"), "Must request the input type.");
    }

    @Test
    void Matcher_givenInvalidInputType_Fails_Test() {
        String[] args = {
                "-t", "blabla",
                "-i", "src/test/resources/Proteoforms/multipleLinesWithIsoforms.txt"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Invalid value for option '--inputType': expected one of"), "Must request a valid input type.");
    }

    @Test
    void Matcher_whenInputFileDoesNotExist_messageInputFileNotFound_Test() {
        String args = "-t uniprot -i blabla.csv -o output/";
        Main.main(args.split(" "));
        assertTrue(outContent.toString().startsWith("The input file: blabla.csv was not found."), "Error reading input file message not shown.");
    }

    @Test
    void Matcher_missingArgumentForOptionInputType_requestsArgumentValue_Test() {
        String[] args = {"-t", "rsidList", "-i"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Invalid value for option '--inputType':"), "Must request the input type positional parameter.");
    }

    @Test
    @Description("Should fail by taking as '-i' as positional argument of '-o', then failing to find the '-i' argument.")
    void Matcher_missingArgumentForOptionOutput_requestsArgumentInputTypeByMistake_Test() {
        String[] args = {"-t", "rsid", "-o", "-i", "src/test/resources/Proteins/UniProt/uniprot-all.list"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request the input type.");
    }

    @Test
    void Matcher_missingArgumentForOptionRange_requests_Test() {
        String[] args = {"-t", "rsid", "-r"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Missing required parameter for option '--range' (<range>)"), "Must request the positional argument for the range parameter.");
    }

    @Test
    void Matcher_givenLowerCapsMatchingType_Works_Test(TestInfo testInfo) throws IOException {
        String[] args = {
                "-t", "proteoform",
                "-i", "src/test/resources/Proteoforms/Valid/multipleLinesWithIsoforms.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "superset"};
        Main.main(args);

        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());

        assertTrue(anyMatches("P08235-1;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));
        assertTrue(anyMatches("P08235-2;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));
        assertTrue(anyMatches("P08235-3;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));
        assertTrue(anyMatches("P08235-4;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));

        assertTrue(anyMatches("P02545-2;00046:395\tP02545\tR-HSA-5244669\t.+\tR-HSA-1640170", output));
        assertEquals(121, output.size());
    }

    @Test
    void Matcher_givenUpperCaseMatchingType_Works_Test(TestInfo testInfo) throws IOException {
        String[] args = {
                "match-proteoforms",
                "-i", "src/test/resources/Proteoforms/Valid/multipleLinesWithIsoforms.txt",
                "-o", testInfo.getTestMethod().get().getName() + "/",
                "-m", "SUPERSET"};
        Main.main(args);
        List<String> output = Files.readLines(new File(testInfo.getTestMethod().get().getName() + "/" + searchFile), Charset.defaultCharset());
        assertEquals(121, output.size());
    }

    @Test
    void Matcher_givenInvalidMatchingType_Fails_Test() {
        String[] args = {
                "-t", "proteoform",
                "-i", "src/test/resources/Proteoforms/multipleLinesWithIsoforms.txt",
                "-m", "blabla"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("Invalid value for option '--matchType': expected one of"), "Must request a valid matching type.");
    }

    @Test
    void couldNotWriteToOutputTest() {
        exit.expectSystemExitWithStatus(3);
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "/???",
                "-T",
                "--graph"};
        Main.main(args);
        assertTrue(errContent.toString().startsWith("There was an error creating/writing to the output files."), "Must show error of output files.");
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
    // TODO: Test show default values of parameters

}