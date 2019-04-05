package extractor.neo4j;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Multimap;
import model.*;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Map;

import static extractor.neo4j.Extractor.getSNPAndSwissProtFromVep;
import static matcher.tools.FileHandler.getBufferedReaderForGzipFile;
import static org.junit.jupiter.api.Assertions.*;

class ExtractorTest {

    static final String mapping_path = "../MappingFiles/Extractor/";

    @AfterEach
    void deleteOutput(TestInfo testInfo) {
        try {
            File directory = new File(testInfo.getTestMethod().get().getName() + "/");
            FileUtils.deleteDirectory(directory);
            assertFalse(directory.exists());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getEnsembleToProteinsTest(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<String, String> imapEnsembleToProteins = Extractor.getEnsemblToProteins(testInfo.getTestMethod().get().getName() + "/");

        assertEquals(1, imapEnsembleToProteins.get("ENSP00000380389").size());
        assertTrue(imapEnsembleToProteins.get("ENSP00000380389").contains("P19438"));

        assertEquals(1, imapEnsembleToProteins.get("ENSG00000143226").size());
        assertTrue(imapEnsembleToProteins.get("ENSG00000143226").contains("P12318"));
    }

    @Test
    void getProteinNamesTest(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableMap<String, String> proteinsToNames = Extractor.getProteinNames(testInfo.getTestMethod().get().getName() + "/");

        assertEquals("Hemoglobin subunit beta", proteinsToNames.get("P68871"), "Protein P68871 missing in the mapping");
        assertEquals("Hemoglobin subunit alpha", proteinsToNames.get("P69905"), "Protein P69905 missing in the mapping");
        assertEquals("Insulin", proteinsToNames.get("P01308"), "Protein P01308 missing in the mapping");
        assertEquals(true, proteinsToNames.containsKey("Q9Y6P5"), "Protein Q9Y6P5 missing in the mapping");
    }

    @Test
    void getReactionsNeighboursTest1(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableMap<String, Reaction> iReactions = Extractor.getReactions(testInfo.getTestMethod().get().getName() + "/");

        assertEquals(76, iReactions.get("R-HSA-1112666").getProteinParticipantsWithRole().keySet().size());
        assertTrue(iReactions.get("R-HSA-1112666").getProteinParticipantsWithRole().containsKey("A0A075B6P5"));
        assertTrue(iReactions.get("R-HSA-1112666").getProteinParticipantsWithRole().containsKey("P01593"));
        assertTrue(iReactions.get("R-HSA-1112666").getProteinParticipantsWithRole().containsKey("P01611"));
        assertTrue(iReactions.get("R-HSA-1112666").getProteinParticipantsWithRole().containsKey("A0M8Q6"));
    }

    @Test
    void getReactionNeightboursTest2(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableMap<String, Reaction> iReactions = Extractor.getReactions(testInfo.getTestMethod().get().getName() + "/");

        assertEquals(2, iReactions.get("R-HSA-74716").getProteinParticipantsWithRole().keySet().size());
        assertTrue(iReactions.get("R-HSA-74716").getProteinParticipantsWithRole().containsKey("P06213"));
        assertTrue(iReactions.get("R-HSA-74716").getProteinParticipantsWithRole().containsKey("P01308"));

        assertEquals(2, iReactions.get("R-HSA-109862").getProteinParticipantsWithRole().keySet().size());
        assertEquals(6, iReactions.get("R-HSA-109862").getProteinParticipantsWithRole().entries().size());
        assertTrue(iReactions.get("R-HSA-109862").getProteinParticipantsWithRole().containsKey("P28482"));
        assertTrue(iReactions.get("R-HSA-109862").getProteinParticipantsWithRole().containsKey("P36507"));

        assertEquals(2, iReactions.get("R-HSA-74716").getProteinParticipantsWithRole().keySet().size());
        assertTrue(iReactions.get("R-HSA-74716").getProteinParticipantsWithRole().containsKey("P01308"));
        assertTrue(iReactions.get("R-HSA-74716").getProteinParticipantsWithRole().containsKey("P06213"));

        assertEquals(2, iReactions.get("R-HSA-74726").getProteinParticipantsWithRole().keySet().size());
        assertTrue(iReactions.get("R-HSA-74726").getProteinParticipantsWithRole().containsKey("P01308"));
        assertTrue(iReactions.get("R-HSA-74726").getProteinParticipantsWithRole().containsKey("P06213"));
    }

    @Test
    void getReactionParticipantsPhenylalaningHydroxylaseTest(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableMap<String, Reaction> iReactions = Extractor.getReactions(testInfo.getTestMethod().get().getName() + "/");

        assertEquals(1, iReactions.get("R-HSA-71118").getProteinParticipantsWithRole().size());
        assertTrue(iReactions.get("R-HSA-71118").getProteinParticipantsWithRole().containsEntry("P00439", Role.CATALYSTACTIVITY));

        assertEquals(1, iReactions.get("R-HSA-71118").getProteoformParticipants().size());
        assertTrue(iReactions.get("R-HSA-71118").getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("P00439"), Role.CATALYSTACTIVITY));
        assertTrue(iReactions.get("R-HSA-71118").getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("P00439;"), Role.CATALYSTACTIVITY));
    }

