package methods.search;

import model.InputType;
import model.Mapping;
import model.ProteoformFormat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchEnsemblTest {

    private static Mapping mapping;

    @BeforeAll
    static void loadStaticMapping() throws FileNotFoundException {
        mapping = new Mapping(InputType.ENSEMBL, true, "");
    }

    @Test
    void searchWithEnsemblFillHitsDiabetesInYouthTest() throws ParseException {
        List<String> input = new ArrayList<>();
        input.add("blabla");
        input.add("ENSG00000101076");
        input.add("ENSG00000106633");
        input.add("ENSP00000223366");
        input.add("ENSP00000312987");
        input.add("ENSP00000315180");
        input.add("ENSP00000379142");
        input.add("ENSP00000384247");
        input.add("ENSP00000396216");
        input.add("ENSP00000410911");
        input.add("\t\tENSP00000412111");
        input.add("ENSP00000476609");
        input.add("ENSP00000482149   ");


        SearchResult result = Search.searchWithEnsembl(input, mapping, true);

        assertEquals(2, result.getHitProteins().size());
        assertTrue(result.getHitProteins().contains("P41235"));
        assertTrue(result.getHitProteins().contains("P35557"));
        assertEquals(9, result.getHitPathways().size());
        assertTrue(result.containsPathwayByStid("R-HSA-170822"));
        assertTrue(result.containsPathwayByStid("R-HSA-74160"));

        // Check counts
        assertEquals(5, result.getHitPathwayByStid("R-HSA-170822").getReactionsFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-170822").getReactionsFound().contains("R-HSA-170796"));
        assertTrue(result.getHitPathwayByStid("R-HSA-170822").getReactionsFound().contains("R-HSA-170810"));
        assertTrue(result.getHitPathwayByStid("R-HSA-170822").getReactionsFound().contains("R-HSA-170825"));
        assertEquals(1, result.getHitPathwayByStid("R-HSA-170822").getEntitiesFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-170822").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P35557")));

        assertEquals(1, result.getHitPathwayByStid("R-HSA-383280").getReactionsFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-383280").getReactionsFound().contains("R-HSA-376419"));
        assertEquals(1, result.getHitPathwayByStid("R-HSA-383280").getEntitiesFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-383280").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P41235")));
    }

    @Test
    void searchWithEnsemblFillHitsCysticFibrosisTest() throws ParseException {
        List<String> input = new ArrayList<>();
        input.add("ENSG00000001626");
        input.add("ENSG00000016602");
        input.add("ENSG00000067182");
        input.add("ENSG00000105329");
        input.add("ENSG00000106089");
        input.add("\tENSG00000111319");
        input.add("ENSG00000132912");
        input.add("ENSG00000143226");
        input.add("   ENSG00000166828");
        input.add("ENSG00000168447");
        input.add("ENSG00000169429");
        input.add("blabla");

        SearchResult result = Search.searchWithEnsembl(input, mapping, true);

        assertEquals(9, result.getHitProteins().size());
//        assertTrue(result.getHitProteins().contains("P37088"));
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
        assertTrue(result.getHitPathwayByStid("R-HSA-1266738").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("Q16623")));
    }
}