package matcher;

import com.google.common.io.Files;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathwayMatcherGraphTest {

    static String proteinVerticesFile = "proteinVertices.tsv";
    static String proteinInternalEdgesFile = "proteinInternalEdges.tsv";
    static String proteinExternalEdgesFile = "proteinExternalEdges.tsv";

    static String geneVerticesFile = "geneVertices.tsv";
    static String geneInternalEdgesFile = "geneInternalEdges.tsv";
    static String geneExternalEdgesFile = "geneExternalEdges.tsv";

    static String proteoformVerticesFile = "proteoformVertices.tsv";
    static String proteoformInternalEdgesFile = "proteoformInternalEdges.tsv";
    static String proteoformExternalEdgesFile = "proteoformExternalEdges.tsv";

    @BeforeEach
    void beforeEach() {
        try {
            java.nio.file.Files.deleteIfExists(Paths.get(proteinVerticesFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void genesGraphDiabetesInYouthTest() throws IOException {
        String[] args = {
                "-t", "genes",
                "-i", "src/test/resources/Genes/DiabetesInYouth.txt",
                "-o", "output/genesGraphDiabetesInYouthTest/",
                "-tlp",
                "--graph", "-gp", "-gu"};
        Main.main(args);

        String searchFile = "output/genesGraphDiabetesInYouthTest/search.tsv";
        String analysisFile = "output/genesGraphDiabetesInYouthTest/analysis.tsv";

        List<String> search = Files.readLines(new File(searchFile), Charset.defaultCharset());
        assertEquals(34, search.size());

        List<String> analysis = Files.readLines(new File(analysisFile), Charset.defaultCharset());
        assertEquals(10, analysis.size());
    }

    @Test
    public void verticesTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "output/verticesTest/",
                "-tlp",
                "--graph", "-gp", "-gg"};
        Main.main(args);

        List<String> lines = Files.readLines(new File("output/verticesTest/" + proteinVerticesFile), Charset.defaultCharset());
        assertEquals(2, lines.size());
        assertTrue(lines.contains("P01308\tInsulin"));

        List<String> genes = Files.readLines(new File("output/verticesTest/" + geneVerticesFile), Charset.defaultCharset());
        assertEquals(2, genes.size());
        assertTrue(genes.contains("INS\tInsulin"));

        List<String> proteoforms = Files.readLines(new File("output/verticesTest/" + proteoformVerticesFile), Charset.defaultCharset());
        assertEquals(6, proteoforms.size());
        assertTrue(proteoforms.contains("P01308;00087:53,00798:31,00798:43\tInsulin"));
        assertTrue(proteoforms.contains("P01308;00798:31,00798:43,00798:95,00798:96,00798:100,00798:109\tInsulin"));
    }

    @Test
    public void verticesTest2() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "output/verticesTest2/",
                "-tlp",
                "--graph"};
        Main.main(args);

        List<String> lines = Files.readLines(new File("output/verticesTest2/" + proteinVerticesFile), Charset.defaultCharset());
        assertEquals(2, lines.size());
        assertTrue(lines.contains("P01308\tInsulin"));
    }

    @Test
    void createGraphSet2Test() throws IOException {
        String[] args = {"-t", "uniprot",
                "-i", "src/test/resources/Proteins/UniProt/Set1.txt",
                "-o", "output/createGraphSet2Test/",
                "-tlp",
                "-g"};
        Main.main(args);

        List<String> vertices = Files.readLines(new File("output/createGraphSet2Test/" + proteinVerticesFile), Charset.defaultCharset());
        assertEquals(3, vertices.size());
        assertTrue(vertices.contains("P68871\tHemoglobin subunit beta"));
        assertTrue(vertices.contains("P69905\tHemoglobin subunit alpha"));

        List<String> internalEdges = Files.readLines(new File("output/createGraphSet2Test/" + proteinInternalEdgesFile), Charset.defaultCharset());
        assertTrue(internalEdges.contains("P68871\tP69905\tReaction\tR-HSA-2168885\toutput\tinput"));
        assertTrue(internalEdges.contains("P68871\tP69905\tComplex\tR-HSA-1237320\tcomponent\tcomponent"));

        List<String> externalEdges = Files.readLines(new File("output/createGraphSet2Test/" + proteinExternalEdgesFile), Charset.defaultCharset());
        assertTrue(externalEdges.contains("P00738\tP68871\tReaction\tR-HSA-6798745\tinput\tinput"));
        assertFalse(externalEdges.contains("P68871\tP00738\tReaction\tR-HSA-6798745\tinput\tinput"));
        assertTrue(externalEdges.contains("P69905\tQ86VB7\tComplex\tR-HSA-2168879\tcomponent\tcomponent"));
        assertFalse(externalEdges.contains("Q86VB7\tP69905\tComplex\tR-HSA-2168879\tcomponent\tcomponent"));
        assertTrue(externalEdges.contains("P00738\tP68871\tComplex\tR-HSA-2168879\tcomponent\tcomponent"));
        assertFalse(externalEdges.contains("P68871\tP00738\tComplex\tR-HSA-2168879\tcomponent\tcomponent"));
    }

    @Test
    void createGraphInsulinTest() throws IOException {
        String[] args = {"-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "output/createGraphInsulinTest/",
                "-tlp",
                "-g"};
        Main.main(args);

        List<String> vertices = Files.readLines(new File("output/createGraphInsulinTest/" + proteinVerticesFile), Charset.defaultCharset());
        assertEquals(2, vertices.size());
        assertTrue(vertices.contains("P01308\tInsulin"));

        List<String> internalEdges = Files.readLines(new File("output/createGraphInsulinTest/" + proteinInternalEdgesFile), Charset.defaultCharset());
        assertEquals(1, internalEdges.size());

        List<String> externalEdges = Files.readLines(new File("output/createGraphInsulinTest/" + proteinExternalEdgesFile), Charset.defaultCharset());
        assertTrue(externalEdges.contains("P01308\tP29120\tReaction\tR-HSA-9023196\tinput\tcatalyst"));

        assertTrue(externalEdges.contains("P01308\tQ01484\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
        assertTrue(externalEdges.contains("P01308\tQ12955\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
        assertTrue(externalEdges.contains("P01308\tP35606\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
    }

    @Test
    void reactionNeighboursTest() throws IOException {
        String[] args = {"-t", "uniprot",
                "-i", "src/test/resources/Proteins/Valid/singleProtein.txt",
                "-o", "output/createGraphInsulinTest/",
                "-tlp",
                "-g"};
        Main.main(args);

        List<String> vertices = Files.readLines(new File("output/createGraphInsulinTest/" + proteinVerticesFile), Charset.defaultCharset());
        assertEquals(2, vertices.size());
        assertTrue(vertices.contains("P01308\tInsulin"));

        List<String> internalEdges = Files.readLines(new File("output/createGraphInsulinTest/" + proteinInternalEdgesFile), Charset.defaultCharset());
        assertEquals(1, internalEdges.size());

        List<String> externalEdges = Files.readLines(new File("output/createGraphInsulinTest/" + proteinExternalEdgesFile), Charset.defaultCharset());
        assertTrue(externalEdges.contains("P01308\tP29120\tReaction\tR-HSA-9023196\tinput\tcatalyst"));

        assertTrue(externalEdges.contains("P01308\tQ01484\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
        assertTrue(externalEdges.contains("P01308\tQ12955\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
        assertTrue(externalEdges.contains("P01308\tP35606\tComplex\tR-HSA-6808913\tcomponent\tcomponent"));
    }

   @Test
    void allProteinsTest() throws IOException {
        String[] args = {
                "-t", "uniprot",
                "-i", "src/test/resources/Proteins/UniProt/uniprot-all.list",
                "-o", "output/",
                "-tlp",
                "-gu", "-gp", "-gg"
        };
       Main.main(args);

       List<String> proteins = Files.readLines(new File("output/" + proteinVerticesFile), Charset.defaultCharset());
       assertEquals(10738, proteins.size());

       List<String> genes = Files.readLines(new File("output/" + geneVerticesFile), Charset.defaultCharset());
       assertEquals(24622, genes.size());

       List<String> proteoforms = Files.readLines(new File("output/" + proteoformVerticesFile), Charset.defaultCharset());
       assertEquals(13870, proteoforms.size());
   }

}