    @Test
    void getReactionParticipantsProteinPhosphataseRegulatorySubunitTest(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableMap<String, Reaction> iReactions = Extractor.getReactions(testInfo.getTestMethod().get().getName() + "/");

        assertEquals(8, iReactions.get("R-HSA-419083").getProteinParticipantsWithRole().keySet().size());
        assertTrue(iReactions.get("R-HSA-419083").getProteinParticipantsWithRole().containsEntry("O14974", Role.INPUT));
        assertTrue(iReactions.get("R-HSA-419083").getProteinParticipantsWithRole().containsEntry("O14974", Role.OUTPUT));
        assertTrue(iReactions.get("R-HSA-419083").getProteinParticipantsWithRole().containsEntry("O75116", Role.CATALYSTACTIVITY));
        assertTrue(iReactions.get("R-HSA-419083").getProteinParticipantsWithRole().containsEntry("P62140", Role.OUTPUT));

        assertEquals(9, iReactions.get("R-HSA-419083").getProteoformParticipants().keySet().size());
        assertEquals(11, iReactions.get("R-HSA-419083").getProteoformParticipants().size());
        assertTrue(iReactions.get("R-HSA-419083").getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("O14974"), Role.INPUT));
        assertTrue(iReactions.get("R-HSA-419083").getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("O14974;00046:852,00047:696"), Role.OUTPUT));
        assertTrue(iReactions.get("R-HSA-419083").getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("O75116"), Role.CATALYSTACTIVITY));
        assertTrue(iReactions.get("R-HSA-419083").getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("P08134"), Role.CATALYSTACTIVITY));
    }

    @Test
    void getReactionParticipantsTest3(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableMap<String, Reaction> iReactions = Extractor.getReactions(testInfo.getTestMethod().get().getName() + "/");

        String reaction = "R-HSA-6802927";
        assertEquals(35, iReactions.get(reaction).getProteinParticipantsWithRole().keySet().size());
        assertTrue(iReactions.get(reaction).getProteinParticipantsWithRole().containsEntry("O00203", Role.INPUT));
        assertTrue(iReactions.get(reaction).getProteinParticipantsWithRole().containsEntry("O15164", Role.OUTPUT));
        assertTrue(iReactions.get(reaction).getProteinParticipantsWithRole().containsEntry("O60674", Role.CATALYSTACTIVITY));
        assertFalse(iReactions.get(reaction).getProteinParticipantsWithRole().containsEntry("Q96PU8", Role.CATALYSTACTIVITY));

        assertEquals(59, iReactions.get(reaction).getProteoformParticipants().keySet().size());
        assertEquals(60, iReactions.get(reaction).getProteoformParticipants().size());
        assertTrue(iReactions.get(reaction).getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("O00203;00046:445,00046:602,00046:729,00047:599"), Role.OUTPUT));
        assertFalse(iReactions.get(reaction).getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("O00203;00046:445,00046:602,00046:729,00047:599"), Role.CATALYSTACTIVITY));
        assertTrue(iReactions.get(reaction).getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("O00203;00046:445,00046:729"), Role.INPUT));
        assertFalse(iReactions.get(reaction).getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("O00203;00046:445,00046:729"), Role.OUTPUT));
        assertTrue(iReactions.get(reaction).getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("O60674"), Role.CATALYSTACTIVITY));
        assertTrue(iReactions.get(reaction).getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("P10398;00046:299,00046:576,00047:452,00047:455,00048:302"), Role.CATALYSTACTIVITY));
        assertTrue(iReactions.get(reaction).getProteoformParticipants().containsEntry(ProteoformFormat.SIMPLE.getProteoform("O95352;00046:445,00046:729"), Role.INPUT));
    }

    @Test
    void getReactionParticipantRolesTest(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableMap<String, Reaction> iReactions = Extractor.getReactions(testInfo.getTestMethod().get().getName() + "/");

        assertEquals(14, iReactions.get("R-HSA-8863895").getProteinParticipantsWithRole().keySet().size());
        assertTrue(iReactions.get("R-HSA-8863895").getProteinParticipantsWithRole().get("O14920").contains(Role.CATALYSTACTIVITY));
        assertTrue(iReactions.get("R-HSA-8863895").getProteinParticipantsWithRole().get("O00161").contains(Role.INPUT));
        assertTrue(iReactions.get("R-HSA-8863895").getProteinParticipantsWithRole().get("O00161").contains(Role.OUTPUT));
        assertTrue(iReactions.get("R-HSA-8863895").getProteinParticipantsWithRole().get("Q99836").contains(Role.REGULATEDBY));

    }

    @Test
    void imapProteinsToComplexesTest(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableSetMultimap<String, String> imapProteinsToComplexes = Extractor.getProteinsToComplexes(testInfo.getTestMethod().get().getName() + "/");

        assertEquals(32, imapProteinsToComplexes.get("Q9Y297").size());
        assertTrue(imapProteinsToComplexes.get("Q9Y297").contains("R-HSA-174138"));
        assertTrue(imapProteinsToComplexes.get("Q9Y297").contains("R-HSA-8952593"));

        assertEquals(1, imapProteinsToComplexes.get("P00558").size());
        assertTrue(imapProteinsToComplexes.get("P00558").contains("R-HSA-70484"));
    }

    @Test
    void imapComplexToProteoformsTest1(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<String, Proteoform> imapComplexesToProteoforms = Extractor.getComplexesToProteoforms(testInfo.getTestMethod().get().getName() + "/");

        String complex = "R-HSA-174138";

        assertEquals(7, imapComplexesToProteoforms.get(complex).size());
        assertTrue(imapComplexesToProteoforms.get(complex).contains(ProteoformFormat.SIMPLE.getProteoform("Q9UKT4;00046:145,00046:149")));
        assertTrue(imapComplexesToProteoforms.get(complex).contains(ProteoformFormat.SIMPLE.getProteoform("Q9UKT4;00046:182")));
        assertTrue(imapComplexesToProteoforms.get(complex).contains(ProteoformFormat.SIMPLE.getProteoform("P63208")));
    }

    @Test
    void imapComplexToProteoformsTest2(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<String, Proteoform> imapComplexesToProteoforms = Extractor.getComplexesToProteoforms(testInfo.getTestMethod().get().getName() + "/");

        String complex = "R-HSA-8952593";

        assertEquals(66, imapComplexesToProteoforms.get(complex).size());
        assertTrue(imapComplexesToProteoforms.get(complex).contains(ProteoformFormat.SIMPLE.getProteoform("Q13616;01150:720")));
        assertTrue(imapComplexesToProteoforms.get(complex).contains(ProteoformFormat.SIMPLE.getProteoform("Q9Y3I1")));
        assertTrue(imapComplexesToProteoforms.get(complex).contains(ProteoformFormat.SIMPLE.getProteoform("Q15843;00134:76")));
    }

    @Test
    void imapProteoformsToComplexesTest1(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<Proteoform, String> imapProteoformsToComplexes = Extractor.getProteoformsToComplexes(testInfo.getTestMethod().get().getName() + "/");

        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("Q9UKT4;00046:182");

        assertEquals(4, imapProteoformsToComplexes.get(proteoform).size());
        assertTrue(imapProteoformsToComplexes.get(proteoform).contains("R-HSA-177328"));
        assertTrue(imapProteoformsToComplexes.get(proteoform).contains("R-HSA-186975"));
        assertTrue(imapProteoformsToComplexes.get(proteoform).contains("R-HSA-174138"));
        assertTrue(imapProteoformsToComplexes.get(proteoform).contains("R-HSA-174061"));

        proteoform = ProteoformFormat.SIMPLE.getProteoform("Q9Y6N7;00048:1073");

        assertEquals(2, imapProteoformsToComplexes.get(proteoform).size());
        assertTrue(imapProteoformsToComplexes.get(proteoform).contains("R-HSA-376027"));
        assertTrue(imapProteoformsToComplexes.get(proteoform).contains("R-HSA-428499"));
    }

    @Test
    void imapProteoformsToComplexesTest2(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<Proteoform, String> imapProteoformsToComplexes = Extractor.getProteoformsToComplexes(testInfo.getTestMethod().get().getName() + "/");

        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("Q01362;00048:219,00048:225,00048:229");

        assertEquals(1, imapProteoformsToComplexes.get(proteoform).size());
        assertTrue(imapProteoformsToComplexes.get(proteoform).contains("R-HSA-2454211"));

        proteoform = ProteoformFormat.SIMPLE.getProteoform("Q01362");

        assertEquals(2, imapProteoformsToComplexes.get(proteoform).size());
        assertTrue(imapProteoformsToComplexes.get(proteoform).contains("R-HSA-2454198"));
        assertTrue(imapProteoformsToComplexes.get(proteoform).contains("R-HSA-2454224"));
    }

    @Test
    void imapComplexesToProteinsTest(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableSetMultimap<String, String> imapComplexesToParticipants = Extractor.getComplexesToProteins(testInfo.getTestMethod().get().getName() + "/");

        assertEquals(1, imapComplexesToParticipants.get("R-HSA-70484").size());
        assertTrue(imapComplexesToParticipants.get("R-HSA-70484").contains("P00558"));

        assertEquals(6, imapComplexesToParticipants.get("R-HSA-174138").size());
        assertTrue(imapComplexesToParticipants.get("R-HSA-174138").contains("Q9Y297"));
        assertTrue(imapComplexesToParticipants.get("R-HSA-174138").contains("P63208"));
        assertTrue(imapComplexesToParticipants.get("R-HSA-174138").contains("Q9UKT4"));
    }

    @Test
    void imapSetsToMembersAndCandidatesTest(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableSetMultimap<String, String> imapSetsToMembersAndCandidates = Extractor.getSetMembersAndCandidates(testInfo.getTestMethod().get().getName() + "/");

        assertEquals(3, imapSetsToMembersAndCandidates.get("R-HSA-1008234").size());
        assertTrue(imapSetsToMembersAndCandidates.get("R-HSA-1008234").contains("Q9ULX9"));

        assertEquals(3, imapSetsToMembersAndCandidates.get("R-HSA-114528").size());
        assertTrue(imapSetsToMembersAndCandidates.get("R-HSA-114528").contains("P17252"));
        assertTrue(imapSetsToMembersAndCandidates.get("R-HSA-114528").contains("P05771"));
        assertTrue(imapSetsToMembersAndCandidates.get("R-HSA-114528").contains("P05129"));
    }

    @Test
    void setsToProteinsTest1(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<String, String> imapSetsToProteins = Extractor.getSetsToProteins(testInfo.getTestMethod().get().getName() + "/");

        String set = "R-HSA-8935716";

        assertEquals(3, imapSetsToProteins.get(set).size());
        assertTrue(imapSetsToProteins.get(set).contains("Q01196"));
        assertTrue(imapSetsToProteins.get(set).contains("Q13951"));
        assertTrue(imapSetsToProteins.get(set).contains("Q99873"));
    }

    @Test
    void setsToProteinsTest2(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<String, String> imapSetsToProteins = Extractor.getSetsToProteins(testInfo.getTestMethod().get().getName() + "/");

        String set = "R-HSA-8854239";

        assertEquals(2, imapSetsToProteins.get(set).size());
        assertTrue(imapSetsToProteins.get(set).contains("P51149"));
        assertTrue(imapSetsToProteins.get(set).contains("Q96AH8"));

    }

    @Test
    void proteinsToSetsTest1(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<String, String> imapProteinsToSets = Extractor.getProteinsToSets(testInfo.getTestMethod().get().getName() + "/");

        String protein = "Q01196";

        assertEquals(5, imapProteinsToSets.get(protein).size());
        assertTrue(imapProteinsToSets.get(protein).contains("R-HSA-8935716"));
        assertTrue(imapProteinsToSets.get(protein).contains("R-HSA-8952092"));
        assertTrue(imapProteinsToSets.get(protein).contains("R-HSA-8938850"));
        assertTrue(imapProteinsToSets.get(protein).contains("R-HSA-8956602"));
        assertTrue(imapProteinsToSets.get(protein).contains("R-HSA-8938964"));
    }

    @Test
    void proteinsToSetsTest2(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<String, String> imapProteinsToSets = Extractor.getProteinsToSets(testInfo.getTestMethod().get().getName() + "/");

        String protein = "Q8WXH6";

        assertEquals(2, imapProteinsToSets.get(protein).size());
        assertTrue(imapProteinsToSets.get(protein).contains("R-HSA-8870439"));
        assertTrue(imapProteinsToSets.get(protein).contains("R-HSA-8870442"));
    }

    @Test
    void setsToProtoformsTest1(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<String, Proteoform> imapSetsToProteoform = Extractor.getSetsToProteoforms(testInfo.getTestMethod().get().getName() + "/");

        String set = "R-HSA-8935716";

        assertEquals(4, imapSetsToProteoform.get(set).size());
        assertTrue(imapSetsToProteoform.get(set).contains(ProteoformFormat.SIMPLE.getProteoform("Q01196")));
        assertTrue(imapSetsToProteoform.get(set).contains(ProteoformFormat.SIMPLE.getProteoform("Q13951")));
        assertTrue(imapSetsToProteoform.get(set).contains(ProteoformFormat.SIMPLE.getProteoform("Q99873")));
        assertTrue(imapSetsToProteoform.get(set).contains(ProteoformFormat.SIMPLE.getProteoform("Q01196;00078:206,00078:210")));
    }

    @Test
    void setsToProteoformsTest2(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<String, Proteoform> imapSetsToProteoform = Extractor.getSetsToProteoforms(testInfo.getTestMethod().get().getName() + "/");

        String set = "R-HSA-8870439";

        assertEquals(60, imapSetsToProteoform.get(set).size());
        assertTrue(imapSetsToProteoform.get(set).contains(ProteoformFormat.SIMPLE.getProteoform("P61019;00113:211,00113:212")));
        assertTrue(imapSetsToProteoform.get(set).contains(ProteoformFormat.SIMPLE.getProteoform("Q8WXH6;00113:274")));
        assertTrue(imapSetsToProteoform.get(set).contains(ProteoformFormat.SIMPLE.getProteoform("Q12829;00113:275")));
        assertTrue(imapSetsToProteoform.get(set).contains(ProteoformFormat.SIMPLE.getProteoform("Q9BZG1;00113:257,00113:258")));
    }

    @Test
    void proteoformsToSetsTest1(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<Proteoform, String> imapProteoformsToSets = Extractor.getProteoformsToSets(testInfo.getTestMethod().get().getName() + "/");

        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("Q01196;00078:206,00078:210");

        assertEquals(1, imapProteoformsToSets.get(proteoform).size());
        assertTrue(imapProteoformsToSets.get(proteoform).contains("R-HSA-8935716"));
    }

    @Test
    void proteoformsToSetsTest2(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");
        ImmutableSetMultimap<Proteoform, String> imapProteoformsToSets = Extractor.getProteoformsToSets(testInfo.getTestMethod().get().getName() + "/");

        Proteoform proteoform = ProteoformFormat.SIMPLE.getProteoform("Q8WXH6;00113:274");

        assertEquals(1, imapProteoformsToSets.get(proteoform).size());
        assertTrue(imapProteoformsToSets.get(proteoform).contains("R-HSA-8870439"));
    }

    @Test
    void imapRsidsToProteinsTest(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableSetMultimap<String, String> imapRsidsToProteins = Extractor.getRsIdsToProteins(11, testInfo.getTestMethod().get().getName() + "/");

        assertTrue(imapRsidsToProteins.containsKey("rs10840447"));
        assertTrue(imapRsidsToProteins.containsKey("rs7110099"));
        assertTrue(imapRsidsToProteins.containsKey("rs555583938"));
        assertTrue(imapRsidsToProteins.get("rs10840447").contains("P01308"));
        assertTrue(imapRsidsToProteins.get("rs7110099").contains("P01308"));
        assertTrue(imapRsidsToProteins.get("rs555583938").contains("P01308"));
    }

    @Test
    void imapChrBpToProteinsTest(TestInfo testInfo ) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableSetMultimap<Long, String> imapChrBpToProteins = Extractor.getChrBpToProteins(11, testInfo.getTestMethod().get().getName() + "/");

        assertTrue(imapChrBpToProteins.containsKey(2176042L));
        assertTrue(imapChrBpToProteins.containsKey(2176105L));
        assertTrue(imapChrBpToProteins.containsKey(2176134L));
        assertTrue(imapChrBpToProteins.get(2176042L).contains("P01308"));
        assertTrue(imapChrBpToProteins.get(2176105L).contains("P01308"));
        assertTrue(imapChrBpToProteins.get(2176134L).contains("P01308"));
    }

    @Test
    void imapGeneticVariantsToProteinsTest5(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableSetMultimap<String, String> imapRsidsToProteins = Extractor.getRsIdsToProteins(5, testInfo.getTestMethod().get().getName() + "/");

        assertTrue(imapRsidsToProteins.containsKey("rs17238540"));
        assertTrue(imapRsidsToProteins.get("rs17238540").contains("P04035"));

        ImmutableSetMultimap<Long, String> imapChrBpToProteins = Extractor.getChrBpToProteins(5, testInfo.getTestMethod().get().getName() + "/");

        assertTrue(imapChrBpToProteins.containsKey(74655498L));
        assertTrue(imapChrBpToProteins.get(74655498L).contains("P04035"));
    }

    @Test
    void getSNPAndSwissProtFromVep_whenHasAllColumnValues_returnProtein() {
        Multimap<Snp, String> snpToSwissprotMap = getSNPAndSwissProtFromVep("19 39738787 rs12979860 T ENSG00000197110 Q8IZI9 NA 282617");

        for (Map.Entry<Snp, String> snpToSwissprotPair : snpToSwissprotMap.entries()) {
            assertEquals("rs12979860", snpToSwissprotPair.getKey().getRsid(), "Missing rsid: ");
        }
    }

    @Test
    void imapGeneticVariantsToProteinsTest19(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableSetMultimap<String, String> imapRsidsToProteins = Extractor.getRsIdsToProteins(19, testInfo.getTestMethod().get().getName() + "/");
//        for (Map.Entry<String, String> entry : imapRsidsToProteins.entries()) {
//            System.out.println(entry.getKey());
//            if (entry.getKey().equals("rs12979860"))
//                break;
//        }
        assertTrue(imapRsidsToProteins.containsKey("rs375192398"));
        assertTrue(imapRsidsToProteins.containsKey("rs150383577"));
        assertTrue(imapRsidsToProteins.containsKey("rs149672762"));
        assertTrue(imapRsidsToProteins.get("rs375192398").contains("Q8NGA8"));
        assertTrue(imapRsidsToProteins.get("rs150383577").contains("Q6NT55"));
        assertTrue(imapRsidsToProteins.get("rs149672762").contains("O75023"));

        ImmutableSetMultimap<Long, String> imapChrBpToProteins = Extractor.getChrBpToProteins(19, testInfo.getTestMethod().get().getName() + "/");

        assertTrue(imapChrBpToProteins.containsKey(287906L));
        assertTrue(imapChrBpToProteins.containsKey(416757L));
        assertTrue(imapChrBpToProteins.get(287906L).contains("O43688"));
        assertTrue(imapChrBpToProteins.get(416757L).contains("P98077"));
    }

    @Test
    void imapGeneticVariantsToProteinsTest1(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableSetMultimap<String, String> imapRsidsToProteins = Extractor.getRsIdsToProteins(1, testInfo.getTestMethod().get().getName() + "/");

        assertTrue(imapRsidsToProteins.containsKey("rs2816958"));
        assertTrue(imapRsidsToProteins.get("rs2816958").contains("O00482"));

        ImmutableSetMultimap<Long, String> imapChrBpToProteins = Extractor.getChrBpToProteins(1, testInfo.getTestMethod().get().getName() + "/");

        assertTrue(imapChrBpToProteins.containsKey(200101920L));
        assertTrue(imapChrBpToProteins.get(200101920L).contains("O00482"));
    }

    @Test
    void getBufferedReaderForGzipFile_withPathToExistentFile_ReadsFirstLine_Test(TestInfo testInfo) {
        try{
            File file = new File(mapping_path + "1.gz");
            if(!file.exists()){
                fail("The file " + mapping_path + "1.gz" + " does not exist.");
            }
            BufferedReader br = getBufferedReaderForGzipFile(mapping_path, "1.gz");
            String header = br.readLine(); // Read header line
            assertTrue(header.length() > 0, "The header is empty, then the function could not read the file.");
        } catch (FileNotFoundException e) {
            fail("Should find the compressed file.");
            e.printStackTrace();
        } catch (IOException e) {
            fail("Should be able to read the compressed file.");
            e.printStackTrace();
        }
    }

    @Test
    void getBufferedReaderForGzipFile_withPathToExistentFileWithoutSlash_ReadsFirstLine_Test(){
        try{
            File file = new File(mapping_path + "1.gz");
            if(!file.exists()){
                fail("The file " + mapping_path + "1.gz" + " does not exist.");
            }
            BufferedReader br = getBufferedReaderForGzipFile(mapping_path.substring(0, mapping_path.length()-1), "1.gz");
            String header = br.readLine(); // Read header line
            assertTrue(header.length() > 0, "The header is empty, then the function could not read the file.");
        } catch (FileNotFoundException e) {
            fail("Should find the compressed file.");
            e.printStackTrace();
        } catch (IOException e) {
            fail("Should be able to read the compressed file.");
            e.printStackTrace();
        }
    }

    @Test
    void getBufferedReaderForGzipFile_withUnexistentFile_sendFileNotFoundException_Test() {
        assertThrows(FileNotFoundException.class, () -> {
            getBufferedReaderForGzipFile("non/existent/path/", "nonExistentFile.gz");
        });
    }


    @Test
    void imapGeneticVariantsToProteinsTest22(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableSetMultimap<String, String> imapRsidsToProteins = Extractor.getRsIdsToProteins(22, testInfo.getTestMethod().get().getName() + "/");

        System.out.println(imapRsidsToProteins.size());

        assertTrue(imapRsidsToProteins.containsKey("rs771638142"));
        assertTrue(imapRsidsToProteins.get("rs771638142").contains("Q8NG94"));

        assertTrue(imapRsidsToProteins.containsKey("rs9628391"));
        assertTrue(imapRsidsToProteins.get("rs9628391").contains("Q8NG94"));

        ImmutableSetMultimap<Long, String> imapChrBpToProteins = Extractor.getChrBpToProteins(22, testInfo.getTestMethod().get().getName() + "/");

        assertTrue(imapChrBpToProteins.containsKey(16444392L));
        assertTrue(imapChrBpToProteins.get(16444392L).contains("Q8NG94"));

        assertTrue(imapChrBpToProteins.containsKey(16446491L));
        assertTrue(imapChrBpToProteins.get(16446491L).contains("Q8NG94"));
    }

    @Test
    void imapGeneticVariantsToProteinsTest20(TestInfo testInfo) {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableSetMultimap<String, String> imapRsidsToProteins = Extractor.getRsIdsToProteins(20, testInfo.getTestMethod().get().getName() + "/");

        assertTrue(imapRsidsToProteins.containsKey("rs1883832"));
        assertTrue(imapRsidsToProteins.get("rs1883832").contains("P25942"));

        ImmutableSetMultimap<Long, String> imapChrBpToProteins = Extractor.getChrBpToProteins(20, testInfo.getTestMethod().get().getName() + "/");

        assertTrue(imapChrBpToProteins.containsKey(44746982L));
        assertTrue(imapChrBpToProteins.get(44746982L).contains("P25942"));
    }

    @Test
    void imapProteinsToProteoformsTest(TestInfo testInfo) throws ParseException {
        ConnectionNeo4j.initializeNeo4j("bolt://127.0.0.1:7687", "", "");

        ImmutableSetMultimap<String, Proteoform> imapProteinsToProteoforms = Extractor.getProteinsToProteoforms(testInfo.getTestMethod().get().getName() + "/");

        assertTrue(imapProteinsToProteoforms.containsKey("O43561"));
        assertFalse(imapProteinsToProteoforms.get("O43561").contains(ProteoformFormat.SIMPLE.getProteoform("O43561-2;00048:127")));
        assertTrue(imapProteinsToProteoforms.get("O43561").contains(ProteoformFormat.SIMPLE.getProteoform("O43561-2")));
        assertTrue(imapProteinsToProteoforms.get("O43561").contains(ProteoformFormat.SIMPLE.getProteoform("O43561-2;00048:127,00048:132,00048:171,00048:191,00048:226")));

        assertTrue(imapProteinsToProteoforms.containsKey("P11362"));
        assertEquals(7, imapProteinsToProteoforms.get("P11362").size());
        assertTrue(imapProteinsToProteoforms.get("P11362").contains(ProteoformFormat.SIMPLE.getProteoform("P11362-1;")));
        assertTrue(imapProteinsToProteoforms.get("P11362").contains(ProteoformFormat.SIMPLE.getProteoform("P11362-1;00048:463,00048:583,00048:585,00048:653,00048:654,00048:730,00048:766,00048:776")));
        assertFalse(imapProteinsToProteoforms.get("P11362").contains(ProteoformFormat.SIMPLE.getProteoform("P11362-19;00048:463")));
        assertTrue(imapProteinsToProteoforms.get("P11362").contains(ProteoformFormat.SIMPLE.getProteoform("P11362-19;00048:463,00048:583,00048:585,00048:653,00048:654,00048:730,00048:766,00048:776")));

        assertTrue(imapProteinsToProteoforms.containsKey("P21802"));
        assertEquals(13, imapProteinsToProteoforms.get("P21802").size());
        assertTrue(imapProteinsToProteoforms.get("P21802").contains(ProteoformFormat.SIMPLE.getProteoform("P21802;00048:null")));
        assertFalse(imapProteinsToProteoforms.get("P21802").contains(ProteoformFormat.SIMPLE.getProteoform("P21802-18;00048:465")));
        assertTrue(imapProteinsToProteoforms.get("P21802").contains(ProteoformFormat.SIMPLE.getProteoform("P21802-18;00048:465,00048:585,00048:587,00048:655,00048:656,00048:732,00048:768,00048:778")));
        assertTrue(imapProteinsToProteoforms.get("P21802").contains(ProteoformFormat.SIMPLE.getProteoform("P21802-3;00048:467,00048:587,00048:589,00048:657,00048:658,00048:734,00048:770,00048:780")));
        assertTrue(imapProteinsToProteoforms.get("P21802").contains(ProteoformFormat.SIMPLE.getProteoform("P21802-5;00048:464,00048:584,00048:586,00048:654,00048:655,00048:731,00048:767,00048:778")));
    }
}