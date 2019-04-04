package matcher.tools;

import com.google.common.io.Files;
import model.Error;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileHandler {

    public static BufferedWriter createFile(String file_path) {

        BufferedWriter br = null;
        try {
            if (file_path.length() == 0) {
                throw new IOException("Cannot create a file with no name and path.");
            }

            File file = new File(file_path);
            if (file.getParent() != null) {
                if (file.getParent().length() > 0) {
                    File outputDir = new File(file.getParent());
                    if (!outputDir.exists()) {
                        if (!outputDir.mkdirs()) {
                            throw new IOException();
                        }
                    }
                }
            }

            br = new BufferedWriter(new FileWriter(file_path));
        } catch (IOException e) {
            System.err.println(model.Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getMessage());
            return null;
        }
        return br;
    }

    public static BufferedWriter createFile(String prefix, String file) {

        BufferedWriter br = null;

        try {
            if (prefix.length() == 0 && file.length() == 0) {
                throw new IOException("Cannot create a file with no name and path.");
            }
            int indexOfSlash = prefix.lastIndexOf("/");
            if (indexOfSlash > 0) {   // Its a file name only prefix
                String path = prefix.substring(0, indexOfSlash + 1);
                File outputDir = new File(path);
                if (!outputDir.exists()) {
                    if (!outputDir.mkdirs()) {
                        throw new IOException();
                    }
                }
            }
            br = new BufferedWriter(new FileWriter(prefix + file));
        } catch (IOException e) {
            System.err.println(model.Error.COULD_NOT_WRITE_TO_OUTPUT_FILES.getMessage());
            return null;
        }
        return br;
    }

    public static List<String> readFile(String path) {
        File file = new File(path);
        try {
            return Files.readLines(file, Charset.forName("ISO-8859-1"));
        } catch (IOException e) {
            System.err.println("The input file: " + path + " was not found.");
        }
        return null;
    }
}
