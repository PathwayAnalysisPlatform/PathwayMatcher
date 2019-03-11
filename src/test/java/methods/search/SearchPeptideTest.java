package methods.search;

import model.InputType;
import model.Mapping;
import model.MatchType;
import model.ProteoformFormat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchPeptideTest {

    private static String fastaFile = "src/main/resources/uniprot-all.fasta";

    private static Mapping mapping;

    @BeforeAll
    static void loadStaticMapping() {
        mapping = new Mapping(InputType.PEPTIDE, true);
    }

    @Test
    void searchWithPeptideFillHitsTest1() {
        System.out.println(System.getProperty("user.dir"));
        List<String> input = new ArrayList<>();
        input.add("LQVGQVELGGGPGAGSLQPLALEGSLQKRGIVEQCCTSICSLYQLENYCN");

        SearchResult result = Search.searchWithPeptide(input, mapping, true, fastaFile);

        assertEquals(1, result.getHitProteins().size());
        assertTrue(result.getHitProteins().contains("P01308"));
        assertEquals(21, result.getHitPathways().size());
        assertTrue(result.containsPathwayByStid("R-HSA-264876"));
        assertTrue(result.containsPathwayByStid("R-HSA-74749"));
    }

    @Test
    void searchWithPeptidesFillHitsTest2() throws ParseException {
        List<String> input = new ArrayList<>();
        input.add("IYLGIGLCLLFIVRTLLLHPAIFGLHHIGMQMRIAMFSLIYKKTLKLSSRVLDKISIGQL"); //P13569
        input.add("YVRYFNSSAFFFSGFFVVFLSVLPYALIKGIILRKIFTTISFCIVLRMAVTRQFPWAVQT"); //P13569
        input.add("TDLSQKSLQLESKGLTLNSNAWMNDTVIIDSTVGKDTFFLITWNSLPPSISLWDPSGTIM"); //Q14CN2
        input.add("TGRRGDLATIHGMNRPFLLLMATPLERAQHLQSSRHRRALDTNYCFSSTEKNCCVRQLYI"); //P01137
        input.add("SEWLVLQTPHLEFQEGETIMLRCHSWKDKPLVKVTFFQNGKSQKFSHLDPTFSIPQANHS"); //P12318
        input.add("PPKELVLAGKDAAAEYDELAEPQDFQDDPDIIAFRKANKVGIFIKVTPQREEGEVTVCFK"); //Q9UJW0
        input.add("\tAVLERILAPELSHANATRNLNFSIWNHTPLVLIDERNPHHPMVLDLFGDNHNGLTSSSAS"); //P51168
        input.add("MAPGEKIKAKIKKNLPVTGPQAPTIKELMRWYCLNTNTHGCRRIVVSRGRLRRLLWIGFT"); //P51170
        input.add("FFCNNTTIHGAIRLVCSQHNRMKTAFWAVLWLCTFGMMYWQFGLLFGEYFSYPVSLNINL"); //P37088
        input.add("   AVVENVPPLRWKEFVRRLGLSDHEIDRLELQNGRCLREAQYSMLATWRRRTPRREATLEL"); //P19438
        input.add("ILASPNPDEKTKEELEELMSDIKKTANKVRSKLKSIEQSIEQEEGLNRSSADLRIRKTQH"); //Q16623
        input.add("MTSKLAVALLAAFLISAALCEGAVLPRSAKELRCQCIKTYSKPFHPKFIKELRVIESGPH"); //P10145
        input.add("blabla");

        SearchResult result = Search.search(input, InputType.PEPTIDE,  true, mapping, MatchType.STRICT, 0L, fastaFile);

        assertEquals(11, result.getHitProteins().size());
        assertTrue(result.getHitProteins().contains("P37088"));
        assertTrue(result.getHitProteins().contains("P01137"));
        assertEquals(104, result.getHitPathways().size());
        assertTrue(result.containsPathwayByStid("R-HSA-2672351"));
        assertTrue(result.containsPathwayByStid("R-HSA-76002"));
        assertTrue(result.containsPathwayByStid("R-HSA-449147"));

        // Check counts
        assertEquals(4, result.getHitPathwayByStid("R-HSA-2672351").getEntitiesFound().size());
        assertEquals(6, result.getHitPathwayByStid("R-HSA-2672351").getReactionsFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-2672351").getReactionsFound().contains("R-HSA-5333671"));
        assertTrue(result.getHitPathwayByStid("R-HSA-2672351").getReactionsFound().contains("R-HSA-2672334"));
        assertTrue(result.getHitPathwayByStid("R-HSA-2672351").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("Q14CN2")));
        assertTrue(result.getHitPathwayByStid("R-HSA-2672351").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P51170")));

        assertEquals(1, result.getHitPathwayByStid("R-HSA-1266738").getReactionsFound().size());
        assertEquals(1, result.getHitPathwayByStid("R-HSA-1266738").getEntitiesFound().size());
        assertFalse(result.getHitPathwayByStid("R-HSA-1266738").getReactionsFound().contains("R-HSA-381283"));
        assertFalse(result.getHitPathwayByStid("R-HSA-1266738").getReactionsFound().contains("R-HSA-560491"));
        assertTrue(result.getHitPathwayByStid("R-HSA-1266738").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("Q16623")));
        assertFalse(result.getHitPathwayByStid("R-HSA-1266738").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01137")));
    }
}