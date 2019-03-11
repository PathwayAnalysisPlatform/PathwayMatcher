package methods.search;

import com.google.common.io.Files;
import model.InputType;
import model.Mapping;
import model.MatchType;
import model.ProteoformFormat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

import static org.junit.jupiter.api.Assertions.*;

class SearchProteoformTest {

    private static String resourcesPath = "src/test/resources/";

    private static Mapping mapping;

    @BeforeAll
    static void loadStaticMapping() {
        mapping = new Mapping(InputType.PROTEOFORM, true);
    }

    @Test
    void searchWithProteoformTest() throws IOException {
        SearchResult result = Search.searchWithProteoform(
                Files.readLines(new File(resourcesPath + "Proteoforms/Reactome/all_proteoforms.csv"), Charset.defaultCharset()),
                mapping,
                true,
                MatchType.STRICT,
                0L);

        assertEquals(13911, result.getInputProteoforms().size());
        assertEquals(mapping.getProteoformsToReactions().keySet().size(), result.getHitProteoforms().size());
    }

    @Test
        // All proteoforms of a protein
    void searchWithProteoformSet1FillHitsTest() throws IOException, ParseException {
        SearchResult result = Search.searchWithProteoform(
                Files.readLines(new File(resourcesPath + "Proteoforms/Simple/Set1.csv"), Charset.defaultCharset()),
                mapping,
                true,
                MatchType.SUBSET,
                0L);

        assertEquals(1, result.getHitProteins().size());
        assertEquals(21, result.getHitPathways().size());

        assertFalse(result.containsPathwayByStid("R-HSA-977225"));

        assertEquals(1, result.getHitPathwayByStid("R-HSA-199991").getEntitiesFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-199991").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109")));
        assertFalse(result.getHitPathwayByStid("R-HSA-199991").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:31,00798:43")));
        assertEquals(5, result.getHitPathwayByStid("R-HSA-199991").getReactionsFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-199991").getReactionsFound().contains("R-HSA-6807877"));
        assertTrue(result.getHitPathwayByStid("R-HSA-199991").getReactionsFound().contains("R-HSA-6809003"));
    }

    @Test
    void searchWithProteoformsInsulinTest() throws IOException, ParseException {
        SearchResult result = Search.searchWithProteoform(
                Files.readLines(new File(resourcesPath + "Proteoforms/Simple/Insulin.txt"), Charset.defaultCharset()),
                mapping,
                true,
                MatchType.SUPERSET,
                0L);

        assertEquals(1, result.getHitProteins().size());
        assertEquals(14, result.getHitPathways().size());

        assertFalse(result.containsPathwayByStid("R-HSA-977225"));

        assertTrue(result.containsPathwayByStid("R-HSA-392499"));
        assertTrue(result.getHitPathwayByStid("R-HSA-392499").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01308;00798:31,00798:43")));
        assertFalse(result.getHitPathwayByStid("R-HSA-392499").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01308;00087:53,00798:31,00798:43")));
        assertTrue(result.getHitPathwayByStid("R-HSA-392499").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01308")));
    }

    @Test
        // Some proteoforms of a protein
    void searchWithProteoformSet2FillHitsTest() throws IOException, ParseException {
        SearchResult result = Search.searchWithProteoform(
                Files.readLines(new File(resourcesPath + "Proteoforms/Simple/Set2.csv"), Charset.defaultCharset()),
                mapping,
                true,
                MatchType.SUPERSET,
                0L);

        assertEquals(14, result.getHitPathways().size());
        assertEquals(1, result.getHitProteins().size());

        assertFalse(result.containsPathwayByStid("R-HSA-977225"));

        assertTrue(result.containsPathwayByStid("R-HSA-392499"));
        assertEquals(2, result.getHitPathwayByStid("R-HSA-392499").getEntitiesFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-392499").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01308")));
        assertTrue(result.getHitPathwayByStid("R-HSA-392499").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01308;00798:31,00798:43")));
        assertFalse(result.getHitPathwayByStid("R-HSA-392499").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01308;00798:95,00798:96,00798:100,00798:109")));
        assertFalse(result.getHitPathwayByStid("R-HSA-392499").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109")));

        assertEquals(5, result.getHitPathwayByStid("R-HSA-392499").getReactionsFound().size());
        assertFalse(result.getHitPathwayByStid("R-HSA-392499").getReactionsFound().contains("R-HSA-977136"));
        assertTrue(result.getHitPathwayByStid("R-HSA-392499").getReactionsFound().contains("R-HSA-9023178"));
    }

    @Test
    void singleProteoformSearchTest() throws IOException, ParseException {
        SearchResult result = Search.searchWithProteoform(
                Files.readLines(new File(resourcesPath + "Proteoforms/Simple/SingleProteoform.txt"), Charset.defaultCharset()),
                mapping,
                true,
                MatchType.SUPERSET,
                0L);

        assertEquals(1, result.getHitProteins().size());
        assertEquals(11, result.getHitPathways().size());

        assertEquals(2, result.getHitPathwayByStid("R-HSA-168249").getEntitiesFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-168249").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("O43561-2;00048:127,00048:132,00048:171,00048:191,00048:226")));
        assertTrue(result.getHitPathwayByStid("R-HSA-168249").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("O43561-2;")));
        assertFalse(result.getHitPathwayByStid("R-HSA-168249").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("O43561;00048:156,00048:161,00048:200,00048:220,00048:255")));
        assertFalse(result.getHitPathwayByStid("R-HSA-168249").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("O43561;00048:200,00048:220")));
    }

    @Test
    void singleProteoformSearchStrictTest() throws IOException, ParseException {
        SearchResult result = Search.searchWithProteoform(
                Files.readLines(new File(resourcesPath + "Proteoforms/Simple/SingleProteoform.txt"), Charset.defaultCharset()),
                mapping,
                true,
                MatchType.STRICT,
                0L);

        assertEquals(1, result.getHitProteins().size());
        assertEquals(11, result.getHitPathways().size());

        assertEquals(1, result.getHitPathwayByStid("R-HSA-168249").getEntitiesFound().size());
        assertTrue(result.getHitPathwayByStid("R-HSA-168249").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("O43561-2;00048:127,00048:132,00048:171,00048:191,00048:226")));
        assertFalse(result.getHitPathwayByStid("R-HSA-168249").getEntitiesFound().contains(ProteoformFormat.SIMPLE.getProteoform("O43561-2;")));
    }
}