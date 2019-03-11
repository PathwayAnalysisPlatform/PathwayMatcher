package matcher;

import com.google.common.io.Files;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ReadLineEndingsTest {

    private static final String PATH = "src/test/resources/LineEndings/";

    @Test
        //Test reading a file with Unix line endings LF
    void readInputlineEndUnixTest() {
        System.out.println();
        List<String> lines = null;
        try {
            lines = Files.readLines(new File(PATH + "lineEndUnix.txt"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
            fail("The file should be read without errors.");
        }
        assertEquals(10, lines.size());
    }

    @Test
        //Test reading a file with Windows line endings CRLF
    void readInputlineEndWindowsTest() {
        List<String> lines = null;
        try {
            lines = Files.readLines(new File(PATH + "lineEndWindows.txt"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
            fail("The file should be read without errors.");
        }
        assertEquals(10, lines.size());
    }

    @Test
        //Test reading a file with Mac line endings CR
    void readInputlineEndMacTest() {
        List<String> lines = null;
        try {
            lines = Files.readLines(new File(PATH + "lineEndMac.txt"), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
            fail("The file should be read without errors.");
        }
        assertEquals(10, lines.size());
    }

}