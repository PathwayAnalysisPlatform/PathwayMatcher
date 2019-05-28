package matcher.tools;

import com.google.common.io.Files;
import methods.matching.ProteoformMatching;
import methods.matching.ProteoformMatchingStrict;
import model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeAll;

import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SensitivityTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;
    static Mapping mapping;
    static Proteoform proteoform1;
    static Proteoform proteoform2;

    @BeforeAll
    static void setUpAll() {
        try {
            mapping = new Mapping(InputType.PROTEOFORM, false, "");
            proteoform1 = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:95");
            proteoform2 = ProteoformFormat.SIMPLE.getProteoform("P01308;00798:31,00798:43");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

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
    void getModType_givenSiteAndResidueS_returnPSIMOD00046() {
        assertEquals("00046", Sensitivity.getModType("S"));
    }

    @Test
    void getModType_givenSiteAndResidueT_returnPSIMOD00047() {
        assertEquals("00047", Sensitivity.getModType("T"));
    }

    @Test
    void getModType_givenResidueY_returnPSIMOD00048() {
        assertEquals("00048", Sensitivity.getModType("Y"));
    }

    @Test
    void getModType_givenNotResidueSTY_throwException() {
        assertEquals("00000", Sensitivity.getModType("HOLA"));
    }

    @Test
    void getPTM_givenLineWithResidueS_returnPTM() {
        String line = "Q68D10,323,S";
        try {
            assertEquals("00046:323", Sensitivity.getPTM(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw exception");
        }
    }

    @Test
    void getPTM_givenLineWithResidueT_returnPTM() {
        String line = "Q9H1B7,633,T";
        try {
            assertEquals("00047:633", Sensitivity.getPTM(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw exception");
        }
    }

    @Test
    void getPTM_givenLineWithResidueY_returnPTM() {
        String line = "Q9H1C7,64,Y";
        try {
            assertEquals("00048:64", Sensitivity.getPTM(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw exception");
        }
    }

    @Test
    void getPTM_givenInvalidSiteWithLetters_throwsNumberFormatException() {
        String line = "Q9H1C7,asv,Y";
        Assertions.assertThrows(NumberFormatException.class, () -> {
            Sensitivity.getPTM(line);
        });
    }

    @Test
    void getPTM_givenInvalidSiteWithNegative_throwsParseException() {
        String line = "Q9H1C7,-2,Y";
        Assertions.assertThrows(ParseException.class, () -> {
            Sensitivity.getPTM(line);
        });
    }

    @Test
    void createProteoform_givenValidLineWithS_returnProteoform() {
        String line = "Q68D51,305,S";
        try {
            assertEquals("Q68D51;00046:305", Sensitivity.getSimpleProteoformStringFromPhosphoModification(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw an exception.");
        }
    }

    @Test
    void createProteoform_givenValidLineWithT_returnProteoform() {
        String line = "Q9H1A4,701,T";
        try {
            assertEquals("Q9H1A4;00047:701", Sensitivity.getSimpleProteoformStringFromPhosphoModification(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw an exception.");
        }
    }

    @Test
    void createProteoform_givenValidLineWithY_returnProteoform() {
        String line = "P46783,12,Y";
        try {
            assertEquals("P46783;00048:12", Sensitivity.getSimpleProteoformStringFromPhosphoModification(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw an exception.");
        }
    }

    @Test
    void createProteoformList_givenPhosphositesList_addsCorrectNumberOfProteoforms() {
        assertEquals(9, Sensitivity.createProteoformList("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt").size());
    }

    @Test
    void createProteoformList_givenPhosphositesList_eachHasOneModification() {
        HashSet<Proteoform> proteoformList = Sensitivity.createProteoformList("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt");
        for (Proteoform proteoform : proteoformList) {
            assertEquals(1, proteoform.getPtms().size());
        }
    }

    @Test
    void createProteoformList_givenPhosphositesWithSerine_hasProteoform() {
        try {
            HashSet<Proteoform> proteoformSet = new HashSet<>(Sensitivity.createProteoformList("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt"));
            assertTrue(proteoformSet.contains(ProteoformFormat.SIMPLE.getProteoform("Q9UK32;00046:389")));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformList_givenPhosphositesWithThreonine_hasProteoform() {
        try {
            HashSet<Proteoform> proteoformSet = new HashSet<>(Sensitivity.createProteoformList("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt"));
            assertTrue(proteoformSet.contains(ProteoformFormat.SIMPLE.getProteoform("Q9UK32;00047:368")));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformList_givenPhosphositesWithTyrosine_hasProteoform() {
        try {
            HashSet<Proteoform> proteoformSet = new HashSet<>(Sensitivity.createProteoformList("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt"));
            assertTrue(proteoformSet.contains(ProteoformFormat.SIMPLE.getProteoform("Q9ULH0;00048:1096")));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformListAggregated_givenPhosphositesList_addsCorrectNumberOfProteoforms() {
        try {
            assertEquals(4, Sensitivity.createProteoformListAggregated("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt").size());
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformListAggregated_givenProteoformWithMultipleModifications_works() {
        try {
            List<Proteoform> proteoformList = Sensitivity.createProteoformListAggregated("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt");
            assertEquals(4, proteoformList.get(0).getPtms().size());
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformListAggregated_lastPhosphositeInList_AddedToLastProteoform() {
        try {
            HashSet<Proteoform> proteoformSet = new HashSet<>(Sensitivity.createProteoformListAggregated("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt"));
            assertTrue(proteoformSet.contains(ProteoformFormat.SIMPLE.getProteoform("Q9ULH0;00048:1096")));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformListAggregated_lastProteoformWithMultipleMods_AddedToLastProteoform() {
        try {
            HashSet<Proteoform> proteoformSet = new HashSet<>(Sensitivity.createProteoformListAggregated("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites_lastProteoformWithTwoMods.txt"));
            assertTrue(proteoformSet.contains(ProteoformFormat.SIMPLE.getProteoform("Q9ULH0;00048:758,00048:1096")));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void alterProteoforms_getsCollectionProteoformsWithModifications_modifiesValue() throws ParseException {
        HashSet<Proteoform> proteoforms = new HashSet<>();
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("A2RUS2;00046:472,00046:490"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696"));
        proteoforms = (HashSet<Proteoform>) proteoforms.stream().map(proteoform -> Sensitivity.alterProteoformSimple(proteoform)).collect(Collectors.toSet());
        assertFalse(proteoforms.contains(ProteoformFormat.SIMPLE.getProteoform("A2RUS2;00046:472,00046:490")));
        assertFalse(proteoforms.contains(ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696")));
    }

    @Test
    void alterProteoforms_getsUnmodifiedProteoform_doesNotGetAltered() throws ParseException {
        HashSet<Proteoform> proteoforms = new HashSet<>();
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;"));
        proteoforms = (HashSet<Proteoform>) proteoforms.stream().map(proteoform -> Sensitivity.alterProteoformSimple(proteoform)).collect(Collectors.toSet());
        assertTrue(proteoforms.contains(ProteoformFormat.SIMPLE.getProteoform("O14974")));
    }

    @Test
    void alterProteoforms_getsProteoformWithOneModification_altersType() throws ParseException {
        HashSet<Proteoform> proteoforms = new HashSet<>(1);
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696"));
        proteoforms = (HashSet<Proteoform>) proteoforms.stream().map(proteoform -> Sensitivity.alterProteoformSimple(proteoform)).collect(Collectors.toSet());
        assertFalse(proteoforms.contains("O14974;00047:696"));
        for (Proteoform proteoform : proteoforms) {
            assertEquals("00000", proteoform.getPtms().get(0).getKey());
        }
    }

    @Test
    void alterProteoforms_getsProteoformWithOneModification_altersCoordinate() throws ParseException {
        HashSet<Proteoform> proteoforms = new HashSet<>(1);
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696"));
        proteoforms = (HashSet<Proteoform>) proteoforms.stream().map(proteoform -> Sensitivity.alterProteoformSimple(proteoform)).collect(Collectors.toSet());
        assertFalse(proteoforms.contains("O14974;00047:696"));
        for (Proteoform proteoform : proteoforms) {
            assertEquals(701L, proteoform.getPtms().get(0).getValue());
        }
    }

    @Test
    void getProteoformSample_givenPercentage_returnsNumberFloorDown() throws ParseException {
        HashSet<Proteoform> proteoforms = new HashSet<>(6);
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14966;"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14966;00113:202,00113:203"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;00046:473"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;00046:852,00047:696"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696"));

        HashSet<Proteoform> sample = Sensitivity.getProteoformSample(proteoforms, 33.0);
        assertEquals(1, sample.size());
    }

    @Test
    void getProteoformSample_givenList_returnsElements() throws ParseException {
        HashSet<Proteoform> proteoforms = new HashSet<>(6);
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14966;"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14966;00113:202,00113:203"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;00046:473"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;00046:852,00047:696"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696"));

        HashSet<Proteoform> sample = Sensitivity.getProteoformSample(proteoforms, 34.0);
        assertEquals(2, sample.size());
    }

    @Test
    void alterProteoformSimple_givenProteoform_originalStaysIntact() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O14966;00113:202,00113:203");
        Proteoform alteredProteoform = Sensitivity.alterProteoformSimple(proteoform);
        Proteoform proteoform2 = ProteoformFormat.SIMPLE.getProteoform("O14966;00113:202,00113:203");
        assertEquals(proteoform, proteoform2);
    }

    @Test
    void alterProteoformSimple_getsProteoformWithOneModification_altersType() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696");
        Proteoform alteredProteoform = Sensitivity.alterProteoformSimple(proteoform);
        assertNotEquals(proteoform, alteredProteoform);
        assertEquals("00000", alteredProteoform.getPtms().get(0).getKey());
    }

    @Test
    void alterProteoformSimple_getsProteoformWithOneModification_altersCoordinate() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696");
        Proteoform alteredProteoform = Sensitivity.alterProteoformSimple(proteoform);
        assertNotEquals(proteoform, alteredProteoform);
        assertEquals(701L, alteredProteoform.getPtms().get(0).getValue());
    }

    @Test
    void alterProteoformSimple_getsUnmodifiedProteoform_doesNotGetAltered() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O14974;");
        Proteoform alteredProteoform = Sensitivity.alterProteoformSimple(proteoform);
        assertEquals(proteoform, alteredProteoform);
    }

    @Test
    void alterProteoformSimple_getsProteoformWithMultiplePTMs_onePTMBecomes00000() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O15151;00046:342,00046:367,00046:403,01148:null");
        Proteoform alteredProteoform = Sensitivity.alterProteoformSimple(proteoform);
        boolean appeared = false;
        for (Pair<String, Long> ptm : alteredProteoform.getPtms()) {
            if (ptm.getKey().equals("00000")) {
                appeared = true;
                break;
            }
        }
        assertTrue(appeared);
    }

    @Test
    void getProteinsWithMultipleProteoforms_givenMapping_neverReturnAProteoformWith00000() throws FileNotFoundException {
        HashSet<Proteoform> proteoforms = Sensitivity.getModifiedProteoformsOfProteinsWithMultipleProteoforms(mapping);
        for (Proteoform proteoform : proteoforms) {
            for (Pair<String, Long> ptm : proteoform.getPtms()) {
                assertNotEquals("00000", ptm.getKey());
            }
        }
    }

    @Test
    void getPotentialProteoforms_givenAll() throws FileNotFoundException, ParseException {
        Mapping mapping = new Mapping(InputType.PROTEOFORM, false, "");
        HashSet<Proteoform> result = Sensitivity.getPotentialProteoforms(
                ProteoformFormat.SIMPLE.getProteoform("O00221;00046:157,00046:161"),
                mapping,
                Sensitivity.PotentialProteoformsType.ALL,
                true);
        assertEquals(2, result.size());
        assertTrue(result.contains(ProteoformFormat.SIMPLE.getProteoform("O00221;00046:157,00046:161")));
        assertTrue(result.contains(ProteoformFormat.SIMPLE.getProteoform("O00221;00046:157,00046:161,01148:null")));
    }

    @Test
    void getPotentialProteoforms_givenOriginal() throws FileNotFoundException, ParseException {
        Mapping mapping = new Mapping(InputType.PROTEOFORM, false, "");
        HashSet<Proteoform> result = Sensitivity.getPotentialProteoforms(
                ProteoformFormat.SIMPLE.getProteoform("O14974;00046:473"),
                mapping,
                Sensitivity.PotentialProteoformsType.ORIGINAL,
                true);
        assertEquals(1, result.size());
        assertTrue(result.contains(ProteoformFormat.SIMPLE.getProteoform("O14974;00046:473")));
    }

    @Test
    void getPotentialProteoform_givenOthers() throws FileNotFoundException, ParseException {
        Mapping mapping = new Mapping(InputType.PROTEOFORM, false, "");
        HashSet<Proteoform> result = Sensitivity.getPotentialProteoforms(
                ProteoformFormat.SIMPLE.getProteoform("O14974;00046:473"),
                mapping,
                Sensitivity.PotentialProteoformsType.OTHERS,
                true);
        assertEquals(2, result.size());
        assertTrue(result.contains(ProteoformFormat.SIMPLE.getProteoform("O14974;00046:852,00047:696")));
        assertTrue(result.contains(ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696")));
    }

    @Test
    void getPotentialProteoform_givenOthersAndProteoformWithoutModifications_returnsEmtptySet() throws FileNotFoundException, ParseException {
        Mapping mapping = new Mapping(InputType.PROTEOFORM, false, "");
        HashSet<Proteoform> result = Sensitivity.getPotentialProteoforms(
                ProteoformFormat.SIMPLE.getProteoform("O15042;"),
                mapping,
                Sensitivity.PotentialProteoformsType.OTHERS,
                true);
        assertEquals(0, result.size());
    }

    @Test
    void main_givenNoArgumentsPrintsHelp() {
        String[] args = {};
        Sensitivity.main(args);
        assertTrue(errContent.toString().startsWith("Missing required options"), "Wrong output .");
    }

    @Test
    void calculateOneRunPercentages_withNonModifiedProteoformsAndFalseModifiedProteoforms_removesUnmodifiedProteoform() throws ParseException {
        HashSet<Proteoform> proteoforms = new HashSet<>();
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("A6NMZ7;00037:null"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("A6NMZ7;"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("A6NNF4;"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("A8MTZ0;"));
        Sensitivity.calculateOneRunPercentages(proteoforms, 100.0, mapping, Sensitivity.PotentialProteoformsType.ALL,
                true, 5L, false);
        assertEquals(4, proteoforms.size());
    }

    @Test
    void writeEvaluationSeparated_givenTwoLists_Works(TestInfo testInfo) throws IOException {
        List<Pair<MatchType, Double>> percentagesOriginal = new ArrayList<>(7);
        List<Pair<MatchType, Double>> percentagesOthers = new ArrayList<>(7);
        String testName = testInfo.getTestMethod().get().getName();
        String outputFile = testName + ".csv";

        percentagesOriginal.add(new MutablePair<MatchType, Double>(MatchType.SUBSET, 55.0));
        percentagesOthers.add(new MutablePair<MatchType, Double>(MatchType.STRICT, 33.0));

        Sensitivity.writeEvaluationSeparated(percentagesOriginal, percentagesOthers, testName + "/", outputFile);
        List<String> lines = Files.readLines(new File(testName + "/" + outputFile), Charset.defaultCharset());
        assertEquals(3, lines.size());
    }

    @Test
    void writeEvaluationSeparated_givenEmptyList_printsHeaders(TestInfo testInfo) throws IOException {
        String testName = testInfo.getTestMethod().get().getName();
        String outputFile = testName + ".csv";

        Sensitivity.writeEvaluationSeparated(new ArrayList<>(), new ArrayList<>(), testName + "/", outputFile);
        List<String> lines = Files.readLines(new File(testName + "/" + outputFile), Charset.defaultCharset());
        assertEquals("MatchType,Percentage,Category", lines.get(0));
    }

    @Test
    void writeEvaluationSeparated_given_percentagesOriginal_writesProperFormat(TestInfo testInfo) throws IOException {
        List<Pair<MatchType, Double>> percentagesOriginal = new ArrayList<>();
        String testName = testInfo.getTestMethod().get().getName();
        String outputFile = testName + ".csv";

        percentagesOriginal.add(new MutablePair<MatchType, Double>(MatchType.ONE, 10.53));
        Sensitivity.writeEvaluationSeparated(percentagesOriginal, new ArrayList<>(), testName + "/", outputFile);
        List<String> lines = Files.readLines(new File(testName + "/" + outputFile), Charset.defaultCharset());
        assertEquals("ONE,10.53,ORIGINAL", lines.get(1));
    }

    @Test
    void writeEvaluation_givenList_WritesAllPercentagesInList(TestInfo testInfo) throws IOException {
        List<Pair<MatchType, Double>> percentages = new ArrayList<>();
        String testName = testInfo.getTestMethod().get().getName();
        String outputFile = testName + ".csv";
        percentages.add(new MutablePair<>(MatchType.SUPERSET, 11.0));
        Sensitivity.writeEvaluation(percentages, testName + "/", outputFile);
        List<String> lines = Files.readLines(new File(testName + "/" + outputFile), Charset.defaultCharset());
        assertEquals(2, lines.size());
        assertEquals("SUPERSET,11.00", lines.get(1));
    }

    // TODO: test getPotentialProteoforms

    // TODO: test header for each file
    @Test
    void writeEvaluation_givenAnyList_WritesHeaders(TestInfo testInfo) throws IOException {
        String testName = testInfo.getTestMethod().get().getName();
        String outputFile = testName + ".csv";
        Sensitivity.writeEvaluation(new ArrayList<>(), testName + "/", outputFile);
        List<String> lines = Files.readLines(new File(testName + "/" + outputFile), Charset.defaultCharset());
        assertEquals("MatchType,Percentage", lines.get(0));
    }

    @Test
    void writeEvaluation_givenSomePercentages_WritesPercentagesInCorrectFormat(TestInfo testInfo) throws IOException {
        List<Pair<MatchType, Double>> percentages = new ArrayList<>(2);
        String testName = testInfo.getTestMethod().get().getName();
        String outputFile = testName + ".csv";

        percentages.add(new MutablePair<>(MatchType.ONE_NO_TYPES, 11.12));
        percentages.add(new MutablePair<>(MatchType.SUPERSET_NO_TYPES, 24.45));

        Sensitivity.writeEvaluation(percentages, testName + "/", outputFile);
        List<String> lines = Files.readLines(new File(testName + "/" + outputFile), Charset.defaultCharset());
        assertEquals("ONE_NO_TYPES,11.12", lines.get(1));
        assertEquals("SUPERSET_NO_TYPES,24.45", lines.get(2));
    }

    @Test
    void alterProteoform_givenUnmodifiedProteoform_staysTheSame() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;");
        proteoform = Sensitivity.alterProteoform(proteoform);
        assertEquals("P01308", proteoform.getUniProtAcc());
    }

    @Test
    void alterProteoform_givenProteoformWithOneModification_altersTypeAndCoordinateofFirstModification() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01275;00091:127");
        proteoform = Sensitivity.alterProteoform(proteoform);
        assertNotEquals("00091", proteoform.getPtms().get(0).getValue());
        assertNotEquals("127", proteoform.getPtms().get(0).getValue());
    }

    @Test
    void alterProteoform_givenProteoformWithMoreThanOneModification_altersFirstAndSecondModification() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:95");
        proteoform = Sensitivity.alterProteoform(proteoform);

        assertNotEquals("00091", proteoform.getPtms().get(0).getKey());
        assertNotEquals(127L, proteoform.getPtms().get(0).getValue());

        assertEquals("00798", proteoform.getPtms().get(1).getKey());
        assertNotEquals(95L, proteoform.getPtms().get(1).getValue());
    }

    @Test
    void alterProteoform_givenProteoformWithModificationAtNullCoordinate_setsCoordinateToNumber() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01275;00091:127");
        proteoform = Sensitivity.alterProteoform(proteoform);
        assertNotEquals("00091", proteoform.getPtms().get(0).getValue());
        assertNotEquals(null, proteoform.getPtms().get(0).getValue());
    }

    /*
     * All insulin proteoforms are:
     *
     * P01308;
     * P01308;00087:53,00798:31,00798:43
     * P01308;00798:31,00798:43
     * P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109
     * P01308;00798:95,00798:96,00798:100,00798:109
     *
     * Proteoform1: P01308;00087:53,00798:95
     * ALTERED Proteoform1:  P01308;00000:53,00798:95
     * */

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsSTRICTPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform1);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.STRICT));
    }

    /**
     * ONE
     * P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109
     * P01308;00798:95,00798:96,00798:100,00798:109
     */

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsONEPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform1);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(50.0, percentages.get(MatchType.ONE));
    }

    /**
     * ONE_NO_TYPES
     * P01308;00087:53,00798:31,00798:43
     * P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109
     * P01308;00798:95,00798:96,00798:100,00798:109
     */

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsONE_NO_TYPESPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform1);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(75.0, percentages.get(MatchType.ONE_NO_TYPES));
    }

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsSUBSETSPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform1);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUBSET));
    }

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsSUBSETS_NO_TYPESPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform1);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUBSET_NO_TYPES));
    }

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsSUPERSETPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform1);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUPERSET));
    }

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsSUPERSET_NO_TYPESPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform1);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUPERSET_NO_TYPES));
    }

    /*
     * All insulin proteoforms are:
     *
     * P01308;
     * P01308;00087:53,00798:31,00798:43
     * P01308;00798:31,00798:43
     * P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109
     * P01308;00798:95,00798:96,00798:100,00798:109
     *
     * Proteoform2: P01308;00798:31,00798:43
     * ALTERED Proteoform2:  P01308;00000:36,00798:48
     * */

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsSTRICTPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.STRICT));
    }

    /**
     * ONE
     * P01308;00087:53,00798:31,00798:43
     * P01308;00798:31,00798:43
     * P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109
     */

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsONEPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(75.0, percentages.get(MatchType.ONE));
    }

    /**
     * ONE_NO_TYPES
     * P01308;00087:53,00798:31,00798:43
     * P01308;00798:31,00798:43
     * P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109
     */

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsONE_NO_TYPESPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(75.0, percentages.get(MatchType.ONE_NO_TYPES));
    }

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsSUBSETSPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUBSET));
    }

    /**
     * SUBSET_NO_TYPES
     * P01308;00087:53,00798:31,00798:43
     * P01308;00798:31,00798:43
     * P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109
     */

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsSUBSETS_NO_TYPESPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(75.0, percentages.get(MatchType.SUBSET_NO_TYPES));
    }

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsSUPERSETPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUPERSET));
    }

    /**
     * SUPERSET_NO_TYPES
     * P01308;00798:31,00798:43
     */

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsSUPERSET_NO_TYPESPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivity.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivity.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(50.0, percentages.get(MatchType.SUPERSET_NO_TYPES));
    }

    @Test
    void getPotentialProteoforms_givenAndOnlyModifiedFalseAndAllCategories_getsOnlyModifiedProteoforms() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertEquals(5, potentialProteoforms.size());
        assertTrue(potentialProteoforms.contains(proteoform));
    }

    @Test
    void getPotentialProteoforms_givenAndOnlyModifiedTrueAndAllCategories_getsAllProteoforms() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, true);
        assertEquals(4, potentialProteoforms.size());
        assertFalse(potentialProteoforms.contains(proteoform));
    }

    @Test
    void getPotentialProteoforms_givenUnmodifiedProteoformAndOnlyModifiedFalseAndOriginalCategory_getsTheProteoform() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ORIGINAL, false);
        assertEquals(1, potentialProteoforms.size());
        assertTrue(potentialProteoforms.contains(proteoform));
    }

    @Test
    void getPotentialProteoforms_givenModifiedProteoformAndOnlyModifiedFalseAndOriginalCategory_getsTheProteoform() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00798:31,00798:43");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ORIGINAL, false);
        assertEquals(1, potentialProteoforms.size());
        assertTrue(potentialProteoforms.contains(proteoform));
    }

    @Test
    void getPotentialProteoforms_givenUnmodifiedProteoformAndOnlyModifiedTrueCategoryOriginal_getsNoProteoforms() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ORIGINAL, true);
        assertEquals(0, potentialProteoforms.size());
    }

    @Test
    void getPotentialProteoforms_givenModifiedProteoformAndOnlyModifiedTrueAndOriginalCategory_getsTheProteoform() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00798:31,00798:43");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ORIGINAL, true);
        assertEquals(1, potentialProteoforms.size());
        assertTrue(potentialProteoforms.contains(proteoform));
    }

    @Test
    void getPotentialProteoforms_givenUnmodifiedProteoformAndOnlyModifiedFalseAndCategoryOthers_getsProteoforms() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.OTHERS, false);
        assertEquals(4, potentialProteoforms.size());
        assertFalse(potentialProteoforms.contains(proteoform));
    }

    @Test
    void getPotentialProteoforms_givenModifiedProteoformAndOnlyModifiedFalseAndCategoryOthers_getsProteoforms() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:31,00798:43");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.OTHERS, false);
        assertEquals(4, potentialProteoforms.size());
        assertFalse(potentialProteoforms.contains(proteoform));
    }

    @Test
    void getPotentialProteoforms_givenUnmodifiedProteoformAndOnlyModifiedTrueAndCategoryOthers_getsProteoforms() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.OTHERS, true);
        assertEquals(4, potentialProteoforms.size());
        assertFalse(potentialProteoforms.contains(proteoform));
    }

    @Test
    void getPotentialProteoforms_givenModifiedProteoformAndOnlyModifiedTrueAndCategoryOthers_getsProteoforms() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:31,00798:43");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.OTHERS, true);
        assertEquals(3, potentialProteoforms.size());
        assertFalse(potentialProteoforms.contains(ProteoformFormat.SIMPLE.getProteoform("P01308")));
        assertFalse(potentialProteoforms.contains(proteoform));
    }

    @Test
    void matchesAtLeastOne_matchTypeStrictAndProteoform_matches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75581;00046:1490");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertTrue(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.STRICT), 5L), "Should match at least one proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeStrictAndProteoform_notMatches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75581;00046:1500");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.STRICT), 5L), "Should not match any proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeStrictAndUnknownAccession_noMatches() throws ParseException {
        Proteoform proteoformWithUnknownAccession = ProteoformFormat.SIMPLE.getProteoform("P00000");
        Proteoform realProteform = ProteoformFormat.SIMPLE.getProteoform("P31749;00046:473");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(realProteform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoformWithUnknownAccession, potentialProteoforms, ProteoformMatching.getInstance(MatchType.STRICT), 5L));
    }

    @Test
    void matchesAtLeastOne_matchTypeSubsetAndProteoform_matches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75385;00046:317,00046:467");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertTrue(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.SUBSET), 5L), "Should match at least one proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeOneAndProteoform_notMatches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75443;00046:724");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.ONE), 5L), "Should not match any proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeOneAndProteoform_matches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75385;00036:347,00046:467");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertTrue(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.ONE), 5L), "Should match at least one proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeSubsetAndProteoform_notMatches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75443;00046:724");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.SUBSET), 5L), "Should not match any proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeSubsetNoTypesAndProteoform_matches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75385;00000:317,00000:467");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertTrue(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.SUBSET_NO_TYPES), 5L), "Should match at least one proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeSubsetNoTypesAndProteoform_notMatches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75443;00046:724");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.SUBSET_NO_TYPES), 5L), "Should not match any proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeSupersetAndProteoform_matches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75385;00046:317,00046:467,00046:556,00046:638,00047:180,00047:575");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertTrue(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.SUPERSET), 5L), "Should match at least one proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeSupersetAndProteoform_notMatches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75891;00047:244");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.SUPERSET), 5L), "Should not match any proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeSupersetNoTypesAndProteoform_matches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75385;00000:317,00000:467,00046:556,00046:638,00047:180,00047:575");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertTrue(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.SUPERSET_NO_TYPES), 5L), "Should match at least one proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeSupersetNoTypesAndProteoform_notMatches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O75891;00000:244");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.SUPERSET_NO_TYPES), 5L), "Should not match any proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeOneNoTypesAndProteoform_matches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00000:33,00000:40");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertTrue(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.ONE_NO_TYPES), 5L), "Should match at least one proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeOneNoTypesAndUnmodifiedProteoformWithoutPTMs_matches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertTrue(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.ONE_NO_TYPES), 5L), "Should match at least one proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeOneNoTypesAndModifiedProteoform_matches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00000:53");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertTrue(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.ONE_NO_TYPES), 5L), "Should match a proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeOneAndProteoformWithWrongTypes_doesNotMatches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00000:33,00000:40");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.ONE), 5L), "Should not match any proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeOneAndModifiedProteoform_doesNotMatchUnmodifiedReferenceProteoform() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00000:10");       // A PTM that is not in any modified proteoform
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.ONE), 5L), "Should not match any proteoform.");
    }

    @Test
    void matchesAtLeastOne_matchTypeOneNoTypesAndModifiedProteoform_doesNotMatchUnmodifiedReferenceProteoform() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00000:10");       // A PTM that is not in any modified proteoform
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.ONE_NO_TYPES), 5L), "Should not match any proteoform.");
    }

    // For the case of a protein with only one proteoform, and it is modified. THen the ONE matching type can not match
    // to the unmodified proteoform as default when altering the modifications.
    @Test
    void matchesAtLeastOne_matchTypeOneAndProteoformWithoutUnmodifiedProteoforms_noMatches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P31358;00105:46");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.ONE), 5L), "Should not match any proteoform.");
    }

    // For the case of a protein with only one proteoform, and it is modified. THen the ONE_NO_TYPES matching type can not match
    // to the unmodified proteoform as default when altering the modification type
    @Test
    void matchesAtLeastOne_matchTypeOneNoTypeAndProteoformWithoutUnmodifiedProteoforms_noMatches() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P31358;00000:42");
        HashSet<Proteoform> potentialProteoforms = Sensitivity.getPotentialProteoforms(proteoform, mapping, Sensitivity.PotentialProteoformsType.ALL, false);
        assertFalse(Sensitivity.matchesAtLeastOne(proteoform, potentialProteoforms, ProteoformMatching.getInstance(MatchType.ONE_NO_TYPES), 5L), "Should not match any proteoform.");
    }

    @Test
    void calculatePercentagesMatchesAtLeastOne_givenSomeProteoformsAndMatchTypeStrict_100Match() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O75385;00046:758"));
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P02545;00046:338,00046:494,00046:621,00047:491,00048:341"));
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P03372-4;00078:87,00115:274"));
        HashMap<MatchType, Double> percentages = Sensitivity.calculatePercentagesMatchesAtLeastOne(inputProteoforms, mapping, 5L, Sensitivity.PotentialProteoformsType.ALL, false, false);
        assertEquals(100.0, percentages.get(MatchType.STRICT), "All input proteoforms should match to at least one proteoform in the database.");
    }

    @Test
    void calculatePercentagesMatchAtLeastOne_matchTypeStrict_50() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("A2RUS4"));
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O75385;00146:758"));
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P02545;00046:338,00046:494,00046:621,00047:491,00048:341"));
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P03372-4;00078:87,00115:274"));
        HashMap<MatchType, Double> percentages = Sensitivity.calculatePercentagesMatchesAtLeastOne(inputProteoforms, mapping, 5L, Sensitivity.PotentialProteoformsType.ALL, false, false);
        assertEquals(50.0, percentages.get(MatchType.STRICT), "All input proteoforms should match to at least one proteoform in the database.");
    }

    @Test
    void calculatePercentagesMatchAtLeastOne_matchTypeStrict_0() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("A2RUS4"));
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O75385;00146:758"));
        HashMap<MatchType, Double> percentages = Sensitivity.calculatePercentagesMatchesAtLeastOne(inputProteoforms, mapping, 5L, Sensitivity.PotentialProteoformsType.ALL, false, false);
        assertEquals(0.0, percentages.get(MatchType.STRICT), "All input proteoforms should match to at least one proteoform in the database.");
    }

    @Test
    void createTableMatchesAtLeastOne_givenAtLeastOneProteoform_createsTheFile(TestInfo testInfo) throws ParseException, IOException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P00000;"));
        String testName = testInfo.getTestMethod().get().getName();
        String fileName = testName + ".tsv";
        Sensitivity.createTableMatchesAtLeastOne(inputProteoforms, mapping, 5L, testName + "/", fileName);
        assertTrue(new File(testName + "/" + fileName).exists(), "The table file should exist.");
    }

    @Test
    void createTableMatchesAtLeastOne_givenNoProteoforms_doesNotCreateTheFile(TestInfo testInfo) throws ParseException, IOException {
        String testName = testInfo.getTestMethod().get().getName();
        String fileName = testName + ".tsv";
        Sensitivity.createTableMatchesAtLeastOne(new HashSet<>(), mapping, 5L, testName + "/", fileName);
        assertFalse(new File(testName + "/" + fileName).exists(), "The table file should not exist.");
    }

    @Test
    void createTableMatchesAtLeastOne_givenOneProteoform_getsCorrectHeaders(TestInfo testInfo) throws ParseException, IOException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01308;"));
        String testName = testInfo.getTestMethod().get().getName();
        String fileName = testName + ".tsv";
        Sensitivity.createTableMatchesAtLeastOne(inputProteoforms, mapping, 5L, testName + "/", fileName);
        List<String> content = Files.readLines(new File(testName + "/" + fileName), Charset.forName("ISO-8859-1"));
        assertEquals(2, content.size(), "There should be a header line and a values line.");
        assertEquals("PROTEOFORM\tSTRICT\tSUPERSET\tSUPERSET_NO_TYPES\tSUBSET\tSUBSET_NO_TYPES\tONE\tONE_NO_TYPES\tACCESSION", content.get(0));
    }

    @Test
    void createTableMatchesAtLeastOne_givenOneUnmodifiedProteoform_getsAllTrue(TestInfo testInfo) throws ParseException, IOException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01308;"));
        String testName = testInfo.getTestMethod().get().getName();
        String fileName = testName + ".tsv";
        Sensitivity.createTableMatchesAtLeastOne(inputProteoforms, mapping, 5L, testName + "/", fileName);
        List<String> content = Files.readLines(new File(testName + "/" + fileName), Charset.forName("ISO-8859-1"));
        assertEquals(2, content.size(), "There should be a header line and a values line.");
        assertEquals("P01308;\t1\t1\t1\t1\t1\t1\t1\t1", content.get(1));
    }


    @Test
    void createTableMatchesAtLeastOne_givenNonExistentProtein_getsAllFalse(TestInfo testInfo) throws ParseException, IOException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P00000;"));
        String testName = testInfo.getTestMethod().get().getName();
        String fileName = testName + ".tsv";
        Sensitivity.createTableMatchesAtLeastOne(inputProteoforms, mapping, 5L, testName + "/", fileName);
        List<String> content = Files.readLines(new File(testName + "/" + fileName), Charset.forName("ISO-8859-1"));
        assertEquals(2, content.size(), "There should be a header line and a values line.");
        assertEquals("P00000;\t0\t0\t0\t0\t0\t0\t0\t0", content.get(1));
    }

    @Test
    void createTableMatchesAtLeastOne_givenProtoeformWithOneExistingModification_getsTrueAllExceptStrict(TestInfo testInfo) throws ParseException, IOException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01308;00798:31"));
        String testName = testInfo.getTestMethod().get().getName();
        String fileName = testName + ".tsv";
        Sensitivity.createTableMatchesAtLeastOne(inputProteoforms, mapping, 5L, testName + "/", fileName);
        List<String> content = Files.readLines(new File(testName + "/" + fileName), Charset.forName("ISO-8859-1"));
        assertEquals(2, content.size(), "There should be a header line and a values line.");
        assertEquals("P01308;00798:31\t0\t1\t1\t1\t1\t1\t1\t1", content.get(1));
    }

    // Given a proteoform with no unmodified proteoform, and altering the PTM type, only the NO_TYPE matchings are true
    @Test
    void createTableMatchesAtLeastOne_givenProteoformWithIncorrectModificationTypes_getsTrueForNoTypeMatchings(TestInfo testInfo) throws ParseException, IOException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P07203;00000:49"));
        String testName = testInfo.getTestMethod().get().getName();
        String fileName = testName + ".tsv";
        Sensitivity.createTableMatchesAtLeastOne(inputProteoforms, mapping, 5L, testName + "/", fileName);
        List<String> content = Files.readLines(new File(testName + "/" + fileName), Charset.forName("ISO-8859-1"));
        assertEquals(2, content.size(), "There should be a header line and a values line.");
        assertEquals("P07203;00000:49\t0\t0\t1\t0\t1\t0\t1\t1", content.get(1));
    }

    // Given a proteoform with non existent unmodified proteoform and an extra PTM. SUBSET is false, but ONE and SUPERSET are true
    @Test
    void createTableMatchesAtLeastOne_givenProteoformWithIncorrectModificationTypes_getsTrueOneAndSuperset(TestInfo testInfo) throws ParseException, IOException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01308;00798:31,00787:4"));
        String testName = testInfo.getTestMethod().get().getName();
        String fileName = testName + ".tsv";
        Sensitivity.createTableMatchesAtLeastOne(inputProteoforms, mapping, 5L, testName + "/", fileName);
        List<String> content = Files.readLines(new File(testName + "/" + fileName), Charset.forName("ISO-8859-1"));
        assertEquals(2, content.size(), "There should be a header line and a values line.");
        assertEquals("P01308;00787:4,00798:31\t0\t1\t1\t0\t0\t1\t1\t1", content.get(1));
    }

    // Given a proteoform with non existent unmodified proteoform and an extra PTM and altered PTM types. SUBSET is false, but ONE_NO_TYPES and SUPERSET_NO_TYPES are true
    @Test
    void createTableMatchesAtLeastOne_givenProteoformWithIncorrectModificationTypes_getsTrueOneNoTypesAndSupersetNoTypes(TestInfo testInfo) throws ParseException, IOException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01308;00700:31,00700:47"));
        String testName = testInfo.getTestMethod().get().getName();
        String fileName = testName + ".tsv";
        Sensitivity.createTableMatchesAtLeastOne(inputProteoforms, mapping, 5L, testName + "/", fileName);
        List<String> content = Files.readLines(new File(testName + "/" + fileName), Charset.forName("ISO-8859-1"));
        assertEquals(2, content.size(), "There should be a header line and a values line.");
        assertEquals("P01308;00700:31,00700:47\t0\t1\t1\t0\t1\t0\t1\t1", content.get(1));
    }

    @Test
    void calculatePercentagesMatchesAtLeastOne_givenExistentProteoformAndAlterTrueAndOriginal_strictGetsFalse() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01308;00000:31,00798:43"));
        HashMap<MatchType, Double> percentages = Sensitivity.calculatePercentagesMatchesAtLeastOne(
                inputProteoforms, mapping, 5L, Sensitivity.PotentialProteoformsType.ORIGINAL, false, true);
        assertEquals(0.0, percentages.get(MatchType.STRICT));
    }

    @Test
    void calculatePercentagesMatchesAtLeastOne_givenExistentProteoformAndAlterTrueAndOthers_strictGetsFalse() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01308;00000:31,00798:43"));
        HashMap<MatchType, Double> percentages = Sensitivity.calculatePercentagesMatchesAtLeastOne(
                inputProteoforms, mapping, 5L, Sensitivity.PotentialProteoformsType.OTHERS, false, true);
        assertEquals(0.0, percentages.get(MatchType.STRICT));
    }

    @Test
    void calculatePercentagesMatchesAtLeastOne_givenExistentProteoformAndAlterTrueAndOriginal_OneGetsFalse() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01275;00091:127"));
        HashMap<MatchType, Double> percentages = Sensitivity.calculatePercentagesMatchesAtLeastOne(
                inputProteoforms, mapping, 5L, Sensitivity.PotentialProteoformsType.ORIGINAL, false, true);
        assertEquals(0.0, percentages.get(MatchType.ONE));
    }

    @Test
    void calculatePercentagesMatchesAtLeastOne_givenExistentProteoformAndAlterTrueAndOriginal_OneGetsTrue() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01308;00798:31,00798:43"));
        HashMap<MatchType, Double> percentages = Sensitivity.calculatePercentagesMatchesAtLeastOne(
                inputProteoforms, mapping, 5L, Sensitivity.PotentialProteoformsType.ORIGINAL, false, false);
        assertEquals(100.0, percentages.get(MatchType.ONE));
    }

    @Test
    void calculatePercentagesMatchesAtLeastOne_givenExistentProteoformAndAlterTrueAndOthers_OneGetsFalse() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:32,00798:60"));
        HashMap<MatchType, Double> percentages = Sensitivity.calculatePercentagesMatchesAtLeastOne(
                inputProteoforms, mapping, 5L, Sensitivity.PotentialProteoformsType.OTHERS, false, true);
        assertEquals(0.0, percentages.get(MatchType.ONE));
    }

    @Test
    void calculatePercentagesMatchesAtLeastOne_givenExistentProteoformAndAlterTrueAndOthers_OneNoTypesGetsTrue() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01308;00000:31,00798:43"));
        HashMap<MatchType, Double> percentages = Sensitivity.calculatePercentagesMatchesAtLeastOne(
                inputProteoforms, mapping, 5L, Sensitivity.PotentialProteoformsType.OTHERS, false, true);
        assertEquals(100.0, percentages.get(MatchType.ONE_NO_TYPES));
    }


    @Test
    void calculatePercentagesMatchesAtLeastOneMultipleTimes_givenExistingProteoformAndAlterTrue_strictOriginalFails() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(ProteoformFormat.SIMPLE.getProteoform("P01308;00798:31,00798:43"));
        List<Pair<MatchType, Double>> percentages = Sensitivity.calculatePercentagesMatchesAtLeastOneMultipleTimes(
                inputProteoforms,
                100.0,
                mapping,
                Sensitivity.PotentialProteoformsType.ORIGINAL, 5L, false, true, 3);
        assertEquals(24, percentages.size());
        assertEquals(0.0, percentages.get(1).getValue());
        assertEquals(0.0, percentages.get(9).getValue());
    }
}