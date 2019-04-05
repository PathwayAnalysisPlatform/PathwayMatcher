package model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.TreeMultimap;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.*;

import static model.Mapping.getSerializedObject;
import static org.junit.jupiter.api.Assertions.*;

class MappingTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUpStreams() {
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
    void load_givenEmtyPath_loadUniProtMapsFromResources_Test() throws FileNotFoundException {
        Mapping mapping = new Mapping(InputType.UNIPROT, false, "");

        assertNotEquals(0, mapping.getReactions().size(),  "Did not load the reactions list.");
        assertNotEquals(0, mapping.getPathways().size(), "Did not load the pathway list.");
        assertNotEquals(0, mapping.getReactionsToPathways().size(), "Did not load the reactions to pathways mapping.");
        assertNotEquals(0, mapping.getProteinsToNames().size(), "Did not load the mapping from proteins to names.");
        assertNotEquals(0, mapping.getProteinsToReactions().size(),  "Did not load the mapping from proteins to reactions.");
    }

    @Test
    void load_givenEmtyPath_loadUniProtMapsFromAnotherPath_Test() throws FileNotFoundException {
        Mapping mapping = new Mapping(InputType.UNIPROT, false, "src/main/resources/");

        assertNotEquals(0, mapping.getReactions().size(), "Did not load the reactions list.");
        assertNotEquals(0, mapping.getPathways().size(), "Did not load the pathway list.");
        assertNotEquals(0, mapping.getReactionsToPathways().size(), "Did not load the reactions to pathways mapping.");
        assertNotEquals(0, mapping.getProteinsToNames().size(),  "Did not load the mapping from proteins to names.");
        assertNotEquals(0, mapping.getProteinsToReactions().size(), "Did not load the mapping from proteins to reactions.");
    }

    @Test
    void getSerializedObject_withEmtyPathAndNonexistentFile_throwFileNotFoundException_Test() {
        try {
            getSerializedObject("", "blabla.gz");
            fail("An exception should be thrown.");
        } catch (FileNotFoundException e) {
            assertEquals(e.getMessage(), "Could not find the file: " + "blabla.gz" + " in the jar file resources.");
        }
    }

    // Requires that the resources folder src/main/resources/ contains the static mapping files
    @Test
    void getSerializedObject_withEmtyPathAndExistentFile_loadFile_Test() {
        try {
            ImmutableMap<String, Reaction> reactions = (ImmutableMap<String, Reaction>) getSerializedObject("", "reactions.gz");
            assertNotEquals(reactions, null);
        } catch (FileNotFoundException e) {
            fail("Should be able to find the requested serialized file in the resources.");
            e.printStackTrace();
        }
    }

    @Test
    void getSerializedObject_withPathAndFileDoesNotExist_sendMessage_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        try {
            getSerializedObject(testName + "/", "reactions.gz");
            fail("An exception should be thrown.");
        } catch (FileNotFoundException e) {
            assertEquals(e.getMessage(), "Could not find the file: " + "reactions.gz" + " at the location: " + testName + "/", "The method threw and exception with another message.");
        }
    }

    @Test
    void getSerializedObject_withPathAndFileDOesNotExist_throwFileNotFoundException_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        assertThrows(FileNotFoundException.class, () -> {
            getSerializedObject(testName + "/", "reactions.gz");
        }, "Shoudl not find the serialized file and throw an exception");
    }

    // Requires that the src/main/test/ contains the static mapping files
    @Test
    void getSerializedObject_withPathAndFile_loadsGenesToProteinsFile_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        try {
            ImmutableSetMultimap<String, String> genesToProteins = (ImmutableSetMultimap<String, String>) getSerializedObject("src/main/resources/", "genesToProteins.gz");
            assertNotEquals(genesToProteins, null);
        } catch (FileNotFoundException e) {
            fail("Should be able to find the requested serialized file.");
            e.printStackTrace();
        }
    }

    @Test
    void getSerializedObject_withPathAndFile_loadsReactonsFile_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        try {
            ImmutableMap<String, Reaction> reactions = (ImmutableMap<String, Reaction>) getSerializedObject("src/main/resources/", "reactions.gz");
            assertNotEquals(reactions, null);
        } catch (FileNotFoundException e) {
            fail("Should be able to find the requested serialized file.");
            e.printStackTrace();
        }
    }

    @Test
    void getSerializedObject_withPathNotEndingInSlashAndValidFile_loadsTheFile_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        try {
            ImmutableMap<String, Reaction> reactions = (ImmutableMap<String, Reaction>) getSerializedObject("src/main/resources", "reactions.gz");
            assertNotEquals(reactions, null);
        } catch (FileNotFoundException e) {
            fail("Should be able to find the requested serialized file.");
            e.printStackTrace();
        }
    }

    @Test
    void Constructor_givenDirectoryMissingReactionsFile_showMessageRequestingFile_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        File directory = new File(testName + "/");
        directory.mkdirs();
        try {
            Mapping mapping = new Mapping(InputType.UNIPROT, false, testName + "/");
            fail("Should throw an exception");
        } catch (FileNotFoundException e) {
            assertEquals("Could not find the file: reactions.gz at the location: " + testName + "/",
                    e.getMessage(),
                    "Did not show message that reactions.gz file is missing.");
        }
    }

}