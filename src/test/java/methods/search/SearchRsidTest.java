package methods.search;

import com.google.common.io.Files;
import model.InputType;
import model.Mapping;
import model.MatchType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchRsidTest {

    private static String resourcesPath = "src/test/resources/";
    private static String fastaFile = "src/main/resources/uniprot-all.fasta";

    private static Mapping mapping;

    @BeforeAll
    static void loadStaticMapping() throws FileNotFoundException {
        mapping = new Mapping(InputType.RSID, true, "");
    }

    @Test
    void searchWithRsId() throws FileNotFoundException {
        List<String> input = new ArrayList<>();
        input.add("rs121918101"); //P01308 not found
        input.add("rs28933985"); //P01308 not found
        input.add("rs10840447"); //P01308
        input.add("rs7110099"); //P01308
        input.add("rs555583938"); //P01308

        SearchResult searchResult = Search.searchWithRsId(input, mapping, true, "");

        assertEquals(5, input.size());
        assertEquals(1, searchResult.getHitProteins().size());
        assertTrue(searchResult.getHitProteins().contains("P01308"));
        assertEquals(21, searchResult.getHitPathways().size());
        assertTrue(searchResult.containsPathwayByStid("R-HSA-264876"));
        assertTrue(searchResult.containsPathwayByStid("R-HSA-74749"));
    }

    @Test
    void diabetesInYouthTest() throws IOException {
        List<String> input = Files.readLines(new File(resourcesPath + "GeneticVariants/RsId/DiabetesInYouth.txt"), Charset.defaultCharset());
        SearchResult searchResult = Search.searchWithRsId(input, mapping, true, "");

        assertTrue(searchResult.getHitProteins().contains("Q9NQB0"));
        assertFalse(searchResult.getHitProteins().contains("P07550"));
    }
}