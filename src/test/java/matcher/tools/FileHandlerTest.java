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

import static org.junit.jupiter.api.Assertions.*;

class FileHandlerTest {

    @Test
    void createFileNoPath_fileDoesNotExist_Test(TestInfo testInfo) {
        String fileName = testInfo.getTestMethod().get().getName() + ".txt";

        try {
            BufferedWriter bw = FileHandler.createFile(fileName);
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

            BufferedWriter bw = FileHandler.createFile(fileName);
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
            BufferedWriter bw = FileHandler.createFile(fileName);
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
            BufferedWriter bw = FileHandler.createFile(fileName);
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
            BufferedWriter bw = FileHandler.createFile(fileName);
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
    void createFile_pathEmptyAndFileDoesNotExist_Test() {
        String path = "";
        String fileName = "createFile_pathEmptyAndFileDoesNotExist_Test.txt";

        try {
            BufferedWriter bw = FileHandler.createFile(path, fileName);
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
    void createFile_pathEmtptyAndFileExists_Test() {
        String path = "";
        String fileName = "createFile_pathEmtptyAndFileExists_Test.txt";

        try {
            File preFile = new File(path + fileName);
            FileWriter fw = new FileWriter(preFile);
            fw.write("createFile_pathEmtptyAndFileExists_Test precondition");
            fw.close();
            assertTrue(preFile.exists(), "The precondition file was not created.");

            BufferedWriter bw = FileHandler.createFile(path, fileName);
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
    void createFile_pathSameDirectory_Test() {
        String path = "./";
        String fileName = "createFile_pathSameDirectory_Test.txt";

        try {
            BufferedWriter bw = FileHandler.createFile(path, fileName);
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
            BufferedWriter bw = FileHandler.createFile(path, fileName);
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
            BufferedWriter bw = FileHandler.createFile(path, fileName);
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