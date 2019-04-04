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

class SearchGeneTest {

    private static Mapping mapping;

    @BeforeAll
    static void loadStaticMapping() throws FileNotFoundException {
        mapping = new Mapping(InputType.GENE, true, "");
    }

    @Test
    void searchWithGeneFillHitsTest1() {
        List<String> input = new ArrayList<>();
        input.add("GCK");
        input.add("GCK ");
        input.add("HNF4A");
        input.add("blabla");

        SearchResult result = Search.searchWithGene(input, mapping, true);

        assertEquals(2, result.getHitProteins().size());
        assertTrue(result.getHitProteins().contains("P35557"));
        assertTrue(result.getHitProteins().contains("P41235"));
        assertEquals(9, result.getHitPathways().size());
        assertTrue(result.containsPathwayByStid("R-HSA-170822"));
        assertTrue(result.containsPathwayByStid("R-HSA-74160"));
    }

    @Test
    void searchWithGeneFillHitsTest2() throws ParseException {
        List<String> input = new ArrayList<>();
        input.add("CFTR");
        input.add("TGFB1 ");
        input.add("FCGR2A");
        input.add("DCTN4");
        input.add("SCNN1B");
        input.add("SCNN1G");
        input.add("SCNN1A");
        input.add("TNFRSF1A");
        input.add("CLCA4");
        input.add("STX1A");
        input.add("CXCL8");

        SearchResult result = Search.searchWithGene(input, mapping, true);

        assertEquals(10, result.getHitProteins().size());
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
        assertTrue(result.getHitPathwayByStid("R-HSA-1266738").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("Q16623")));
    }
}