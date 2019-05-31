package matcher.tools;

import com.google.common.io.Files;
import model.Error;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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

    public static final String UTF8_BOM = "\uFEFF";

    private static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }

    public static List<String> readFile(String path) {
        File file = new File(path);
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readLines(file, Charset.forName("ISO-8859-1"));
            lines.set( 0, removeUTF8BOM(lines.get(0)) );
        } catch (IOException e) {
            System.err.println("The input file: " + path + " was not found.");
        }
        return lines;
    }

    public static BufferedReader getBufferedReaderForGzipFile(String path, String fileName) throws FileNotFoundException, IOException {
        if (!path.endsWith("/")) {
            path += "/";
        }
        File file = new File(path + fileName);
        InputStream fileStream = new FileInputStream(file);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, Charset.defaultCharset());
        return new BufferedReader(decoder);
    }

    static BufferedReader getBufferedReaderFromResource(String fileName) throws FileNotFoundException, IOException {

        BufferedReader br = null;
        InputStream fileStream = ClassLoader.getSystemResourceAsStream(fileName);
        Reader decoder = null;
        if (fileName.endsWith(".gz")) {
            InputStream gzipStream = new GZIPInputStream(fileStream);
            decoder = new InputStreamReader(gzipStream);
        } else {
            decoder = new InputStreamReader(fileStream);
        }
        br = new BufferedReader(decoder);

        return br;
    }

    public static void storeSerialized(Object obj, String path, String fileName) {
        if(path.length() > 0){
            if (!path.endsWith("/")) {
                path += "/";
            }
            File directory = new File(path);
            directory.mkdirs();
        }
        FileOutputStream fos = null;
        GZIPOutputStream gz;
        ObjectOutputStream oos;
        try {
            fos = new FileOutputStream(path + fileName);
            gz = new GZIPOutputStream(fos);
            oos = new ObjectOutputStream(gz);
            oos.writeObject(obj);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
