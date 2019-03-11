package methods.search;

import model.InputType;
import model.Mapping;
import model.ProteoformFormat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchUniprotTest {

    private static Mapping mapping;

    @BeforeAll
    static void loadStaticMapping() {
        mapping = new Mapping(InputType.UNIPROT, true);
    }

    @Test
    void searchWithUniProtFillHitsTest1() {

        List<String> input = new ArrayList<>();
        input.add("P01308");
        input.add("P0130898989");

        SearchResult result = Search.search(input, InputType.UNIPROT, true, mapping);

        assertEquals(1, result.getHitProteins().size());
        assertTrue(result.getHitProteins().contains("P01308"));
        assertEquals(21, result.getHitPathways().size());
        assertTrue(result.containsPathwayByStid("R-HSA-264876"));
        assertTrue(result.containsPathwayByStid("R-HSA-74749"));
    }

    @Test
    void searchWithUniProtFillHitsTest2() {
        List<String> input = new ArrayList<>();
        input.add("P31749");
        input.add("blabla");
        input.add("Q5S007 ");
        input.add("P10636");

        SearchResult result = Search.search(input, InputType.UNIPROT, true, mapping);

        assertEquals(3, result.getHitProteins().size());
        assertTrue(result.getHitProteins().contains("P10636"));
        assertEquals(92, result.getHitPathways().size());
        assertTrue(result.containsPathwayByStid("R-HSA-8857538"));
        assertTrue(result.containsPathwayByStid("R-HSA-5663202"));
        assertTrue(result.containsPathwayByStid("R-HSA-264870"));
    }

    @Test
    void searchUniProtCountReactionsAndEntitiesFoundTest() throws ParseException {
        List<String> input = new ArrayList<>();
        input.add("P01308");

        SearchResult result = Search.search(input, InputType.UNIPROT, true, mapping);

        assertEquals(1, result.getHitPathwayByStid("R-HSA-74749").getEntitiesFound().size());
        assertEquals(1, result.getHitPathwayByStid("R-HSA-74749").getReactionsFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-74749").getReactionsFound().contains("R-HSA-110011"));

        assertEquals(5, result.getHitPathwayByStid("R-HSA-6807878").getReactionsFound().size());
        assertEquals(1, result.getHitPathwayByStid("R-HSA-6807878").getEntitiesFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-6807878").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01308")));
        assertTrue(result.getHitPathwayByStid("R-HSA-6807878").getReactionsFound().contains("R-HSA-6807875"));
        assertTrue(result.getHitPathwayByStid("R-HSA-6807878").getReactionsFound().contains("R-HSA-6809003"));
    }
}