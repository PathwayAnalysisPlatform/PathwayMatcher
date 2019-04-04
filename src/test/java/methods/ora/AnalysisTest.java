package methods.ora;

import methods.search.Search;
import methods.search.SearchResult;
import model.InputType;
import model.Mapping;
import model.ProteoformFormat;
import org.junit.jupiter.api.BeforeAll;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalysisTest {

    private static Mapping mapping;

    @BeforeAll
    static void loadStaticMapping() throws FileNotFoundException {
        System.out.println("The working directory is: " + System.getProperty("user.dir"));
        mapping = new Mapping(InputType.UNIPROT, true, "");
    }

    @org.junit.jupiter.api.Test
    void analysisTest() throws ParseException {
        List<String> input = new ArrayList<>();

        input.add("P01308");
        // Execute the search to fill the iPathways, hitPahtways and hitProteins data structures
        SearchResult searchResult = Search.searchWithUniProt(input, mapping, true);
        AnalysisResult analysisResult = Analysis.analysis(searchResult, mapping.getProteinsToReactions().keySet().size());

        assertEquals(21, searchResult.getHitPathways().size());
        assertTrue(searchResult.containsPathwayByStid("R-HSA-392499"));
        assertEquals(11, analysisResult.getHitPathwayByStid("R-HSA-392499").getReactionsFound().size());
        assertEquals(1, analysisResult.getHitPathwayByStid("R-HSA-392499").getEntitiesFound().size());
        assertTrue(analysisResult.getHitPathwayByStid("R-HSA-392499").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01308")));
        assertTrue(analysisResult.getHitPathwayByStid("R-HSA-392499").getReactionsFound().contains("R-HSA-6809011"));

        assertEquals(0.001349, analysisResult.getHitPathwayByStid("R-HSA-5653656").getReactionsRatio(), 0.01);
        assertEquals(0.001199, analysisResult.getHitPathwayByStid("R-HSA-5653656").getEntitiesRatio(), 0.01);
    }
}