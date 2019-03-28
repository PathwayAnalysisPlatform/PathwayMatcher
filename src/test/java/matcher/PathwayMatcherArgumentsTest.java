package matcher;

import com.google.common.io.Files;
import model.Error;
import model.InputType;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.List;

import static matcher.tools.ListDiff.anyMatches;
import static org.junit.jupiter.api.Assertions.*;

public class PathwayMatcherArgumentsTest {

    private static String searchFile = "output/search.tsv";

    private final String HEADER = "\r\n PathwayMatcher 1.9.0\r\n";
    private final String EXPECTED_HELP_MESSAGE_START = HEADER + "\r\nUsage: PathwayMatcher [";

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams(TestInfo testInfo) {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams(TestInfo testInfo) {
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
    public void Matcher_NoArguments_requiresInput_test() {
        PathwayMatcher.main(new String[0]);
        assertTrue(errContent.toString().startsWith("Missing required options [--inputType=<inputType>, --input=<input_path>]"), "Should ask for the input file when no arguments are provided.");
    }

    @Test
    public void Matcher_shortHelpArgument_printsHelpMessage_Test() {
        String[] args = {
                "-h"
        };
        PathwayMatcher.main(args);
        assertTrue(outContent.toString().startsWith(EXPECTED_HELP_MESSAGE_START), "Help message was not shown.");
    }

    @Test
    public void Matcher_longHelpArgument_printsHelpMessage_Test() {
        String[] args = {
                "--help"
        };
        PathwayMatcher.main(args);
        assertTrue(outContent.toString().startsWith(EXPECTED_HELP_MESSAGE_START), "Help message was not shown.");
    }

    @Test
    public void Matcher_helpArgumentWithOtherArguments_printsHelpMessage_Test() {
        String[] args = {
                "--help",
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "/???",
                "-T",
                "--graph"};
        PathwayMatcher.main(args);
        assertTrue(outContent.toString().startsWith(EXPECTED_HELP_MESSAGE_START), "Help message was not shown.");
    }

    @Test
    public void Matcher_argumentsFirstWithHelpSecond_printsHelpMessage_Test() {
        String[] args = {
                "-t", "uniprot",
                "--help",
                "-i", "file.txt",
                "-T",
                "--graph"};
        PathwayMatcher.main(args);
        assertTrue(outContent.toString().startsWith(EXPECTED_HELP_MESSAGE_START), "Help message was not shown.");
    }

    @Test
    public void Matcher_shortHelpArgumentWithOtherArguments_printsHelpMessage_Test() {
        String[] args = {
                "-h",
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "/???",
                "-T",
                "--graph"};
        PathwayMatcher.main(args);
        assertTrue(outContent.toString().startsWith("\r\n PathwayMatcher 1.9.0\r\n\r\nUsage: PathwayMatcher ["), "Help message was not shown.");
    }

    @Test
    public void Matcher_missingRequiredArgumentInputType_requestsInputType_Test() {
        String[] args = {"-i", "input.txt"};
        PathwayMatcher.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--inputType=<inputType>'"), "Must request the input type.");
    }

    @Test
    public void Matcher_missingArgumentForInputTypeWithAnotherArgumentNext_sendsUnkownTypeMessage_Test() {
        String[] args = {"-t", "-i", "src\\test\\resources\\Proteins\\UniProt\\AKT1.txt"};
        PathwayMatcher.main(args);
        assertTrue(errContent.toString().startsWith("Invalid value for option '--inputType': expected one of [GENE, ENSEMBL, UNIPROT, PROTEOFORM, PEPTIDE, MODIFIEDPEPTIDE, VCF, RSID, CHRBP] (case-insensitive) but was '-i'"), "Needs to get confused with the arguments and request the input type");
    }

    @Test
    public void Matcher_receivesInputTypeUniprotSmallcaps_setsInputTypeToUniprot_Test(TestInfo testInfo) {
        String[] args = {"-t", "uniprot", "-i", "src/test/resources/Proteins/UniProt/AKT1.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);

        assertEquals(pathwayMatcher.getInputType(), InputType.UNIPROT, "Failed to read the correct input type.");
    }

    @Test
    public void Matcher_receivesInputTypeUniprotUppercaps_setsInputTypeUniprot_Test(TestInfo testInfo) {
        String[] args = {"-t", "UNIPROT", "-i", "src/test/resources/Proteins/UniProt/AKT1.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.UNIPROT, "Failed to read the correct input type.");
    }

    @Test
    public void Matcher_receivesInputTypeGeneLowerCase_setsInputTypeGENE_Test(TestInfo testInfo){
        String[] args = {"-t", "gene", "-i", "src/test/resources/Genes/Diabetes.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.GENE, "Failed to read the correct input type GENE");
    }

    @Test
    public void Matcher_receivesInputTypeGeneUpperCase_setsInputTypeGENE_Test(TestInfo testInfo){
        String[] args = {"-t", "GENE", "-i", "src/test/resources/Genes/Diabetes.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.GENE, "Failed to read the correct input type GENE");
    }

    @Test
    public void Matcher_receivesInputTypeChrBpLowerCase_setsInputTypeCHRBP_Test(TestInfo testInfo){
        String[] args = {"-t", "chrbp", "-i", "src/test/resources/GeneticVariants/Chr_Bp/Diabetes.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.CHRBP, "Failed to read the correct input type CHRBP");
    }

    @Test
    public void Matcher_receivesInputTypeChrBpUpperCase_setsInputTypeCHRBP_Test(TestInfo testInfo){
        String[] args = {"-t", "CHRBP", "-i", "src/test/resources/GeneticVariants/Chr_Bp/Diabetes.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.CHRBP, "Failed to read the correct input type CHRBP");
    }

    @Test
    public void Matcher_receivesInputTypeVcfLowerCase_setsInputTypeVCF_Test(TestInfo testInfo){
        String[] args = {"-t", "vcf", "-i", "src/test/resources/GeneticVariants/VCF/CysticFibrosis.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.VCF, "Failed to read the correct input type CHRBP");
    }

    @Test
    public void Matcher_receivesInputTypeVcfUpperCase_setsInputTypeVCF_Test(TestInfo testInfo){
        String[] args = {"-t", "VCF", "-i", "src/test/resources/GeneticVariants/VCF/CysticFibrosis.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.VCF, "Failed to read the correct input type VCF");
    }

    @Test
    public void Matcher_receivesInputTypeRsidUpperCase_setsInputTypeRsid_Test(TestInfo testInfo){
        String[] args = {"-t", "RSID", "-i", "src/test/resources/GeneticVariants/RsId/SingleSnp.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.RSID, "Failed to read the correct input type VCF");
    }

    @Test
    public void Matcher_receivesInputTypeProteoformUpperCase_setsInputTypeProteoform_Test(TestInfo testInfo){
        String[] args = {"-t", "proteoform", "-i", "src/test/resources/Proteoforms/Simple/SingleProteoform.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.PROTEOFORM, "Failed to read the correct input type PROTEOFORM");
    }

    @Test
    public void Matcher_receivesInputTypeEnsemblUpperCase_setsInputTypeEnsembl_Test(TestInfo testInfo){
        String[] args = {"-t", "ensembl", "-i", "src/test/resources/Proteins/Ensembl/Diabetes.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.ENSEMBL, "Failed to read the correct input type ENSEMBL");
    }

    @Test
    public void Matcher_receivesInputTypePeptideUpperCase_setsInputTypePeptide_Test(TestInfo testInfo){
        String[] args = {"-t", "proteoform", "-i", "src/test/resources/Proteoforms/Simple/SingleProteoform.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.PROTEOFORM, "Failed to read the correct input type PROTEOFORM");
    }

    @Test
    public void Matcher_receivesInputTypeModifiedPeptideLowerCase_setsInputTypeModifiedPeptide_Test(TestInfo testInfo){
        String[] args = {"-t", "modifiedpeptide", "-i", "src/test/resources/ModifiedPeptides/SingleModifiedPeptide.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertEquals(pathwayMatcher.getInputType(), InputType.MODIFIEDPEPTIDE, "Failed to read the correct input type MODIFIEDPEPTIDE");
    }

    @Test
    public void Matcher_withInputTypePeptideAndMissingRequiredArgumentFasta_requireFasta_Test(TestInfo testInfo){
        String[] args = {"-t", "peptide", "-i", "src/test/resources/Peptides/singlePeptide2.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--fasta=<fasta_path>'"), "Must request fasta file.");
    }

    @Test
    public void Matcher_withInputTypeModifiedPeptideAndMissingRequiredArgumentFasta_requireFasta_Test(TestInfo testInfo){
        String[] args = {"-t", "modifiedpeptide", "-i", "src/test/resources/ModifiedPeptides/SingleModifiedPeptide.txt", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher pathwayMatcher = new PathwayMatcher();
        pathwayMatcher.callCommandLine(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--fasta=<fasta_path>'"), "Must request fasta file.");
    }
    
    @Test
    public void Matcher_missingRequiredOption_i_requiresOption_Test(TestInfo testInfo) {
        String[] args = { "-t", "uniprot", "-o", testInfo.getTestMethod().get().getName() + "/"};
        PathwayMatcher.main(args);
        assertTrue(errContent.toString().startsWith("Missing required option '--input=<input_path>'"), "Must request the input type.");
    }

    @Test
    public void inputArgumentBroken_Test() {
        // Fails because the input file can not be read, not because of configuration
        exit.expectSystemExitWithStatus(Error.MISSING_ARGUMENT.getCode());
        String[] args = {
                "-", "t", "uniprot",
                "-i", "blabla.csv",
                "-o", "output/"};
        PathwayMatcher.main(args);
    }

    @Test
    public void inputFileNotFound_Test() {
        // Fails because the input file can not be read, not because of configuration
        exit.expectSystemExitWithStatus(Error.COULD_NOT_READ_INPUT_FILE.getCode());
        String[] args = {
                "-t", "uniprot",
                "-i", "blabla.csv",
                "-o", "output/"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_i_Test() {
        exit.expectSystemExitWithStatus(Error.MISSING_ARGUMENT.getCode());
        String[] args = {"-t", "rsidList", "-i"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_o_Test() {
        exit.expectSystemExitWithStatus(Error.MISSING_ARGUMENT.getCode());
        String[] args = {"-t", "rsidList", "-o", "-i", "src/test/resources/Proteins/UniProt/uniprot-all.list"};
        PathwayMatcher.main(args);
    }

    @Test
    public void missingArgumentForOption_r_Test() {
        exit.expectSystemExitWithStatus(Error.MISSING_ARGUMENT.getCode());
        String[] args = {"-t", "rsidList", "-r"};
        PathwayMatcher.main(args);
    }

    @Test
    public void matchingTypeTest() throws IOException {
        String[] args = {
                "-t", "proteoform",
                "-i", "src/test/resources/Proteoforms/Valid/multipleLinesWithIsoforms.txt",
                "-o", "output/",
                "-m", "superset"};
        PathwayMatcher.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());

        assertTrue(anyMatches("P08235-1;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));
        assertTrue(anyMatches("P08235-2;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));
        assertTrue(anyMatches("P08235-3;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));
        assertTrue(anyMatches("P08235-4;\tP08235\tR-HSA-376419\t.+\tR-HSA-212436", output));

        assertTrue(anyMatches("P02545-2;00046:395\tP02545\tR-HSA-5244669\t.+\tR-HSA-1640170", output));
        assertEquals(121, output.size());
    }

    @Test
    public void matchingTypeUpperCaseTest() throws IOException {
        String[] args = {
                "-t", "PROTEOFORM",
                "-i", "src/test/resources/Proteoforms/Valid/multipleLinesWithIsoforms.txt",
                "-o", "output/",
                "-m", "SUPERSET"};
        PathwayMatcher.main(args);

        List<String> output = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(121, output.size());
    }

    @Test
    public void invalidMatchingTypeTest() {
        exit.expectSystemExitWithStatus(Error.INVALID_MATCHING_TYPE.getCode());
        String[] args = {
                "-t", "proteoform",
                "-i", "src/test/resources/Proteoforms/multipleLinesWithIsoforms.txt",
                "-m", "blabla"};
        PathwayMatcher.main(args);
    }

    @Test
    public void couldNotWriteToOutputTest() {
        exit.expectSystemExitWithStatus(3);
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "/???",
                "-tlp",
                "--graph"};
        PathwayMatcher.main(args);
    }


    @Test
    public void printVersionShortTest() {
        String[] args = {"-v"};
        PathwayMatcher.main(args);
        assertTrue(outContent.toString().startsWith("PathwayMatcher 1.9.0"), "Wrong output to the console for version command.");
    }

    @Test
    public void printVersionLongTest() {
        String[] args = {"--version"};
        PathwayMatcher.main(args);
        assertTrue(outContent.toString().startsWith("PathwayMatcher 1.9.0"), "Wrong output to the console for version command.");
    }

    @Test
    public void printVersionLongOneDashTest() {
        String[] args = {"-version"};
        PathwayMatcher.main(args);
        assertTrue(outContent.toString().startsWith("PathwayMatcher 1.9.0"), "Wrong output to the console for version command.");
    }
    // TODO: Test show default values of parameters

}