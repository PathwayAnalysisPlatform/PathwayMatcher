package matcher.tools;

import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

import static matcher.tools.FileHandler.createFile;
import static org.junit.jupiter.api.Assertions.*;

class FileHandlerTest {

    @AfterEach
    void deleteOutputFiles(TestInfo testInfo) {
        try {
            File directory = new File(testInfo.getTestMethod().get().getName() + "/");
            FileUtils.deleteDirectory(directory);
            assertFalse(directory.exists());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFileNoPath_fileDoesNotExist_Test(TestInfo testInfo) {
        String fileName = testInfo.getTestMethod().get().getName() + ".txt";

        try {
            BufferedWriter bw = createFile(fileName);
            bw.write(testInfo.getTestMethod().get().getName());
            bw.close();

            // Check that the file exists
            File file = new File(fileName);
            assertTrue(file.exists(), "The file was not created.");

            // Postcondition: Erase file
            FileUtils.deleteQuietly(file);
            assertFalse(file.exists(), "Traces left by this test: the test file was not deleted.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFileNoPath_pathEmtptyAndFileExists_Test(TestInfo testInfo) {
        String fileName = testInfo.getTestMethod().get().getName() + ".txt";

        try {
            File preFile = new File(fileName);
            FileWriter fw = new FileWriter(preFile);
            fw.write(testInfo.getTestMethod().get().getName() + " precondition");
            fw.close();
            assertTrue(preFile.exists(), "The precondition file was not created.");

            BufferedWriter bw = createFile(fileName);
            bw.write(testInfo.getTestMethod().get().getName() + " with the function under test");
            bw.close();

            // Check that the file exists
            File file = new File(fileName);
            assertTrue(file.exists(), "The file was not created.");

            // Postcondition: Erase file
            file.delete();
            assertFalse(file.exists(), "Traces left by this test: the test file was not deleted.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFileNoPath_pathSameDirectory_Test(TestInfo testInfo) {
        String fileName = "./" + testInfo.getTestMethod().get().getName() + ".txt";

        try {
            BufferedWriter bw = createFile(fileName);
            bw.write("createFile_pathSameDirectory_Test with the function under test");
            bw.close();

            // Check that the file exists
            File file = new File(fileName);
            assertTrue(file.exists(), "The file was not created.");

            // Postcondition: Delete the file
            file.delete();
            assertFalse(file.exists(), "Traces left by this test: the test file was not deleted.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFileNoPath_nonExistentPath_Test(TestInfo testInfo) {
        String fileName = "this/path/does/not/exist/" + testInfo.getTestMethod().get().getName() + ".txt";

        try {
            BufferedWriter bw = createFile(fileName);
            bw.write("createFile_nonExistentPath_Test with the function under test");
            bw.close();

            // Check that the file exists
            File file = new File(fileName);
            assertTrue(file.exists(), "The file was not created.");

            // Postcondition: Delete the file
            file.delete();
            assertFalse(file.exists(), "Traces left by this test: the test file was not deleted.");

            File directory = new File("this/");
            FileUtils.deleteDirectory(directory);
            assertFalse(directory.exists());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFileNoPath_withCorrectPathAndName_returnsBufferedWriter_Test(TestInfo testInfo) {
        String fileName = "this/path/does/not/exist/" + testInfo.getTestMethod().get().getName() + ".txt";

        try {
            // Create file
            BufferedWriter bw = createFile(fileName);
            // Write something to the file using the BufferedWriter
            bw.write("createFile_nonExistentPath_Test with the function under test");
            bw.close();

            // Check that the file exists
            File file = new File(fileName);
            // Read the contents of the file to check if they really are there.
            List<String> content = Files.readLines(file, Charset.forName("ISO-8859-1"));
            assertEquals("createFile_nonExistentPath_Test with the function under test", content.get(0), "The content writen to the file was not the same.");

            // Postcondition: Delete the file
            file.delete();
            assertFalse(file.exists(), "Traces left by this test: the test file was not deleted.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_prefixHasOnlyDirectory_createsDirectory_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        try {
            BufferedWriter bw = createFile(testName + "/", testName + ".tsv");
            assertTrue(new File(testName + "/").exists(), "Did not create directory from prefix.");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_prefixHasOnlyDirectory_createsFile_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        try {
            BufferedWriter bw = createFile(testName + "/", testName + ".tsv");
            assertTrue(new File(testName + "/" + testName + ".tsv").exists(), "Did not create file with prefix.");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_prefixHasPathAndFileName_createsDirectory_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        try {
            BufferedWriter bw = createFile(testName + "/" + "prefix", testName + ".tsv");
            assertTrue(new File(testName + "/").exists(), "Did not create directory from prefix.");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_prefixHasPathAndFileName_createsFile_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        try {
            BufferedWriter bw = createFile(testName + "/" + "prefix", testName + ".tsv");
            assertTrue(new File(testName + "/" + "prefix" + testName + ".tsv").exists(), "Did not create file from prefix.");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_prefixHasPathAndFileName_doesNotCreateFileNamePrefixDirectory_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        try {
            BufferedWriter bw = createFile(testName + "/" + "prefix", testName + ".tsv");
            bw.close();
            assertFalse(new File(testName + "/" + "prefix").exists(), "Should not create directory including the file name from prefix.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_prefixHasOnlyFileName_createsFile_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        try {
            BufferedWriter bw = createFile("prefix_", testName + ".tsv");
            bw.close();
            File file = new File("prefix_" + testName + ".tsv");
            assertTrue(file.exists(), "Did not create the file with the prefix.");
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_prefixHasOnlyFileName_noDirectoryCreated_Test(TestInfo testInfo) {
        String testName = testInfo.getTestMethod().get().getName();
        try {
            BufferedWriter bw = createFile("prefix_", testName + ".tsv");
            bw.close();
            File directory = new File("prefix_");
            assertFalse(directory.exists(), "Should not create a directory with the prefix.");
            File file = new File("prefix_" + testName + ".tsv");
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_prefixEmptyAndFileDoesNotExist_Test() {
        String path = "";
        String fileName = "createFile_pathEmptyAndFileDoesNotExist_Test.txt";

        try {
            BufferedWriter bw = createFile(path, fileName);
            bw.write("createFile_pathEmptyAndFileDoesNotExist_Test");
            bw.close();

            // Check that the file exists
            File file = new File(path + fileName);
            assertTrue(file.exists(), "The file was not created.");

            // Postcondition: Erase file
            FileUtils.deleteQuietly(file);
            assertFalse(file.exists(), "Traces left by this test: the test file was not deleted.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_prefixEmtptyAndFileExists_Test() {
        String path = "";
        String fileName = "createFile_pathEmtptyAndFileExists_Test.txt";

        try {
            File preFile = new File(path + fileName);
            FileWriter fw = new FileWriter(preFile);
            fw.write("createFile_pathEmtptyAndFileExists_Test precondition");
            fw.close();
            assertTrue(preFile.exists(), "The precondition file was not created.");

            BufferedWriter bw = createFile(path, fileName);
            bw.write("createFile_pathEmtptyAndFileExists_Test with the function under test");
            bw.close();

            // Check that the file exists
            File file = new File(path + fileName);
            assertTrue(file.exists(), "The file was not created.");

            // Postcondition: Erase file
            file.delete();
            assertFalse(file.exists(), "Traces left by this test: the test file was not deleted.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_prefixSameDirectory_Test() {
        String path = "./";
        String fileName = "createFile_pathSameDirectory_Test.txt";

        try {
            BufferedWriter bw = createFile(path, fileName);
            bw.write("createFile_pathSameDirectory_Test with the function under test");
            bw.close();

            // Check that the file exists
            File file = new File(path + fileName);
            assertTrue(file.exists(), "The file was not created.");

            // Postcondition: Delete the file
            file.delete();
            assertFalse(file.exists(), "Traces left by this test: the test file was not deleted.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_nonExistentPath_Test() {
        String path = "this/path/does/not/exist/";
        String fileName = "createFile_nonExistentPath_Test.txt";

        try {
            BufferedWriter bw = createFile(path, fileName);
            bw.write("createFile_nonExistentPath_Test with the function under test");
            bw.close();

            // Check that the file exists
            File file = new File(path + fileName);
            assertTrue(file.exists(), "The file was not created.");

            // Postcondition: Delete the file
            file.delete();
            assertFalse(file.exists(), "Traces left by this test: the test file was not deleted.");

            File directory = new File("this/");
            FileUtils.deleteDirectory(directory);
            assertFalse(directory.exists());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void createFile_withCorrectPathAndName_returnsBufferedWriter_Test() {
        String path = "this/path/does/not/exist/";
        String fileName = "createFile_nonExistentPath_Test.txt";

        try {
            // Create file
            BufferedWriter bw = createFile(path, fileName);
            // Write something to the file using the BufferedWriter
            bw.write("createFile_nonExistentPath_Test with the function under test");
            bw.close();

            // Check that the file exists
            File file = new File(path + fileName);
            // Read the contents of the file to check if they really are there.
            List<String> content = Files.readLines(file, Charset.forName("ISO-8859-1"));
            assertEquals("createFile_nonExistentPath_Test with the function under test", content.get(0), "The content writen to the file was not the same.");

            // Postcondition: Delete the file
            file.delete();
            assertFalse(file.exists(), "Traces left by this test: the test file was not deleted.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void readFile_givenNonExistentFile_returnNull_Test() {
        assertEquals(null, FileHandler.readFile("Nonexistentfile.txt"), "Should not return any value.");
    }

    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalErr = System.err;

    @Test
    void readFile_givenNonExistentFile_showErrorMessage_Test() {
        System.setErr(new PrintStream(errContent));
        FileHandler.readFile("Nonexistentfile.txt");
        assertTrue(errContent.toString().startsWith("The input file: Nonexistentfile.txt was not found."), "Error reading input file message not shown.");
        System.setErr(originalErr);
    }

    @Test
    void readFile_givenExistingFile_returnLinesList_Test() {
        List<String> lines = FileHandler.readFile("src/test/resources/Genes/CysticFibrosis.txt");
        assertEquals(11, lines.size(), "The number of lines read is not correct.");
        assertEquals("CFTR", lines.get(0), "Line 1 is not correct.");
        assertEquals("CXCL8", lines.get(10), "Last line of the file is not correct.");
    }
}