package matcher.tools;

import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.xerces.impl.xpath.regex.Match;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeAll;

import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SensitivyTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;
    static Mapping mapping;
    static Proteoform proteoform2;

    @BeforeAll
    static void setUpAll() {
        try {
            mapping = new Mapping(InputType.PROTEOFORM, false, "");
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
        assertEquals("00046", Sensitivy.getModType("S"));
    }

    @Test
    void getModType_givenSiteAndResidueT_returnPSIMOD00047() {
        assertEquals("00047", Sensitivy.getModType("T"));
    }

    @Test
    void getModType_givenResidueY_returnPSIMOD00048() {
        assertEquals("00048", Sensitivy.getModType("Y"));
    }

    @Test
    void getModType_givenNotResidueSTY_throwException() {
        assertEquals("00000", Sensitivy.getModType("HOLA"));
    }

    @Test
    void getPTM_givenLineWithResidueS_returnPTM() {
        String line = "Q68D10,323,S";
        try {
            assertEquals("00046:323", Sensitivy.getPTM(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw exception");
        }
    }

    @Test
    void getPTM_givenLineWithResidueT_returnPTM() {
        String line = "Q9H1B7,633,T";
        try {
            assertEquals("00047:633", Sensitivy.getPTM(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw exception");
        }
    }

    @Test
    void getPTM_givenLineWithResidueY_returnPTM() {
        String line = "Q9H1C7,64,Y";
        try {
            assertEquals("00048:64", Sensitivy.getPTM(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw exception");
        }
    }

    @Test
    void getPTM_givenInvalidSiteWithLetters_throwsNumberFormatException() {
        String line = "Q9H1C7,asv,Y";
        Assertions.assertThrows(NumberFormatException.class, () -> {
            Sensitivy.getPTM(line);
        });
    }

    @Test
    void getPTM_givenInvalidSiteWithNegative_throwsParseException() {
        String line = "Q9H1C7,-2,Y";
        Assertions.assertThrows(ParseException.class, () -> {
            Sensitivy.getPTM(line);
        });
    }

    @Test
    void createProteoform_givenValidLineWithS_returnProteoform() {
        String line = "Q68D51,305,S";
        try {
            assertEquals("Q68D51;00046:305", Sensitivy.getSimpleProteoformStringFromPhosphoModification(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw an exception.");
        }
    }

    @Test
    void createProteoform_givenValidLineWithT_returnProteoform() {
        String line = "Q9H1A4,701,T";
        try {
            assertEquals("Q9H1A4;00047:701", Sensitivy.getSimpleProteoformStringFromPhosphoModification(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw an exception.");
        }
    }

    @Test
    void createProteoform_givenValidLineWithY_returnProteoform() {
        String line = "P46783,12,Y";
        try {
            assertEquals("P46783;00048:12", Sensitivy.getSimpleProteoformStringFromPhosphoModification(line));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should not throw an exception.");
        }
    }

    @Test
    void createProteoformList_givenPhosphositesList_addsCorrectNumberOfProteoforms() {
        assertEquals(9, Sensitivy.createProteoformList("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt").size());
    }

    @Test
    void createProteoformList_givenPhosphositesList_eachHasOneModification() {
        HashSet<Proteoform> proteoformList = Sensitivy.createProteoformList("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt");
        for (Proteoform proteoform : proteoformList) {
            assertEquals(1, proteoform.getPtms().size());
        }
    }

    @Test
    void createProteoformList_givenPhosphositesWithSerine_hasProteoform() {
        try {
            HashSet<Proteoform> proteoformSet = new HashSet<>(Sensitivy.createProteoformList("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt"));
            assertTrue(proteoformSet.contains(ProteoformFormat.SIMPLE.getProteoform("Q9UK32;00046:389")));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformList_givenPhosphositesWithThreonine_hasProteoform() {
        try {
            HashSet<Proteoform> proteoformSet = new HashSet<>(Sensitivy.createProteoformList("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt"));
            assertTrue(proteoformSet.contains(ProteoformFormat.SIMPLE.getProteoform("Q9UK32;00047:368")));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformList_givenPhosphositesWithTyrosine_hasProteoform() {
        try {
            HashSet<Proteoform> proteoformSet = new HashSet<>(Sensitivy.createProteoformList("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt"));
            assertTrue(proteoformSet.contains(ProteoformFormat.SIMPLE.getProteoform("Q9ULH0;00048:1096")));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformListAggregated_givenPhosphositesList_addsCorrectNumberOfProteoforms() {
        try {
            assertEquals(4, Sensitivy.createProteoformListAggregated("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt").size());
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformListAggregated_givenProteoformWithMultipleModifications_works() {
        try {
            List<Proteoform> proteoformList = Sensitivy.createProteoformListAggregated("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt");
            assertEquals(4, proteoformList.get(0).getPtms().size());
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformListAggregated_lastPhosphositeInList_AddedToLastProteoform() {
        try {
            HashSet<Proteoform> proteoformSet = new HashSet<>(Sensitivy.createProteoformListAggregated("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites.txt"));
            assertTrue(proteoformSet.contains(ProteoformFormat.SIMPLE.getProteoform("Q9ULH0;00048:1096")));
        } catch (ParseException e) {
            e.printStackTrace();
            fail("Should parse all the proteoforms correctly.");
        }
    }

    @Test
    void createProteoformListAggregated_lastProteoformWithMultipleMods_AddedToLastProteoform() {
        try {
            HashSet<Proteoform> proteoformSet = new HashSet<>(Sensitivy.createProteoformListAggregated("src\\test\\resources\\Proteoforms\\Phosphosites\\phosphosites_lastProteoformWithTwoMods.txt"));
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
        proteoforms = (HashSet<Proteoform>) proteoforms.stream().map(proteoform -> Sensitivy.alterProteoformSimple(proteoform)).collect(Collectors.toSet());
        assertFalse(proteoforms.contains(ProteoformFormat.SIMPLE.getProteoform("A2RUS2;00046:472,00046:490")));
        assertFalse(proteoforms.contains(ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696")));
    }

    @Test
    void alterProteoforms_getsUnmodifiedProteoform_doesNotGetAltered() throws ParseException {
        HashSet<Proteoform> proteoforms = new HashSet<>();
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;"));
        proteoforms = (HashSet<Proteoform>) proteoforms.stream().map(proteoform -> Sensitivy.alterProteoformSimple(proteoform)).collect(Collectors.toSet());
        assertTrue(proteoforms.contains(ProteoformFormat.SIMPLE.getProteoform("O14974")));
    }

    @Test
    void alterProteoforms_getsProteoformWithOneModification_altersType() throws ParseException {
        HashSet<Proteoform> proteoforms = new HashSet<>(1);
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696"));
        proteoforms = (HashSet<Proteoform>) proteoforms.stream().map(proteoform -> Sensitivy.alterProteoformSimple(proteoform)).collect(Collectors.toSet());
        assertFalse(proteoforms.contains("O14974;00047:696"));
        for (Proteoform proteoform : proteoforms) {
            assertEquals("00000", proteoform.getPtms().get(0).getKey());
        }
    }

    @Test
    void alterProteoforms_getsProteoformWithOneModification_altersCoordinate() throws ParseException {
        HashSet<Proteoform> proteoforms = new HashSet<>(1);
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696"));
        proteoforms = (HashSet<Proteoform>) proteoforms.stream().map(proteoform -> Sensitivy.alterProteoformSimple(proteoform)).collect(Collectors.toSet());
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

        HashSet<Proteoform> sample = Sensitivy.getProteoformSample(proteoforms, 33.0);
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

        HashSet<Proteoform> sample = Sensitivy.getProteoformSample(proteoforms, 34.0);
        assertEquals(2, sample.size());
    }

    @Test
    void alterProteoformSimple_givenProteoform_originalStaysIntact() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O14966;00113:202,00113:203");
        Proteoform alteredProteoform = Sensitivy.alterProteoformSimple(proteoform);
        Proteoform proteoform2 = ProteoformFormat.SIMPLE.getProteoform("O14966;00113:202,00113:203");
        assertEquals(proteoform, proteoform2);
    }

    @Test
    void alterProteoformSimple_getsProteoformWithOneModification_altersType() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696");
        Proteoform alteredProteoform = Sensitivy.alterProteoformSimple(proteoform);
        assertNotEquals(proteoform, alteredProteoform);
        assertEquals("00000", alteredProteoform.getPtms().get(0).getKey());
    }

    @Test
    void alterProteoformSimple_getsProteoformWithOneModification_altersCoordinate() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696");
        Proteoform alteredProteoform = Sensitivy.alterProteoformSimple(proteoform);
        assertNotEquals(proteoform, alteredProteoform);
        assertEquals(701L, alteredProteoform.getPtms().get(0).getValue());
    }

    @Test
    void alterProteoformSimple_getsUnmodifiedProteoform_doesNotGetAltered() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O14974;");
        Proteoform alteredProteoform = Sensitivy.alterProteoformSimple(proteoform);
        assertEquals(proteoform, alteredProteoform);
    }

    @Test
    void alterProteoformSimple_getsProteoformWithMultiplePTMs_onePTMBecomes00000() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("O15151;00046:342,00046:367,00046:403,01148:null");
        Proteoform alteredProteoform = Sensitivy.alterProteoformSimple(proteoform);
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
        HashSet<Proteoform> proteoforms = Sensitivy.getModifiedProteoformsOfProteinsWithMultipleProteoforms(mapping);
        for (Proteoform proteoform : proteoforms) {
            for (Pair<String, Long> ptm : proteoform.getPtms()) {
                assertNotEquals("00000", ptm.getKey());
            }
        }
    }

    @Test
    void getPotentialProteoforms_givenAll() throws FileNotFoundException, ParseException {
        Mapping mapping = new Mapping(InputType.PROTEOFORM, false, "");
        HashSet<Proteoform> result = Sensitivy.getPotentialProteoforms(
                ProteoformFormat.SIMPLE.getProteoform("O00221;00046:157,00046:161"),
                mapping,
                Sensitivy.PotentialProteoformsType.ALL,
                true);
        assertEquals(2, result.size());
        assertTrue(result.contains(ProteoformFormat.SIMPLE.getProteoform("O00221;00046:157,00046:161")));
        assertTrue(result.contains(ProteoformFormat.SIMPLE.getProteoform("O00221;00046:157,00046:161,01148:null")));
    }

    @Test
    void getPotentialProteoforms_givenOriginal() throws FileNotFoundException, ParseException {
        Mapping mapping = new Mapping(InputType.PROTEOFORM, false, "");
        HashSet<Proteoform> result = Sensitivy.getPotentialProteoforms(
                ProteoformFormat.SIMPLE.getProteoform("O14974;00046:473"),
                mapping,
                Sensitivy.PotentialProteoformsType.ORIGINAL,
                true);
        assertEquals(1, result.size());
        assertTrue(result.contains(ProteoformFormat.SIMPLE.getProteoform("O14974;00046:473")));
    }

    @Test
    void getPotentialProteoform_givenOthers() throws FileNotFoundException, ParseException {
        Mapping mapping = new Mapping(InputType.PROTEOFORM, false, "");
        HashSet<Proteoform> result = Sensitivy.getPotentialProteoforms(
                ProteoformFormat.SIMPLE.getProteoform("O14974;00046:473"),
                mapping,
                Sensitivy.PotentialProteoformsType.OTHERS,
                true);
        assertEquals(2, result.size());
        assertTrue(result.contains(ProteoformFormat.SIMPLE.getProteoform("O14974;00046:852,00047:696")));
        assertTrue(result.contains(ProteoformFormat.SIMPLE.getProteoform("O14974;00047:696")));
    }

    @Test
    void getPotentialProteoform_givenOthersAndProteoformWithoutModifications_returnsEmtptySet() throws FileNotFoundException, ParseException {
        Mapping mapping = new Mapping(InputType.PROTEOFORM, false, "");
        HashSet<Proteoform> result = Sensitivy.getPotentialProteoforms(
                ProteoformFormat.SIMPLE.getProteoform("O15042;"),
                mapping,
                Sensitivy.PotentialProteoformsType.OTHERS,
                true);
        assertEquals(0, result.size());
    }

    @Test
    void main_givenNoArgumentsPrintsHelp() {
        String[] args = {};
        Sensitivy.main(args);
        assertTrue(errContent.toString().startsWith("Missing required options"), "Wrong output .");
    }

    @Test
    void calculateOneRunPercentages_withNonModifiedProteoformsAndFalseModifiedProteoforms_removesUnmodifiedProteoform() throws ParseException {
        HashSet<Proteoform> proteoforms = new HashSet<>();
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("A6NMZ7;00037:null"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("A6NMZ7;"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("A6NNF4;"));
        proteoforms.add(ProteoformFormat.SIMPLE.getProteoform("A8MTZ0;"));
        Sensitivy.calculateOneRunPercentages(proteoforms, 100.0, mapping, Sensitivy.PotentialProteoformsType.ALL,
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

        Sensitivy.writeEvaluationSeparated(percentagesOriginal, percentagesOthers, testName + "/", outputFile);
        List<String> lines = Files.readLines(new File(testName + "/" + outputFile), Charset.defaultCharset());
        assertEquals(3, lines.size());
    }

    @Test
    void writeEvaluationSeparated_givenEmptyList_printsHeaders(TestInfo testInfo) throws IOException {
        String testName = testInfo.getTestMethod().get().getName();
        String outputFile = testName + ".csv";

        Sensitivy.writeEvaluationSeparated(new ArrayList<>(), new ArrayList<>(), testName + "/", outputFile);
        List<String> lines = Files.readLines(new File(testName + "/" + outputFile), Charset.defaultCharset());
        assertEquals("MatchType,Percentage,Category", lines.get(0));
    }

    @Test
    void writeEvaluationSeparated_given_percentagesOriginal_writesProperFormat(TestInfo testInfo) throws IOException {
        List<Pair<MatchType, Double>> percentagesOriginal = new ArrayList<>();
        String testName = testInfo.getTestMethod().get().getName();
        String outputFile = testName + ".csv";

        percentagesOriginal.add(new MutablePair<MatchType, Double>(MatchType.ONE, 10.53));
        Sensitivy.writeEvaluationSeparated(percentagesOriginal, new ArrayList<>(), testName + "/", outputFile);
        List<String> lines = Files.readLines(new File(testName + "/" + outputFile), Charset.defaultCharset());
        assertEquals("ONE,10.53,ORIGINAL", lines.get(1));
    }

    @Test
    void writeEvaluation_givenList_WritesAllPercentagesInList(TestInfo testInfo) throws IOException {
        List<Pair<MatchType, Double>> percentages = new ArrayList<>();
        String testName = testInfo.getTestMethod().get().getName();
        String outputFile = testName + ".csv";
        percentages.add(new MutablePair<>(MatchType.SUPERSET, 11.0));
        Sensitivy.writeEvaluation(percentages, testName + "/", outputFile);
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
        Sensitivy.writeEvaluation(new ArrayList<>(), testName + "/", outputFile);
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

        Sensitivy.writeEvaluation(percentages, testName + "/", outputFile);
        List<String> lines = Files.readLines(new File(testName + "/" + outputFile), Charset.defaultCharset());
        assertEquals("ONE_NO_TYPES,11.12", lines.get(1));
        assertEquals("SUPERSET_NO_TYPES,24.45", lines.get(2));
    }

    @Test
    void alterProteoform_givenUnmodifiedProteoform_staysTheSame() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;");
        proteoform = Sensitivy.alterProteoform(proteoform);
        assertEquals("P01308", proteoform.getUniProtAcc());
    }

    @Test
    void alterProteoform_givenProteoformWithOneModification_altersTypeAndCoordinateofFirstModification() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01275;00091:127");
        proteoform = Sensitivy.alterProteoform(proteoform);
        assertNotEquals("00091", proteoform.getPtms().get(0).getValue());
        assertNotEquals("127", proteoform.getPtms().get(0).getValue());
    }

    @Test
    void alterProteoform_givenProteoformWithMoreThanOneModification_altersFirstAndSecondModification() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:95");
        proteoform = Sensitivy.alterProteoform(proteoform);

        assertNotEquals("00091", proteoform.getPtms().get(0).getKey());
        assertNotEquals(127L, proteoform.getPtms().get(0).getValue());

        assertEquals("00798", proteoform.getPtms().get(1).getKey());
        assertNotEquals(95L, proteoform.getPtms().get(1).getValue());
    }

    @Test
    void alterProteoform_givenProteoformWithModificationAtNullCoordinate_setsCoordinateToNumber() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01275;00091:127");
        proteoform = Sensitivy.alterProteoform(proteoform);
        assertNotEquals("00091", proteoform.getPtms().get(0).getValue());
        assertNotEquals(null, proteoform.getPtms().get(0).getValue());
    }

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsSTRICTPercentagesRight() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:95");
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.STRICT));
    }

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsONEPercentagesRight() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:95");
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(50.0, percentages.get(MatchType.ONE));
    }

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsONE_NO_TYPESPercentagesRight() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:95");
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(75.0, percentages.get(MatchType.ONE_NO_TYPES));
    }

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsSUBSETSPercentagesRight() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:95");
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUBSET));
    }

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsSUBSETS_NO_TYPESPercentagesRight() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:95");
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUBSET_NO_TYPES));
    }

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsSUPERSETPercentagesRight() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:95");
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUPERSET));
    }

    @Test
    void calculateOneRunPercentages_givenMixedProteoform_getsSUPERSET_NO_TYPESPercentagesRight() throws ParseException {
        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:95");
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUPERSET_NO_TYPES));
    }


    // Proteoform2: P01308;00798:31,00798:43
    @Test
    void calculateOneRunPercentages_givenProteoform2_getsSTRICTPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.STRICT));
    }

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsONEPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(75.0, percentages.get(MatchType.ONE));
    }

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsONE_NO_TYPESPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(75.0, percentages.get(MatchType.ONE_NO_TYPES));
    }

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsSUBSETSPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUBSET));
    }

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsSUBSETS_NO_TYPESPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(75.0, percentages.get(MatchType.SUBSET_NO_TYPES));
    }

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsSUPERSETPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(0.0, percentages.get(MatchType.SUPERSET));
    }

    @Test
    void calculateOneRunPercentages_givenProteoform2_getsSUPERSET_NO_TYPESPercentagesRight() throws ParseException {
        HashSet<Proteoform> inputProteoforms = new HashSet<>();
        inputProteoforms.add(proteoform2);
        HashMap<MatchType, Double> percentages = Sensitivy.calculateOneRunPercentages(inputProteoforms, 100.0, mapping,
                Sensitivy.PotentialProteoformsType.ALL, true, 5L, true);
        assertEquals(50.0, percentages.get(MatchType.SUPERSET_NO_TYPES));
    }
}